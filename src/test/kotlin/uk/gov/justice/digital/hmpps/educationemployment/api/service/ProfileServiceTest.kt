package uk.gov.justice.digital.hmpps.educationemployment.api.service

import com.fasterxml.jackson.core.type.TypeReference
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.entity.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.AlreadyExistsException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.InvalidStateException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.repository.ReadinessProfileRepository
import java.util.Optional

class ProfileServiceTest {
  private val readinessProfileRepository: ReadinessProfileRepository = mock()
  private lateinit var profileService: ProfileService

  @BeforeEach
  fun beforeEach() {
    profileService = ProfileService(readinessProfileRepository)
  }

  @Test
  fun `makes a call to the repository to save the readiness profile`() {
    whenever(readinessProfileRepository.save(any())).thenReturn(TestData.readinessProfile)
    whenever(readinessProfileRepository.existsById(any())).thenReturn(TestData.booleanFalse)

    val rProfile = profileService.createProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile)
    val argumentCaptor = ArgumentCaptor.forClass(ReadinessProfile::class.java)
    verify(readinessProfileRepository).save(argumentCaptor.capture())
    assertThat(rProfile).extracting(TestData.createdByString, TestData.offenderIdString, TestData.bookingIdString)
      .contains(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId)
  }

  @Test
  fun `makes a call to the repository to update the readiness profile`() {
    whenever(readinessProfileRepository.save(any())).thenReturn(TestData.updatedReadinessProfile)
    whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(TestData.updatedReadinessProfile))

    val rProfile = profileService.updateProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile)
    val argumentCaptor = ArgumentCaptor.forClass(ReadinessProfile::class.java)
    verify(readinessProfileRepository).save(argumentCaptor.capture())
    assertThat(rProfile).extracting(TestData.createdByString, TestData.offenderIdString, TestData.bookingIdString)
      .contains(TestData.updatedBy, TestData.newOffenderId, TestData.updatedBookingId)
  }

  @Test
  fun `makes a call to the repository to update the readiness profile Prison location`() {
    whenever(readinessProfileRepository.save(any())).thenReturn(TestData.updatedReadinessProfile)
    whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(TestData.updatedReadinessProfile))

    val rProfile = profileService.updateProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile)
    val argumentCaptor = ArgumentCaptor.forClass(ReadinessProfile::class.java)
    verify(readinessProfileRepository).save(argumentCaptor.capture())
    assertThat(rProfile).extracting(TestData.createdByString, TestData.offenderIdString, TestData.bookingIdString)
      .contains(TestData.updatedBy, TestData.newOffenderId, TestData.updatedBookingId)
    var storedCoreProfile: Profile = CapturedSpringConfigValues.objectMapper.readValue(
      CapturedSpringConfigValues.objectMapper.writeValueAsString(rProfile.profileData),
      object : TypeReference<Profile>() {},
    )
    assertThat(storedCoreProfile.prisonName)
      .isEqualTo(TestData.profile.prisonName)
  }

  @Test
  fun `makes a call to the repository to add supportDeclined to empty supportDeclined List on Update of declined State in readiness profile`() {
    whenever(readinessProfileRepository.save(any())).thenReturn(TestData.updatedReadinessProfile_declined_1)
    whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(TestData.readinessProfile_declined_1))

    val rProfile = profileService.updateProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile_declinedModified)
    val argumentCaptor = ArgumentCaptor.forClass(ReadinessProfile::class.java)
    verify(readinessProfileRepository).save(argumentCaptor.capture())
    var storedCoreProfile: Profile = CapturedSpringConfigValues.objectMapper.readValue(
      CapturedSpringConfigValues.objectMapper.writeValueAsString(rProfile.profileData),
      object : TypeReference<Profile>() {},
    )
    assertThat(storedCoreProfile.supportDeclined_history!!.size == 1)
  }

  @Test
  fun `makes a call to the repository to add supportDeclined to empty supportDeclined List on Update of acceptedSupport  in readiness profile`() {
    whenever(readinessProfileRepository.save(any())).thenReturn(TestData.updatedReadinessProfile_accpeted_1)
    whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(TestData.readinessProfile_declined_1))

    val rProfile = profileService.updateProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile_accpeted_modified)
    val argumentCaptor = ArgumentCaptor.forClass(ReadinessProfile::class.java)
    verify(readinessProfileRepository).save(argumentCaptor.capture())
    var storedCoreProfile: Profile = CapturedSpringConfigValues.objectMapper.readValue(
      CapturedSpringConfigValues.objectMapper.writeValueAsString(rProfile.profileData),
      object : TypeReference<Profile>() {},
    )
    assertThat(storedCoreProfile.supportDeclined_history!!.size == 1)
    assertThat(storedCoreProfile.statusChangeType!!.equals(StatusChange.DECLINED_TO_ACCEPTED))
  }

  @Test
  fun `makes a call to the repository to add supportAccepted to empty supportAccepted List on Update of declinedSupport in readiness profile`() {
    whenever(readinessProfileRepository.save(any())).thenReturn(TestData.updatedReadinessProfile_declined_1)
    whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(TestData.readinessProfile_accepted_1))

    val rProfile = profileService.updateProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile_accpeted_modified)
    val argumentCaptor = ArgumentCaptor.forClass(ReadinessProfile::class.java)
    verify(readinessProfileRepository).save(argumentCaptor.capture())
    var storedCoreProfile: Profile = CapturedSpringConfigValues.objectMapper.readValue(
      CapturedSpringConfigValues.objectMapper.writeValueAsString(rProfile.profileData),
      object : TypeReference<Profile>() {},
    )
    assertThat(storedCoreProfile.supportAccepted_history!!.size == 1)
    assertThat(storedCoreProfile.statusChangeType!!.equals(StatusChange.ACCEPTED_TO_DECLINED))
  }

  @Test
  fun `makes a call to the repository to add support accepted to empty supportAccpetedList on Update of declined in readiness profile`() {
    whenever(readinessProfileRepository.save(any())).thenReturn(TestData.updatedReadinessProfile_accpeted_1)
    whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(TestData.readinessProfile_accepted_1))

    val rProfile = profileService.updateProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile_accpeted_modified)
    val argumentCaptor = ArgumentCaptor.forClass(ReadinessProfile::class.java)
    verify(readinessProfileRepository).save(argumentCaptor.capture())
    var storedCoreProfile: Profile = CapturedSpringConfigValues.objectMapper.readValue(
      CapturedSpringConfigValues.objectMapper.writeValueAsString(rProfile.profileData),
      object : TypeReference<Profile>() {},
    )
    assertThat(storedCoreProfile.supportAccepted_history!!.size == 1)
  }

  @Test
  fun `makes a call to the repository to add supportDeclined to supportDeclineList on Update of declinedSupport in readiness profile`() {
    whenever(readinessProfileRepository.save(any())).thenReturn(TestData.updatedReadinessProfile_declined_1)
    whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(TestData.readinessProfile_declined_1_declined_list))

    val rProfile = profileService.updateProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile_declinedModified)
    val argumentCaptor = ArgumentCaptor.forClass(ReadinessProfile::class.java)
    verify(readinessProfileRepository).save(argumentCaptor.capture())
    var storedCoreProfile: Profile = CapturedSpringConfigValues.objectMapper.readValue(
      CapturedSpringConfigValues.objectMapper.writeValueAsString(rProfile.profileData),
      object : TypeReference<Profile>() {},
    )
    assertThat(storedCoreProfile.supportDeclined_history!!.size == 2)
  }

  @Test
  fun `makes a call to the repository to verify support declined is not added to supportDeclineList when declinedsupport has not chnaged in readiness profile`() {
    whenever(readinessProfileRepository.save(any())).thenReturn(TestData.readinessProfile_declined_1)
    whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(TestData.readinessProfile_declined_1))

    val rProfile = profileService.updateProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile_declinedModified)
    val argumentCaptor = ArgumentCaptor.forClass(ReadinessProfile::class.java)
    verify(readinessProfileRepository).save(argumentCaptor.capture())
    var storedCoreProfile: Profile = CapturedSpringConfigValues.objectMapper.readValue(
      CapturedSpringConfigValues.objectMapper.writeValueAsString(rProfile.profileData),
      object : TypeReference<Profile>() {},
    )
    assertThat(storedCoreProfile.supportDeclined_history!!.size == 0)
  }

  @Test
  fun `makes a call to the repository to save a readiness profile note`() {
    whenever(readinessProfileRepository.save(any())).thenReturn(TestData.updatedReadinessProfile)
    whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(TestData.updatedReadinessProfile))

    val listNote = profileService.addProfileNoteForOffender(TestData.createdBy, TestData.newOffenderId, TestData.actionToDoCV, TestData.noteString)
    val argumentCaptor = ArgumentCaptor.forClass(ReadinessProfile::class.java)
    verify(readinessProfileRepository).save(argumentCaptor.capture())
    assert(listNote[0].text.equals(TestData.noteString))
  }

  @Test
  fun `makes a call to the repository to retreive a readiness profile note`() {
    whenever(readinessProfileRepository.save(any())).thenReturn(TestData.updatedReadinessProfileNotes)
    whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(TestData.updatedReadinessProfileNotes))

    val listNote = profileService.getProfileNotesForOffender(TestData.newOffenderId, TestData.actionToDoCV)
    assert(listNote[0].text.equals(TestData.noteString))
  }

  @Test
  fun `makes a call to the repository to retreive a readiness profile for an offender id`() {
    whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(TestData.updatedReadinessProfileNotes))
    val profile = profileService.getProfileForOffender(TestData.newOffenderId)
    assert(profile.equals(TestData.updatedReadinessProfileNotes))
  }

  @Test
  fun `makes a call to the repository to retreive a list ofreadiness profiles for list offender ids`() {
    whenever(readinessProfileRepository.findAllById(any())).thenReturn(TestData.profileList)
    val profileList = profileService.getProfilesForOffenders(TestData.offenderIdList)
    assert(profileList.containsAll(TestData.profileList))
  }

  @Test
  fun `throws an exception when a call is made to the repository to save the readiness profile`() {
    assertThatExceptionOfType(AlreadyExistsException::class.java).isThrownBy {
      whenever(readinessProfileRepository.save(any())).thenReturn(TestData.readinessProfile)
      whenever(readinessProfileRepository.existsById(any())).thenReturn(TestData.booleanTrue)

      profileService.createProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile)
    }.withMessageContaining("Readiness profile already exists for offender")
  }

  @Test
  fun `throws an exception when a call is made to the repository to save the readiness profile with incorrect status`() {
    assertThatExceptionOfType(InvalidStateException::class.java).isThrownBy {
      whenever(readinessProfileRepository.save(any())).thenReturn(TestData.readinessProfile)
      whenever(readinessProfileRepository.existsById(any())).thenReturn(TestData.booleanFalse)

      profileService.createProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile_IncorrectStatus)
    }.withMessageContaining("Readiness profile is in an invalid state for")
  }

  @Test
  fun `throws an exception when a call is made to the repository to save the readiness profile with both support state`() {
    assertThatExceptionOfType(InvalidStateException::class.java).isThrownBy {
      whenever(readinessProfileRepository.save(any())).thenReturn(TestData.readinessProfile)
      whenever(readinessProfileRepository.existsById(any())).thenReturn(TestData.booleanFalse)

      profileService.createProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile_NEW_BOTHSTATE_INCOORECT)
    }.withMessageContaining("Readiness profile is in an invalid state for")
  }

  @Test
  fun `throws an exception when a call is made to the repository to update the readiness profile`() {
    assertThatExceptionOfType(NotFoundException::class.java).isThrownBy {
      whenever(readinessProfileRepository.save(any())).thenReturn(TestData.readinessProfile)
      whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.empty())
      profileService.updateProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile)
    }.withMessageContaining("Readiness profile does not exist for offender")
  }

  @Test
  fun `throws an exception when a call is made to the repository to add a note to an non existing readiness profile`() {
    assertThatExceptionOfType(NotFoundException::class.java).isThrownBy {
      whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.empty())
      profileService.addProfileNoteForOffender(TestData.createdBy, TestData.newOffenderId, ActionTodo.BANK_ACCOUNT, TestData.newNotes)
    }.withMessageContaining("Readiness profile does not exist for offender")
  }

  @Test
  fun `throws an exception when a call is made to the repository to retreive a note from a non existing readiness profile`() {
    assertThatExceptionOfType(NotFoundException::class.java).isThrownBy {
      whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.empty())
      profileService.getProfileNotesForOffender(TestData.createdBy, ActionTodo.BANK_ACCOUNT)
    }.withMessageContaining("Readiness profile does not exist for offender")
  }
}
