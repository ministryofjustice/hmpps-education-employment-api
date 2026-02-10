@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v1

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.AlreadyExistsException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.InvalidStateException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v1.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.ProfileService
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.StatusChangeUpdateRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfileRepository
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.domain.TimeProvider
import java.time.LocalDate

const val PROFILE_SCHEMA_VERSION = "1.0"

@Deprecated(
  message = "Use v2 instead",
  replaceWith = ReplaceWith("ProfileV2Service", "uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2"),
)
@Service
class ProfileV1Service(
  private val readinessProfileRepository: ReadinessProfileRepository,
  private val timeProvider: TimeProvider,
  private val objectMapper: ObjectMapper,
) : ProfileService<Profile, ReadinessProfile> {
  private val typeRefProfile by lazy { object : TypeReference<Profile>() {} }
  private val emptyJsonArray: JsonNode get() = objectMapper.readTree("[]")

  override fun createProfileForOffender(
    userId: String,
    offenderId: String,
    bookingId: Long,
    profile: Profile,
  ): ReadinessProfile {
    if (readinessProfileRepository.existsById(offenderId)) {
      throw AlreadyExistsException(offenderId)
    }
    if (profile.supportAccepted != null && profile.supportDeclined != null) {
      throw InvalidStateException(offenderId)
    }
    setMiscellaneousAttributesOnSupportState(profile, userId, offenderId)
    profile.statusChange = false
    profile.statusChangeType = StatusChange.NEW
    profile.supportAccepted_history = mutableListOf()
    profile.supportDeclined_history = mutableListOf()
    return readinessProfileRepository.save(
      ReadinessProfile(
        offenderId = offenderId,
        bookingId = bookingId,
        createdBy = userId,
        createdDateTime = timeProvider.now(),
        modifiedBy = userId,
        modifiedDateTime = timeProvider.now(),
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
    val storedProfile: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    val storedCoreProfile: Profile = parseProfile(storedProfile.profileData)
    profile.supportAccepted_history = storedCoreProfile.supportAccepted_history
    profile.supportDeclined_history = storedCoreProfile.supportDeclined_history
    if (storedCoreProfile.supportAccepted == null && storedCoreProfile.supportDeclined == null && profile.supportAccepted != null && profile.supportDeclined != null) {
      throw InvalidStateException(offenderId)
    } else if (storedCoreProfile.supportAccepted != null &&
      profile.supportAccepted != null &&
      !profile.supportAccepted!!.equals(
        storedCoreProfile.supportAccepted,
      )
    ) {
      updateAcceptedStatusList(profile, storedCoreProfile, userId)
    } else if (storedCoreProfile.supportDeclined != null &&
      profile.supportDeclined != null &&
      !profile.supportDeclined?.equals(
        storedCoreProfile.supportDeclined,
      )!!
    ) {
      updateDeclinedStatusList(profile, storedCoreProfile, userId, offenderId)
    } else if (storedCoreProfile.supportAccepted != null && profile.supportDeclined != null) {
      updateProfileDeclinedStatusChange(profile, userId, offenderId, storedProfile)
    } else if (storedCoreProfile.supportDeclined != null && profile.supportAccepted != null) {
      updateProfileAcceptStatusChange(profile, userId, offenderId, storedProfile)
    }

    storedProfile.profileData = profile.json()
    storedProfile.modifiedBy = userId
    storedProfile.modifiedDateTime = timeProvider.now()
    readinessProfileRepository.save(storedProfile)
    return storedProfile
  }

  override fun changeStatusForOffender(
    userId: String,
    offenderId: String,
    statusChangeUpdateRequestDTO: StatusChangeUpdateRequestDTO?,
  ): ReadinessProfile {
    val storedProfile: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    val storedCoreProfile: Profile?
    if (statusChangeUpdateRequestDTO != null && statusChangeUpdateRequestDTO.supportDeclined != null) {
      storedCoreProfile =
        changeStatusToDeclinedForOffender(userId, offenderId, statusChangeUpdateRequestDTO, storedProfile)
      storedCoreProfile.status = statusChangeUpdateRequestDTO.status
      checkDeclinedProfileStatus(storedCoreProfile, offenderId)
    } else if (statusChangeUpdateRequestDTO != null && statusChangeUpdateRequestDTO.supportAccepted != null) {
      storedCoreProfile =
        changeStatusToAcceptedForOffender(userId, offenderId, statusChangeUpdateRequestDTO, storedProfile)
      storedCoreProfile.status = statusChangeUpdateRequestDTO.status
    } else if (statusChangeUpdateRequestDTO!!.status.equals(ProfileStatus.READY_TO_WORK) || statusChangeUpdateRequestDTO.status.equals(ProfileStatus.NO_RIGHT_TO_WORK)) {
      storedCoreProfile = parseProfile(storedProfile.profileData)
      storedCoreProfile.status = statusChangeUpdateRequestDTO.status
    } else {
      throw InvalidStateException(offenderId)
    }

    storedCoreProfile.statusChangeDate = timeProvider.now()
    storedCoreProfile.statusChange = true
    storedProfile.profileData = storedCoreProfile.json()
    storedProfile.modifiedBy = userId
    storedProfile.modifiedDateTime = timeProvider.now()
    readinessProfileRepository.save(storedProfile)
    return storedProfile
  }

  override fun getProfilesForOffenders(offenders: List<String>) = readinessProfileRepository.findAllById(offenders)

  override fun getProfileForOffender(offenderId: String): ReadinessProfile = readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))

  @Throws(NotFoundException::class, IllegalArgumentException::class)
  override fun getProfilesForOffenderFilterByPeriod(
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
      fromDate?.run {
        if (fromDate.isAfter(readinessProfile.modifiedDateTime.toLocalDate())) {
          profile.supportAccepted_history?.clear()
          profile.supportDeclined_history?.clear()
        } else {
          val recentAcceptedDateTimeAtBeginning =
            profile.supportAccepted_history?.filter { it.modifiedDateTime?.toLocalDate()?.isBefore(fromDate) ?: false }
              ?.maxByOrNull { it.modifiedDateTime!! }?.modifiedDateTime
          profile.supportAccepted_history?.retainAll {
            it.modifiedDateTime == null ||
              !(it.modifiedDateTime!!.toLocalDate().isBefore(fromDate)) ||
              it.modifiedDateTime!!.isEqual(recentAcceptedDateTimeAtBeginning)
          }
          val recentDeclinedDateTimeAtBeginning =
            profile.supportDeclined_history?.filter { it.modifiedDateTime?.toLocalDate()?.isBefore(fromDate) ?: false }
              ?.maxByOrNull { it.modifiedDateTime!! }?.modifiedDateTime
          profile.supportDeclined_history?.retainAll {
            it.modifiedDateTime == null ||
              !(it.modifiedDateTime!!.toLocalDate().isBefore(fromDate)) ||
              (recentDeclinedDateTimeAtBeginning != null && it.modifiedDateTime!!.isEqual(recentDeclinedDateTimeAtBeginning))
          }
        }
      }
      toDate?.run {
        profile.supportAccepted_history?.retainAll {
          it.modifiedDateTime == null || !(it.modifiedDateTime!!.toLocalDate().isAfter(toDate))
        }
        profile.supportDeclined_history?.retainAll {
          it.modifiedDateTime == null || !(it.modifiedDateTime!!.toLocalDate().isAfter(toDate))
        }
      }
    }
    profile.supportAccepted_history?.sortByDescending { it.modifiedDateTime }
    profile.supportDeclined_history?.sortByDescending { it.modifiedDateTime }

    readinessProfile.profileData = profile.json()
    return readinessProfile
  }

  fun parseProfile(profileData: JsonNode): Profile = objectMapper.treeToValue(profileData, typeRefProfile)

  private fun checkDeclinedProfileStatus(profile: Profile, offenderId: String): Boolean {
    if (profile.status.equals(ProfileStatus.SUPPORT_NEEDED)) {
      throw InvalidStateException(offenderId)
    }
    return true
  }

  private fun setMiscellaneousAttributesOnSupportState(profile: Profile, userId: String, offenderId: String) {
    if (profile.supportAccepted != null) {
      profile.supportAccepted?.modifiedBy = userId
      profile.supportAccepted?.modifiedDateTime = timeProvider.now()
    }

    if (profile.supportDeclined != null) {
      profile.supportDeclined?.modifiedBy = userId
      profile.supportDeclined?.modifiedDateTime = timeProvider.now()
      checkDeclinedProfileStatus(profile, offenderId)
    }
  }

  private fun changeStatusToAcceptedForOffender(
    userId: String,
    offenderId: String,
    statusChangeUpdateRequestDTO: StatusChangeUpdateRequestDTO,
    storedProfile: ReadinessProfile,
  ): Profile {
    val profile: Profile = parseProfile(storedProfile.profileData)
    checkDeclinedProfileStatus(profile, offenderId)
    profile.statusChangeDate = timeProvider.now()
    profile.statusChangeType = StatusChange.DECLINED_TO_ACCEPTED

    profile.supportDeclined_history?.add(profile.supportDeclined!!) ?: run {
      profile.supportDeclined_history = mutableListOf()
      profile.supportDeclined_history!!.add(profile.supportDeclined!!)
    }
    profile.supportDeclined = null
    statusChangeUpdateRequestDTO.supportAccepted!!.modifiedBy = userId
    statusChangeUpdateRequestDTO.supportAccepted.modifiedDateTime = timeProvider.now()
    profile.supportAccepted = statusChangeUpdateRequestDTO.supportAccepted
    profile.status = statusChangeUpdateRequestDTO.status

    return profile
  }

  private fun setProfileValues(profileToBeModified: Profile, profileReference: Profile) {
    profileToBeModified.supportDeclined = profileReference.supportDeclined
    profileToBeModified.supportAccepted = profileReference.supportAccepted
    profileToBeModified.supportAccepted_history = profileReference.supportAccepted_history
    profileToBeModified.supportDeclined_history = profileReference.supportDeclined_history
    profileToBeModified.statusChangeDate = timeProvider.now()
    profileToBeModified.statusChange = true
    profileToBeModified.statusChangeType = profileReference.statusChangeType
  }

  private fun changeStatusToDeclinedForOffender(
    userId: String,
    offenderId: String,
    statusChangeUpdateRequestDTO: StatusChangeUpdateRequestDTO,
    storedProfile: ReadinessProfile,
  ): Profile {
    val profile: Profile = parseProfile(storedProfile.profileData)
    profile.statusChangeDate = timeProvider.now()
    profile.statusChangeType = StatusChange.ACCEPTED_TO_DECLINED

    profile.supportAccepted_history?.add(profile.supportAccepted!!) ?: run {
      profile.supportAccepted_history = mutableListOf()
      profile.supportAccepted_history!!.add(profile.supportAccepted!!)
    }
    statusChangeUpdateRequestDTO.supportDeclined!!.modifiedBy = userId
    statusChangeUpdateRequestDTO.supportDeclined.modifiedDateTime = timeProvider.now()
    profile.supportDeclined = statusChangeUpdateRequestDTO.supportDeclined
    profile.status = statusChangeUpdateRequestDTO.status
    checkDeclinedProfileStatus(profile, offenderId)

    return profile
  }

  private fun updateProfileAcceptStatusChange(
    profile: Profile,
    userId: String,
    offenderId: String,
    storedProfile: ReadinessProfile,
  ) {
    val statusChangeUpdateRequestDTO = StatusChangeUpdateRequestDTO(profile.supportAccepted!!, null, profile.status)
    val storedCoreProfile: Profile =
      changeStatusToAcceptedForOffender(userId, offenderId, statusChangeUpdateRequestDTO, storedProfile)
    setProfileValues(profile, storedCoreProfile)
  }

  private fun updateProfileDeclinedStatusChange(
    profile: Profile,
    userId: String,
    offenderId: String,
    storedProfile: ReadinessProfile,
  ) {
    val statusChangeUpdateRequestDTO = StatusChangeUpdateRequestDTO(null, profile.supportDeclined!!, profile.status)
    val storedCoreProfile: Profile =
      changeStatusToDeclinedForOffender(userId, offenderId, statusChangeUpdateRequestDTO, storedProfile)
    setProfileValues(profile, storedCoreProfile)
    checkDeclinedProfileStatus(profile, offenderId)
  }

  private fun updateAcceptedStatusList(profile: Profile, storedCoreProfile: Profile, userId: String) {
    profile.supportAccepted?.modifiedBy = userId
    profile.supportAccepted?.modifiedBy = userId
    profile.supportAccepted?.modifiedDateTime = timeProvider.now()
    profile.supportAccepted_history?.add(storedCoreProfile.supportAccepted!!) ?: run {
      profile.supportAccepted_history = mutableListOf()
      profile.supportAccepted_history!!.add(storedCoreProfile.supportAccepted!!)
    }
  }

  private fun updateDeclinedStatusList(profile: Profile, storedCoreProfile: Profile, userId: String, offenderId: String) {
    profile.supportAccepted?.modifiedBy = userId
    profile.supportDeclined?.modifiedBy = userId
    profile.supportDeclined?.modifiedDateTime = timeProvider.now()
    profile.supportDeclined_history?.add(storedCoreProfile.supportDeclined!!) ?: run {
      profile.supportDeclined_history = mutableListOf()
      profile.supportDeclined_history!!.add(storedCoreProfile.supportDeclined!!)
    }

    checkDeclinedProfileStatus(profile, offenderId)
  }

  private fun Profile.json(): JsonNode = objectMapper.valueToTree(this)
}
