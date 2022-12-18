package uk.gov.justice.digital.hmpps.educationemploymentapi.service

import com.fasterxml.jackson.core.type.TypeReference
import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemploymentapi.config.CapturedSpringMapperConfiguration
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.AcceptStatusUpdateRequestDTO
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.DeclinedStatusUpdateRequestDTO
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
    if (profile.currentSupportState.supportAccepted != null && profile.currentSupportState.supportDeclined != null) {
      throw InvalidStateException(offenderId)
    }
    setMiscellaneousAttributesOnSupportState(profile, userId, offenderId)
    profile.statusChange = false
    profile.statusChangeType = StatusChange.NEW
    profile.supportAccepted = mutableListOf<SupportAccepted>()
    profile.supportDeclined = mutableListOf<SupportDeclined>()
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
    profile.supportAccepted = storedCoreProfile.supportAccepted
    profile.supportDeclined = storedCoreProfile.supportDeclined
    if (storedCoreProfile.currentSupportState.supportAccepted == null && storedCoreProfile.currentSupportState.supportDeclined == null && profile.currentSupportState.supportAccepted != null && profile.currentSupportState.supportDeclined != null) {
      throw InvalidStateException(offenderId)
    } else if (storedCoreProfile.currentSupportState.supportAccepted != null && profile.currentSupportState.supportAccepted != null && !profile.currentSupportState.supportAccepted?.equals(
        storedCoreProfile.currentSupportState.supportAccepted
      )!!
    ) {
      updateAcceptedStatusList(profile, storedCoreProfile, userId, offenderId)
    } else if (storedCoreProfile.currentSupportState.supportDeclined != null && profile.currentSupportState.supportDeclined != null && !profile.currentSupportState.supportDeclined?.equals(
        storedCoreProfile.currentSupportState.supportDeclined
      )!!
    ) {
      updateDeclinedStatusList(profile, storedCoreProfile, userId, offenderId)
    } else if (storedCoreProfile.currentSupportState.supportAccepted != null && profile.currentSupportState.supportDeclined != null) {
      updateProfileDeclinedStatusChange(profile, userId, offenderId, storedProfile)
    } else if (storedCoreProfile.currentSupportState.supportDeclined != null && profile.currentSupportState.supportAccepted != null) {
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
    if (profile.status.equals(ProfileStatus.NO_RIGHT_TO_WORK) || profile.status.equals(ProfileStatus.SUPPORT_DECLINED)) {
      throw InvalidStateException(offenderId)
    }
    return true
  }

  fun checkDeclinedProfileStatus(profile: Profile, offenderId: String): Boolean {
    if (profile.status.equals(ProfileStatus.READY_TO_WORK) || profile.status.equals(ProfileStatus.SUPPORT_NEEDED)) {
      throw InvalidStateException(offenderId)
    }
    return true
  }

  fun setMiscellaneousAttributesOnSupportState(profile: Profile, userId: String, offenderId: String) {
    if (profile.currentSupportState.supportAccepted != null) {
      profile.currentSupportState.supportAccepted?.modifiedBy = userId
      profile.currentSupportState.supportAccepted?.modifiedDateTime = LocalDateTime.now()
      checkAcceptedProfileStatus(profile, offenderId)
    }

    if (profile.currentSupportState.supportDeclined != null) {
      profile.currentSupportState.supportDeclined?.modifiedBy = userId
      profile.currentSupportState.supportDeclined?.modifiedDateTime = LocalDateTime.now()
      checkDeclinedProfileStatus(profile, offenderId)
    }
  }

  fun changeStatusToAcceptedForOffender(
    userId: String,
    offenderId: String,
    acceptStatusUpdateRequestDTO: AcceptStatusUpdateRequestDTO,
    storedProfile: ReadinessProfile
  ): Profile {
    var profile: Profile = CapturedSpringMapperConfiguration.OBJECT_MAPPER.readValue(
      JacksonUtil.toString(storedProfile.profileData), object : TypeReference<Profile>() {}
    )
    checkDeclinedProfileStatus(profile, offenderId)
    profile.statusChangeDate = LocalDateTime.now()
    profile.statusChangeType = StatusChange.DECLINED_TO_ACCEPTED

    profile.supportDeclined?.let { it.add(profile.currentSupportState.supportDeclined!!) } ?: run {
      profile.supportDeclined = mutableListOf<SupportDeclined>()
      profile.supportDeclined!!.add(profile.currentSupportState.supportDeclined!!)
    }
    profile.currentSupportState.supportDeclined = null
    acceptStatusUpdateRequestDTO.supportAccepted.modifiedBy = userId
    acceptStatusUpdateRequestDTO.supportAccepted.modifiedDateTime = LocalDateTime.now()
    profile.currentSupportState.supportAccepted = acceptStatusUpdateRequestDTO.supportAccepted
    profile.status = acceptStatusUpdateRequestDTO.status
    checkAcceptedProfileStatus(profile, offenderId)

    return profile
  }

  fun setProfileValues(profileToBeModified: Profile, profileReference: Profile) {
    profileToBeModified.currentSupportState = profileReference.currentSupportState
    profileToBeModified.supportAccepted = profileReference.supportAccepted
    profileToBeModified.supportDeclined = profileReference.supportDeclined
    profileToBeModified.statusChangeDate = LocalDateTime.now()
    profileToBeModified.statusChange = true
    profileToBeModified.statusChangeType = profileReference.statusChangeType
  }

  fun changeStatusToDeclinedForOffender(
    userId: String,
    offenderId: String,
    declinedStatusUpdateRequestDTO: DeclinedStatusUpdateRequestDTO,
    storedProfile: ReadinessProfile
  ): Profile {
    var profile: Profile = CapturedSpringMapperConfiguration.OBJECT_MAPPER.readValue(
      JacksonUtil.toString(storedProfile.profileData), object : TypeReference<Profile>() {}
    )
    checkAcceptedProfileStatus(profile, offenderId)
    profile.statusChangeDate = LocalDateTime.now()
    profile.statusChangeType = StatusChange.ACCEPTED_TO_DECLINED

    profile.supportAccepted?.let { it.add(profile.currentSupportState.supportAccepted!!) } ?: run {
      profile.supportAccepted = mutableListOf<SupportAccepted>()
      profile.supportAccepted!!.add(profile.currentSupportState.supportAccepted!!)
    }
    profile.currentSupportState.supportAccepted = null
    declinedStatusUpdateRequestDTO.supportDeclined.modifiedBy = userId
    declinedStatusUpdateRequestDTO.supportDeclined.modifiedDateTime = LocalDateTime.now()
    profile.currentSupportState.supportDeclined = declinedStatusUpdateRequestDTO.supportDeclined
    profile.status = declinedStatusUpdateRequestDTO.status
    checkDeclinedProfileStatus(profile, offenderId)

    return profile
  }

  fun updateProfileAcceptStatusChange(
    profile: Profile,
    userId: String,
    offenderId: String,
    storedProfile: ReadinessProfile
  ) {
    val acceptStatusUpdateRequestDTO: AcceptStatusUpdateRequestDTO =
      AcceptStatusUpdateRequestDTO(profile.currentSupportState.supportAccepted!!, profile.status)
    val storedCoreProfile: Profile =
      changeStatusToAcceptedForOffender(userId, offenderId, acceptStatusUpdateRequestDTO, storedProfile)
    setProfileValues(profile, storedCoreProfile)
    checkAcceptedProfileStatus(profile, offenderId)
  }

  fun updateProfileDeclinedStatusChange(
    profile: Profile,
    userId: String,
    offenderId: String,
    storedProfile: ReadinessProfile
  ) {
    val declinedStatusUpdateRequestDTO: DeclinedStatusUpdateRequestDTO =
      DeclinedStatusUpdateRequestDTO(profile.currentSupportState.supportDeclined!!, profile.status)
    val storedCoreProfile: Profile =
      changeStatusToDeclinedForOffender(userId, offenderId, declinedStatusUpdateRequestDTO, storedProfile)
    setProfileValues(profile, storedCoreProfile)
    checkDeclinedProfileStatus(profile, offenderId)
  }

  fun updateAcceptedStatusList(profile: Profile, storedCoreProfile: Profile, userId: String, offenderId: String) {
    profile.currentSupportState.supportAccepted?.modifiedBy = userId
    profile.currentSupportState.supportAccepted?.modifiedBy = userId
    profile.currentSupportState.supportAccepted?.modifiedDateTime = LocalDateTime.now()
    profile.supportAccepted?.let { it.add(storedCoreProfile.currentSupportState.supportAccepted!!) } ?: run {
      profile.supportAccepted = mutableListOf<SupportAccepted>()
      profile.supportAccepted!!.add(storedCoreProfile.currentSupportState.supportAccepted!!)
    }
    checkAcceptedProfileStatus(profile, offenderId)
  }

  fun updateDeclinedStatusList(profile: Profile, storedCoreProfile: Profile, userId: String, offenderId: String) {
    profile.currentSupportState.supportAccepted?.modifiedBy = userId
    profile.currentSupportState.supportDeclined?.modifiedBy = userId
    profile.currentSupportState.supportDeclined?.modifiedDateTime = LocalDateTime.now()
    profile.supportDeclined?.let { it.add(storedCoreProfile.currentSupportState.supportDeclined!!) } ?: run {
      profile.supportDeclined = mutableListOf<SupportDeclined>()
      profile.supportDeclined!!.add(storedCoreProfile.currentSupportState.supportDeclined!!)
    }

    checkDeclinedProfileStatus(profile, offenderId)
  }
}
