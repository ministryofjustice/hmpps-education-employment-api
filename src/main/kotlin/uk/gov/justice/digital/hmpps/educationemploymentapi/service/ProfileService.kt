package uk.gov.justice.digital.hmpps.educationemploymentapi.service

import com.fasterxml.jackson.core.type.TypeReference
import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemploymentapi.config.CapturedSpringMapperConfiguration
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.StatusChangeUpdateRequestDTO
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.ActionTodo
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Note
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Profile
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.StatusChange
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.SupportAccepted
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.SupportDeclined
import uk.gov.justice.digital.hmpps.educationemploymentapi.entity.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemploymentapi.exceptions.AlreadyExistsException
import uk.gov.justice.digital.hmpps.educationemploymentapi.exceptions.InvalidStateException
import uk.gov.justice.digital.hmpps.educationemploymentapi.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemploymentapi.repository.ReadinessProfileRepository
import java.time.LocalDateTime

@Service
class ProfileService(
  private val readinessProfileRepository: ReadinessProfileRepository
) {
  fun createProfileForOffender(
    userId: String,
    offenderId: String,
    bookingId: Long,
    profile: Profile
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
        JacksonUtil.toJsonNode(CapturedSpringMapperConfiguration.OBJECT_MAPPER.writeValueAsString(profile)),
        JacksonUtil.toJsonNode("[]"),
        true
      )
    )
  }

  fun updateProfileForOffender(
    userId: String,
    offenderId: String,
    bookingId: Long,
    profile: Profile
  ): ReadinessProfile {
    var storedProfile: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    var storedCoreProfile: Profile = CapturedSpringMapperConfiguration.OBJECT_MAPPER.readValue(
      JacksonUtil.toString(storedProfile.profileData), object : TypeReference<Profile>() {}
    )
    profile.supportAccepted_history = storedCoreProfile.supportAccepted_history
    profile.supportDeclined_history = storedCoreProfile.supportDeclined_history
    if (storedCoreProfile.supportAccepted == null && storedCoreProfile.supportDeclined == null && profile.supportAccepted != null && profile.supportDeclined != null) {
      throw InvalidStateException(offenderId)
    } else if (storedCoreProfile.supportAccepted != null && profile.supportAccepted != null && !profile.supportAccepted?.equals(
        storedCoreProfile.supportAccepted
      )!!
    ) {
      updateAcceptedStatusList(profile, storedCoreProfile, userId, offenderId)
    } else if (storedCoreProfile.supportDeclined != null && profile.supportDeclined != null && !profile.supportDeclined?.equals(
        storedCoreProfile.supportDeclined
      )!!
    ) {
      updateDeclinedStatusList(profile, storedCoreProfile, userId, offenderId)
    } else if (storedCoreProfile.supportAccepted != null && profile.supportDeclined != null) {
      updateProfileDeclinedStatusChange(profile, userId, offenderId, storedProfile)
    } else if (storedCoreProfile.supportDeclined != null && profile.supportAccepted != null) {
      updateProfileAcceptStatusChange(profile, userId, offenderId, storedProfile)
    }

    storedProfile.profileData =
      JacksonUtil.toJsonNode(CapturedSpringMapperConfiguration.OBJECT_MAPPER.writeValueAsString(profile))
    storedProfile.modifiedBy = userId
    storedProfile.modifiedDateTime = LocalDateTime.now()
    readinessProfileRepository.save(storedProfile)
    return storedProfile
  }

  fun getProfilesForOffenders(offenders: List<String>) =
    readinessProfileRepository.findAllById(offenders)

  fun getProfileForOffender(offenderId: String): ReadinessProfile {
    var profile: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    return profile
  }

  fun addProfileNoteForOffender(userId: String, offenderId: String, attribute: ActionTodo, text: String): List<Note> {
    var storedProfile: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    var notesList: MutableList<Note> = CapturedSpringMapperConfiguration.OBJECT_MAPPER.readValue(
      JacksonUtil.toString(storedProfile.notesData), object : TypeReference<MutableList<Note>>() {}
    )
    notesList.add(Note(userId, LocalDateTime.now(), attribute, text))
    storedProfile.notesData =
      JacksonUtil.toJsonNode(CapturedSpringMapperConfiguration.OBJECT_MAPPER.writeValueAsString(notesList))
    storedProfile.modifiedBy = userId
    readinessProfileRepository.save(storedProfile)
    return notesList.filter { n -> n.attribute == attribute }
  }

  fun getProfileNotesForOffender(offenderId: String, attribute: ActionTodo): List<Note> {
    var storedProfile: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    var notesList: List<Note> = CapturedSpringMapperConfiguration.OBJECT_MAPPER.readValue(
      JacksonUtil.toString(storedProfile.notesData), object : TypeReference<List<Note>>() {}
    )
    return notesList.filter { n -> n.attribute == attribute }
  }

  fun checkAcceptedProfileStatus(profile: Profile, offenderId: String): Boolean {
    /*if (profile.status.equals(ProfileStatus.NO_RIGHT_TO_WORK) || profile.status.equals(ProfileStatus.SUPPORT_DECLINED)) {
      throw InvalidStateException(offenderId)
    }*/
    return true
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
      checkAcceptedProfileStatus(profile, offenderId)
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
    storedProfile: ReadinessProfile
  ): Profile {
    var profile: Profile = CapturedSpringMapperConfiguration.OBJECT_MAPPER.readValue(
      JacksonUtil.toString(storedProfile.profileData), object : TypeReference<Profile>() {}
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
    checkAcceptedProfileStatus(profile, offenderId)

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
    statusChangeUpdateRequestDTO: StatusChangeUpdateRequestDTO?
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
      checkAcceptedProfileStatus(storedCoreProfile, offenderId)
    } else if (statusChangeUpdateRequestDTO!!.status.equals(ProfileStatus.READY_TO_WORK) || statusChangeUpdateRequestDTO.status.equals(ProfileStatus.NO_RIGHT_TO_WORK)) {
      storedCoreProfile = CapturedSpringMapperConfiguration.OBJECT_MAPPER.readValue(
        JacksonUtil.toString(storedProfile.profileData), object : TypeReference<Profile>() {}
      )
      storedCoreProfile!!.status = statusChangeUpdateRequestDTO!!.status
    } else {
      throw InvalidStateException(offenderId)
    }

    storedCoreProfile.statusChangeDate = LocalDateTime.now()
    storedCoreProfile.statusChange = true
    storedProfile.profileData =
      JacksonUtil.toJsonNode(CapturedSpringMapperConfiguration.OBJECT_MAPPER.writeValueAsString(storedCoreProfile))
    storedProfile.modifiedBy = userId
    storedProfile.modifiedDateTime = LocalDateTime.now()
    readinessProfileRepository.save(storedProfile)
    return storedProfile
  }
  fun changeStatusToDeclinedForOffender(
    userId: String,
    offenderId: String,
    statusChangeUpdateRequestDTO: StatusChangeUpdateRequestDTO,
    storedProfile: ReadinessProfile
  ): Profile {
    var profile: Profile = CapturedSpringMapperConfiguration.OBJECT_MAPPER.readValue(
      JacksonUtil.toString(storedProfile.profileData), object : TypeReference<Profile>() {}
    )
    checkAcceptedProfileStatus(profile, offenderId)
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
    storedProfile: ReadinessProfile
  ) {
    val statusChangeUpdateRequestDTO: StatusChangeUpdateRequestDTO =
      StatusChangeUpdateRequestDTO(profile.supportAccepted!!, null, profile.status)
    val storedCoreProfile: Profile =
      changeStatusToAcceptedForOffender(userId, offenderId, statusChangeUpdateRequestDTO, storedProfile)
    setProfileValues(profile, storedCoreProfile)
    checkAcceptedProfileStatus(profile, offenderId)
  }

  fun updateProfileDeclinedStatusChange(
    profile: Profile,
    userId: String,
    offenderId: String,
    storedProfile: ReadinessProfile
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
    checkAcceptedProfileStatus(profile, offenderId)
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
