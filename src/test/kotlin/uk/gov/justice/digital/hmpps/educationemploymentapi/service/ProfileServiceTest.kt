package uk.gov.justice.digital.hmpps.educationemploymentapi.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.ActionTodo
import uk.gov.justice.digital.hmpps.educationemploymentapi.entity.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemploymentapi.exceptions.AlreadyExistsException
import uk.gov.justice.digital.hmpps.educationemploymentapi.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemploymentapi.repository.ReadinessProfileRepository
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
    whenever(readinessProfileRepository.save(any())).thenReturn(TestData.updatedReadinessProfile.get())
    whenever(readinessProfileRepository.findById(any())).thenReturn(TestData.updatedReadinessProfile)

    val rProfile = profileService.updateProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile)
    val argumentCaptor = ArgumentCaptor.forClass(ReadinessProfile::class.java)
    verify(readinessProfileRepository).save(argumentCaptor.capture())
    assertThat(rProfile).extracting(TestData.createdByString, TestData.offenderIdString, TestData.bookingIdString)
      .contains(TestData.updatedBy, TestData.newOffenderId, TestData.updatedBookingId)
  }

  @Test
  fun `makes a call to the repository to save a readiness profile note`() {
    whenever(readinessProfileRepository.save(any())).thenReturn(TestData.updatedReadinessProfile.get())
    whenever(readinessProfileRepository.findById(any())).thenReturn(TestData.updatedReadinessProfile)

    val listNote = profileService.addProfileNoteForOffender(TestData.createdBy, TestData.newOffenderId, TestData.actionToDoCV, TestData.noteString)
    val argumentCaptor = ArgumentCaptor.forClass(ReadinessProfile::class.java)
    verify(readinessProfileRepository).save(argumentCaptor.capture())
    assert(listNote[0].text.equals(TestData.noteString))
  }

  @Test
  fun `makes a call to the repository to retreive a readiness profile note`() {
    whenever(readinessProfileRepository.save(any())).thenReturn(TestData.updatedReadinessProfileNotes.get())
    whenever(readinessProfileRepository.findById(any())).thenReturn(TestData.updatedReadinessProfileNotes)

    val listNote = profileService.getProfileNotesForOffender(TestData.newOffenderId, TestData.actionToDoCV)
    assert(listNote[0].text.equals(TestData.noteString))
  }

  @Test
  fun `makes a call to the repository to retreive a readiness profile for an offender id`() {
    whenever(readinessProfileRepository.findById(any())).thenReturn(TestData.updatedReadinessProfileNotes)
    val profile = profileService.getProfileForOffender(TestData.newOffenderId)
    assert(profile.equals(TestData.updatedReadinessProfileNotes.get()))
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
