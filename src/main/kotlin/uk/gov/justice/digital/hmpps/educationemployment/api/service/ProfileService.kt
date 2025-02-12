package uk.gov.justice.digital.hmpps.educationemployment.api.service

import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.data.StatusChangeUpdateRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.Note
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.SupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.SupportDeclined
import uk.gov.justice.digital.hmpps.educationemployment.api.entity.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.AlreadyExistsException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.InvalidStateException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.repository.ReadinessProfileRepository
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ProfileService(
  private val readinessProfileRepository: ReadinessProfileRepository,
) {
  fun createProfileForOffender(
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
    profile.supportAccepted_history = mutableListOf<SupportAccepted>()
    profile.supportDeclined_history = mutableListOf<SupportDeclined>()
    return readinessProfileRepository.save(
      ReadinessProfile(
        offenderId,
        bookingId,
        userId,
        LocalDateTime.now(),
        userId,
        LocalDateTime.now(),
        "1.0",
        CapturedSpringConfigValues.objectMapper.readTree(CapturedSpringConfigValues.objectMapper.writeValueAsString(profile)),
        CapturedSpringConfigValues.objectMapper.readTree("[]"),
        true,
      ),
    )
  }

  fun updateProfileForOffender(
    userId: String,
    offenderId: String,
    bookingId: Long,
    profile: Profile,
  ): ReadinessProfile {
    var storedProfile: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    var storedCoreProfile: Profile = CapturedSpringConfigValues.objectMapper.readValue(
      CapturedSpringConfigValues.objectMapper.writeValueAsString(storedProfile.profileData),
      object : TypeReference<Profile>() {},
    )
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
      updateAcceptedStatusList(profile, storedCoreProfile, userId, offenderId)
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

    storedProfile.profileData =
      CapturedSpringConfigValues.objectMapper.readTree(CapturedSpringConfigValues.objectMapper.writeValueAsString(profile))
    storedProfile.modifiedBy = userId
    storedProfile.modifiedDateTime = LocalDateTime.now()
    readinessProfileRepository.save(storedProfile)
    return storedProfile
  }

  fun getProfilesForOffenders(offenders: List<String>) = readinessProfileRepository.findAllById(offenders)

  fun getProfileForOffender(offenderId: String): ReadinessProfile {
    var profile: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    return profile
  }

  fun getProfileForOffenderFilterByPeriod(
    prisonNumber: String,
    fromDate: LocalDate? = null,
    toDate: LocalDate? = null,
  ): ReadinessProfile {
    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
      throw IllegalArgumentException("fromDate cannot be after toDate")
    }

    val readinessProfile = getProfileForOffender(prisonNumber)
    val profile =
      CapturedSpringConfigValues.objectMapper.treeToValue(
        readinessProfile.profileData,
        object : TypeReference<Profile>() {},
      )
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

    readinessProfile.profileData = CapturedSpringConfigValues.objectMapper.valueToTree(profile)
    return readinessProfile
  }

  fun addProfileNoteForOffender(userId: String, offenderId: String, attribute: ActionTodo, text: String): List<Note> {
    var storedProfile: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    var notesList: MutableList<Note> = CapturedSpringConfigValues.objectMapper.readValue(
      CapturedSpringConfigValues.objectMapper.writeValueAsString(storedProfile.notesData),
      object : TypeReference<MutableList<Note>>() {},
    )
    notesList.add(Note(userId, LocalDateTime.now(), attribute, text))
    storedProfile.notesData =
      CapturedSpringConfigValues.objectMapper.readTree(CapturedSpringConfigValues.objectMapper.writeValueAsString(notesList))
    storedProfile.modifiedBy = userId
    readinessProfileRepository.save(storedProfile)
    return notesList.filter { n -> n.attribute == attribute }
  }

  fun getProfileNotesForOffender(offenderId: String, attribute: ActionTodo): List<Note> {
    var storedProfile: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    var notesList: List<Note> = CapturedSpringConfigValues.objectMapper.readValue(
      CapturedSpringConfigValues.objectMapper.writeValueAsString(storedProfile.notesData),
      object : TypeReference<List<Note>>() {},
    )
    return notesList.filter { n -> n.attribute == attribute }
  }

  fun checkDeclinedProfileStatus(profile: Profile, offenderId: String): Boolean {
    if (profile.status.equals(ProfileStatus.SUPPORT_NEEDED)) {
      throw InvalidStateException(offenderId)
    }
    return true
  }

  fun setMiscellaneousAttributesOnSupportState(profile: Profile, userId: String, offenderId: String) {
    if (profile.supportAccepted != null) {
      profile.supportAccepted?.modifiedBy = userId
      profile.supportAccepted?.modifiedDateTime = LocalDateTime.now()
    }

    if (profile.supportDeclined != null) {
      profile.supportDeclined?.modifiedBy = userId
      profile.supportDeclined?.modifiedDateTime = LocalDateTime.now()
      checkDeclinedProfileStatus(profile, offenderId)
    }
  }

  fun changeStatusToAcceptedForOffender(
    userId: String,
    offenderId: String,
    statusChangeUpdateRequestDTO: StatusChangeUpdateRequestDTO,
    storedProfile: ReadinessProfile,
  ): Profile {
    var profile: Profile = CapturedSpringConfigValues.objectMapper.readValue(
      CapturedSpringConfigValues.objectMapper.writeValueAsString(storedProfile.profileData),
      object : TypeReference<Profile>() {},
    )
    checkDeclinedProfileStatus(profile, offenderId)
    profile.statusChangeDate = LocalDateTime.now()
    profile.statusChangeType = StatusChange.DECLINED_TO_ACCEPTED

    profile.supportDeclined_history?.let { it.add(profile.supportDeclined!!) } ?: run {
      profile.supportDeclined_history = mutableListOf<SupportDeclined>()
      profile.supportDeclined_history!!.add(profile.supportDeclined!!)
    }
    profile.supportDeclined = null
    statusChangeUpdateRequestDTO.supportAccepted!!.modifiedBy = userId
    statusChangeUpdateRequestDTO.supportAccepted.modifiedDateTime = LocalDateTime.now()
    profile.supportAccepted = statusChangeUpdateRequestDTO.supportAccepted
    profile.status = statusChangeUpdateRequestDTO.status

    return profile
  }

  fun setProfileValues(profileToBeModified: Profile, profileReference: Profile) {
    profileToBeModified.supportDeclined = profileReference.supportDeclined
    profileToBeModified.supportAccepted = profileReference.supportAccepted
    profileToBeModified.supportAccepted_history = profileReference.supportAccepted_history
    profileToBeModified.supportDeclined_history = profileReference.supportDeclined_history
    profileToBeModified.statusChangeDate = LocalDateTime.now()
    profileToBeModified.statusChange = true
    profileToBeModified.statusChangeType = profileReference.statusChangeType
  }
  fun changeStatusForOffender(
    userId: String,
    offenderId: String,
    statusChangeUpdateRequestDTO: StatusChangeUpdateRequestDTO?,
  ): ReadinessProfile {
    var storedProfile: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    var storedCoreProfile: Profile? = null
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
      storedCoreProfile = CapturedSpringConfigValues.objectMapper.readValue(
        CapturedSpringConfigValues.objectMapper.writeValueAsString(storedProfile.profileData),
        object : TypeReference<Profile>() {},
      )
      storedCoreProfile!!.status = statusChangeUpdateRequestDTO!!.status
    } else {
      throw InvalidStateException(offenderId)
    }

    storedCoreProfile.statusChangeDate = LocalDateTime.now()
    storedCoreProfile.statusChange = true
    storedProfile.profileData =
      CapturedSpringConfigValues.objectMapper.readTree(CapturedSpringConfigValues.objectMapper.writeValueAsString(storedCoreProfile))
    storedProfile.modifiedBy = userId
    storedProfile.modifiedDateTime = LocalDateTime.now()
    readinessProfileRepository.save(storedProfile)
    return storedProfile
  }
  fun changeStatusToDeclinedForOffender(
    userId: String,
    offenderId: String,
    statusChangeUpdateRequestDTO: StatusChangeUpdateRequestDTO,
    storedProfile: ReadinessProfile,
  ): Profile {
    var profile: Profile = CapturedSpringConfigValues.objectMapper.readValue(
      CapturedSpringConfigValues.objectMapper.writeValueAsString(storedProfile.profileData),
      object : TypeReference<Profile>() {},
    )
    profile.statusChangeDate = LocalDateTime.now()
    profile.statusChangeType = StatusChange.ACCEPTED_TO_DECLINED

    profile.supportAccepted_history?.let { it.add(profile.supportAccepted!!) } ?: run {
      profile.supportAccepted_history = mutableListOf<SupportAccepted>()
      profile.supportAccepted_history!!.add(profile.supportAccepted!!)
    }
    // profile.supportAccepted = null
    statusChangeUpdateRequestDTO.supportDeclined!!.modifiedBy = userId
    statusChangeUpdateRequestDTO.supportDeclined.modifiedDateTime = LocalDateTime.now()
    profile.supportDeclined = statusChangeUpdateRequestDTO.supportDeclined
    profile.status = statusChangeUpdateRequestDTO.status
    checkDeclinedProfileStatus(profile, offenderId)

    return profile
  }

  fun updateProfileAcceptStatusChange(
    profile: Profile,
    userId: String,
    offenderId: String,
    storedProfile: ReadinessProfile,
  ) {
    val statusChangeUpdateRequestDTO: StatusChangeUpdateRequestDTO =
      StatusChangeUpdateRequestDTO(profile.supportAccepted!!, null, profile.status)
    val storedCoreProfile: Profile =
      changeStatusToAcceptedForOffender(userId, offenderId, statusChangeUpdateRequestDTO, storedProfile)
    setProfileValues(profile, storedCoreProfile)
  }

  fun updateProfileDeclinedStatusChange(
    profile: Profile,
    userId: String,
    offenderId: String,
    storedProfile: ReadinessProfile,
  ) {
    val statusChangeUpdateRequestDTO: StatusChangeUpdateRequestDTO =
      StatusChangeUpdateRequestDTO(null, profile.supportDeclined!!, profile.status)
    val storedCoreProfile: Profile =
      changeStatusToDeclinedForOffender(userId, offenderId, statusChangeUpdateRequestDTO, storedProfile)
    setProfileValues(profile, storedCoreProfile)
    checkDeclinedProfileStatus(profile, offenderId)
  }

  fun updateAcceptedStatusList(profile: Profile, storedCoreProfile: Profile, userId: String, offenderId: String) {
    profile.supportAccepted?.modifiedBy = userId
    profile.supportAccepted?.modifiedBy = userId
    profile.supportAccepted?.modifiedDateTime = LocalDateTime.now()
    profile.supportAccepted_history?.let { it.add(storedCoreProfile.supportAccepted!!) } ?: run {
      profile.supportAccepted_history = mutableListOf<SupportAccepted>()
      profile.supportAccepted_history!!.add(storedCoreProfile.supportAccepted!!)
    }
  }

  fun updateDeclinedStatusList(profile: Profile, storedCoreProfile: Profile, userId: String, offenderId: String) {
    profile.supportAccepted?.modifiedBy = userId
    profile.supportDeclined?.modifiedBy = userId
    profile.supportDeclined?.modifiedDateTime = LocalDateTime.now()
    profile.supportDeclined_history?.let { it.add(storedCoreProfile.supportDeclined!!) } ?: run {
      profile.supportDeclined_history = mutableListOf<SupportDeclined>()
      profile.supportDeclined_history!!.add(storedCoreProfile.supportDeclined!!)
    }

    checkDeclinedProfileStatus(profile, offenderId)
  }
}
