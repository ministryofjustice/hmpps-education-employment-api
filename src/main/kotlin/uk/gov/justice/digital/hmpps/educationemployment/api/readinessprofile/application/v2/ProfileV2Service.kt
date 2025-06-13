package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.AlreadyExistsException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.InvalidStateException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.ProfileService
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.StatusChangeUpdateRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfileRepository
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.domain.TimeProvider
import java.time.LocalDate
import java.time.LocalDateTime

const val PROFILE_SCHEMA_VERSION = "2.0"
private const val PROFILE_SCHEMA_PREVIOUS_VERSION = "1.0"

@Service
class ProfileV2Service(
  private val readinessProfileRepository: ReadinessProfileRepository,
  private val timeProvider: TimeProvider,
  private val objectMapper: ObjectMapper,
) : ProfileService<Profile> {
  private val typeRefProfile by lazy { object : TypeReference<Profile>() {} }

  private val emptyJsonArray: JsonNode get() = objectMapper.readTree("[]")

  override fun createProfileForOffender(
    userId: String,
    offenderId: String,
    bookingId: Long,
    profile: Profile,
  ): ReadinessProfile {
    checkArgumentNotNull(profile.prisonId, "prisonId")
    when {
      profile.supportAccepted != null && profile.supportDeclined != null -> throw InvalidStateException(offenderId)
      readinessProfileRepository.existsById(offenderId) -> throw AlreadyExistsException(offenderId)
    }
    profile.supportDeclined?.run { checkDeclinedProfileStatus(profile, offenderId) }

    val currentTime = timeProvider.now()
    profile.apply {
      setMetaDataOnSupportState(this, userId, currentTime)
      statusChange = false
      statusChangeType = StatusChange.NEW
      within12Weeks ?: run { within12Weeks = true }
      prisonName ?: run { prisonName = "" }
    }
    return readinessProfileRepository.save(
      ReadinessProfile(
        offenderId = offenderId,
        bookingId = bookingId,
        createdBy = userId,
        createdDateTime = currentTime,
        modifiedBy = userId,
        modifiedDateTime = currentTime,
        schemaVersion = PROFILE_SCHEMA_VERSION,
        profileData = profile.json(),
        notesData = emptyJsonArray,
        new = true,
      ),
    )
  }

  override fun updateProfileForOffender(
    userId: String,
    offenderId: String,
    bookingId: Long,
    profile: Profile,
  ): ReadinessProfile {
    checkArgumentNotNull(profile.prisonId, "prisonId")

    val profileToUpdate: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    val storedCoreProfile: Profile = parseProfile(profileToUpdate.profileData)
    val currentTime = timeProvider.now()

    when {
      storedCoreProfile.supportAccepted == null && storedCoreProfile.supportDeclined == null ->
        if (profile.supportAccepted != null && profile.supportDeclined != null) {
          throw InvalidStateException(offenderId)
        }

      storedCoreProfile.supportAccepted != null && profile.supportAccepted != null ->
        if (profile.supportAccepted != storedCoreProfile.supportAccepted) {
          updateAcceptedStatusList(profile, userId, currentTime)
        }

      storedCoreProfile.supportDeclined != null && profile.supportDeclined != null ->
        if (profile.supportDeclined != storedCoreProfile.supportDeclined) {
          updateDeclinedStatusList(profile, userId, offenderId, currentTime)
        }

      storedCoreProfile.supportAccepted != null && profile.supportDeclined != null ->
        updateProfileDeclinedStatusChange(profile, userId, offenderId, profileToUpdate, currentTime)

      storedCoreProfile.supportDeclined != null && profile.supportAccepted != null ->
        updateProfileAcceptStatusChange(profile, userId, offenderId, profileToUpdate, currentTime)
    }

    with(profileToUpdate) {
      profile.within12Weeks ?: run { profile.within12Weeks = true }
      schemaVersion = PROFILE_SCHEMA_VERSION
      profileData = profile.json()
      modifiedBy = userId
      modifiedDateTime = currentTime
    }
    return readinessProfileRepository.save(profileToUpdate)
  }

  override fun changeStatusForOffender(
    userId: String,
    offenderId: String,
    statusChangeUpdateRequestDTO: StatusChangeUpdateRequestDTO?,
  ): ReadinessProfile {
    checkNotNull(statusChangeUpdateRequestDTO)
    val storedProfile: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    val currentTime by lazy { timeProvider.now() }
    val storedCoreProfile: Profile = statusChangeUpdateRequestDTO.let { request ->
      when {
        request.supportDeclined != null ->
          transitToDeclinedForOffender(userId, offenderId, request, storedProfile, currentTime).apply { status = request.status }
            .also { checkDeclinedProfileStatus(it, offenderId) }

        request.supportAccepted != null ->
          transitToAcceptedForOffender(userId, offenderId, request, storedProfile, currentTime).apply { status = request.status }

        else -> when (request.status) {
          ProfileStatus.READY_TO_WORK, ProfileStatus.NO_RIGHT_TO_WORK -> parseProfile(storedProfile.profileData).apply { status = request.status }

          else -> throw InvalidStateException(offenderId)
        }
      }
    }.apply {
      statusChangeDate = currentTime
      statusChange = true
    }

    storedProfile.apply {
      profileData = storedCoreProfile.json()
      modifiedBy = userId
      modifiedDateTime = currentTime
    }
    return readinessProfileRepository.save(storedProfile)
  }

  override fun getProfilesForOffenders(offenders: List<String>) = readinessProfileRepository.findAllById(offenders).migrateSchema()

  override fun getProfileForOffender(offenderId: String): ReadinessProfile = readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId)).migrateSchema()

  override fun getProfileForOffenderFilterByPeriod(
    prisonNumber: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): ReadinessProfile {
    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
      throw IllegalArgumentException("fromDate cannot be after toDate")
    }

    val readinessProfile = getProfileForOffender(prisonNumber)
    val profile = parseProfile(readinessProfile.profileData)
    if (fromDate != null || toDate != null) {
      toDate?.let {
        if (readinessProfile.createdDateTime.toLocalDate().isAfter(it)) {
          throw NotFoundException(prisonNumber)
        }
      }
      fromDate?.let { from ->
        if (fromDate.isAfter(readinessProfile.modifiedDateTime.toLocalDate())) {
          profile.supportAccepted = null
          profile.supportDeclined = null
        } else {
          val acceptedDate = profile.supportAccepted?.modifiedDateTime?.toLocalDate()
          if (acceptedDate != null && acceptedDate.isBefore(from)) {
            profile.supportAccepted = null
          }

          val declinedDate = profile.supportDeclined?.modifiedDateTime?.toLocalDate()
          if (declinedDate != null && declinedDate.isBefore(from)) {
            profile.supportDeclined = null
          }
        }
      }
      toDate?.let { to ->
        val acceptedDate = profile.supportAccepted?.modifiedDateTime?.toLocalDate()
        if (acceptedDate != null && acceptedDate.isAfter(to)) {
          profile.supportAccepted = null
        }

        val declinedDate = profile.supportDeclined?.modifiedDateTime?.toLocalDate()
        if (declinedDate != null && declinedDate.isAfter(to)) {
          profile.supportDeclined = null
        }
      }
    }

    readinessProfile.profileData = profile.json()
    return readinessProfile
  }

  private fun checkDeclinedProfileStatus(profile: Profile, offenderId: String) = when {
    profile.status == ProfileStatus.SUPPORT_NEEDED -> throw InvalidStateException(offenderId)
    else -> true
  }

  private fun setMetaDataOnSupportState(profile: Profile, userId: String, currentTime: LocalDateTime) {
    profile.supportAccepted?.let {
      it.modifiedBy = userId
      it.modifiedDateTime = currentTime
    }
    profile.supportDeclined?.let {
      it.modifiedBy = userId
      it.modifiedDateTime = currentTime
    }
  }

  private fun setProfileValues(profileToUpdate: Profile, revisedProfile: Profile, currentTime: LocalDateTime) {
    profileToUpdate.apply {
      supportDeclined = revisedProfile.supportDeclined
      supportAccepted = revisedProfile.supportAccepted
      statusChangeDate = currentTime
      statusChange = true
      statusChangeType = revisedProfile.statusChangeType
    }
  }

  private fun transitToAcceptedForOffender(
    userId: String,
    offenderId: String,
    statusChangeRequest: StatusChangeUpdateRequestDTO,
    storedProfile: ReadinessProfile,
    currentTime: LocalDateTime,
  ) = parseProfile(storedProfile.profileData).also { checkDeclinedProfileStatus(it, offenderId) }.apply {
    statusChangeDate = currentTime
    statusChangeType = StatusChange.DECLINED_TO_ACCEPTED
    supportDeclined = null
    supportAccepted = checkNotNull(statusChangeRequest.supportAccepted).apply {
      modifiedBy = userId
      modifiedDateTime = currentTime
    }
    status = statusChangeRequest.status
  }

  private fun transitToDeclinedForOffender(
    userId: String,
    offenderId: String,
    statusChangeRequest: StatusChangeUpdateRequestDTO,
    storedProfile: ReadinessProfile,
    currentTime: LocalDateTime,
  ) = parseProfile(storedProfile.profileData).apply {
    statusChangeDate = currentTime
    statusChangeType = StatusChange.ACCEPTED_TO_DECLINED
    supportDeclined = checkNotNull(statusChangeRequest.supportDeclined).apply {
      modifiedBy = userId
      modifiedDateTime = currentTime
    }
    status = statusChangeRequest.status
  }.also { checkDeclinedProfileStatus(it, offenderId) }

  private fun updateProfileAcceptStatusChange(
    profile: Profile,
    userId: String,
    offenderId: String,
    storedProfile: ReadinessProfile,
    currentTime: LocalDateTime,
  ) {
    val statusChangeRequest = StatusChangeUpdateRequestDTO(profile.supportAccepted!!, null, profile.status)
    transitToAcceptedForOffender(userId, offenderId, statusChangeRequest, storedProfile, currentTime)
      .also { setProfileValues(profile, it, currentTime) }
  }

  private fun updateProfileDeclinedStatusChange(
    profile: Profile,
    userId: String,
    offenderId: String,
    storedProfile: ReadinessProfile,
    currentTime: LocalDateTime,
  ) {
    val statusChangeRequest = StatusChangeUpdateRequestDTO(null, profile.supportDeclined!!, profile.status)
    transitToDeclinedForOffender(userId, offenderId, statusChangeRequest, storedProfile, currentTime)
      .also { setProfileValues(profile, it, currentTime) }
    checkDeclinedProfileStatus(profile, offenderId)
  }

  private fun updateAcceptedStatusList(profile: Profile, userId: String, currentTime: LocalDateTime) {
    profile.supportAccepted?.apply {
      modifiedBy = userId
      modifiedDateTime = currentTime
    }
  }

  private fun updateDeclinedStatusList(profile: Profile, userId: String, offenderId: String, currentTime: LocalDateTime) {
    profile.supportDeclined?.apply {
      modifiedBy = userId
      modifiedDateTime = currentTime
    }
    checkDeclinedProfileStatus(profile, offenderId)
  }

  private fun parseProfile(profileData: JsonNode): Profile = objectMapper.treeToValue(profileData, typeRefProfile)

  private fun List<ReadinessProfile>.migrateSchema() = map { it.migrateSchema() }.toList()

  private fun ReadinessProfile.migrateSchema() = when (schemaVersion) {
    PROFILE_SCHEMA_PREVIOUS_VERSION -> parseProfile(profileData).apply {
      within12Weeks = within12Weeks ?: true
      prisonId = prisonId ?: ""
    }.let { this.copy(profileData = it.json(), schemaVersion = PROFILE_SCHEMA_VERSION) }

    else -> this
  }

  private fun checkArgumentNotNull(argument: Any?, argumentName: String) = checkArgumentNotNull(argument, { "$argumentName is missing" })
  private fun checkArgumentNotNull(argument: Any?, lazyMessage: () -> Any) {
    if (argument == null) {
      throw IllegalArgumentException(lazyMessage().toString())
    }
  }

  private fun Profile.json(): JsonNode = objectMapper.valueToTree(this)
}
