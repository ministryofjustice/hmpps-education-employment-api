package uk.gov.justice.digital.hmpps.educationemployment.api.service

import com.fasterxml.jackson.core.type.TypeReference
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues.Companion.objectMapper
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.deepCopy
import uk.gov.justice.digital.hmpps.educationemployment.api.entity.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.AlreadyExistsException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.InvalidStateException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.repository.ReadinessProfileRepository
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.UnitTestBase
import java.util.*
import kotlin.test.assertFailsWith

class ProfileServiceTest : UnitTestBase() {
  @Mock
  private lateinit var readinessProfileRepository: ReadinessProfileRepository

  @InjectMocks
  private lateinit var profileService: ProfileService

  @BeforeEach
  fun beforeEach() {
    profileService = ProfileService(readinessProfileRepository, timeProvider)
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
  fun `makes a call to the repository to retrieve a readiness profile note`() {
    whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(TestData.updatedReadinessProfileNotes))

    val listNote = profileService.getProfileNotesForOffender(TestData.newOffenderId, TestData.actionToDoCV)
    assert(listNote[0].text.equals(TestData.noteString))
  }

  @Test
  fun `makes a call to the repository to retrieve a readiness profile for an offender id`() {
    whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(TestData.updatedReadinessProfileNotes))
    val profile = profileService.getProfileForOffender(TestData.newOffenderId)
    assert(profile.equals(TestData.updatedReadinessProfileNotes))
  }

  @Test
  fun `makes a call to the repository to retrieve a list of readiness profiles for list offender ids`() {
    whenever(readinessProfileRepository.findAllById(any())).thenReturn(TestData.profileList)
    val profileList = profileService.getProfilesForOffenders(TestData.offenderIdList)
    assert(profileList.containsAll(TestData.profileList))
  }

  @Test
  fun `throws an exception when a call is made to the repository to save the readiness profile`() {
    assertThatExceptionOfType(AlreadyExistsException::class.java).isThrownBy {
      whenever(readinessProfileRepository.existsById(any())).thenReturn(TestData.booleanTrue)

      profileService.createProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile)
    }.withMessageContaining("Readiness profile already exists for offender")
  }

  @Test
  fun `throws an exception when a call is made to the repository to save the readiness profile with incorrect status`() {
    assertThatExceptionOfType(InvalidStateException::class.java).isThrownBy {
      whenever(readinessProfileRepository.existsById(any())).thenReturn(TestData.booleanFalse)

      profileService.createProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile_IncorrectStatus)
    }.withMessageContaining("Readiness profile is in an invalid state for")
  }

  @Test
  fun `throws an exception when a call is made to the repository to save the readiness profile with both support state`() {
    assertThatExceptionOfType(InvalidStateException::class.java).isThrownBy {
      whenever(readinessProfileRepository.existsById(any())).thenReturn(TestData.booleanFalse)

      profileService.createProfileForOffender(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.profile_NEW_BOTHSTATE_INCOORECT)
    }.withMessageContaining("Readiness profile is in an invalid state for")
  }

  @Test
  fun `throws an exception when a call is made to the repository to update the readiness profile`() {
    assertThatExceptionOfType(NotFoundException::class.java).isThrownBy {
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
  fun `throws an exception when a call is made to the repository to retrieve a note from a non existing readiness profile`() {
    assertThatExceptionOfType(NotFoundException::class.java).isThrownBy {
      whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.empty())
      profileService.getProfileNotesForOffender(TestData.createdBy, ActionTodo.BANK_ACCOUNT)
    }.withMessageContaining("Readiness profile does not exist for offender")
  }

  @Test
  fun `retrieve readiness profile with full history, when no period is specified`() {
    val expectedProfile = givenProfileWithSupportDeclinedTwiceInHistory()

    val profile = profileService.getProfileForOffenderFilterByPeriod(prisonNumber = expectedProfile.offenderId)

    assertThat(profile).usingRecursiveComparison().isEqualTo(expectedProfile)
  }

  @Test
  fun `retrieve readiness profile with full history, sorted in descending order`() {
    val expectedProfile = givenProfileWithSupportDeclinedTwiceInHistory()

    val profile = profileService.getProfileForOffenderFilterByPeriod(prisonNumber = expectedProfile.offenderId)

    assertThat(profile)
      .usingRecursiveComparison().ignoringFields("profileData")
      .isEqualTo(expectedProfile)

    val profileData = objectMapper.treeToValue(profile.profileData, object : TypeReference<Profile>() {})
    assertThat(profileData.supportDeclined_history).isNotNull.hasSize(2)
    val firstTimestamp = profileData.supportDeclined_history!![0].modifiedDateTime
    val secondTimestamp = profileData?.supportDeclined_history!![1].modifiedDateTime
    assertThat(firstTimestamp).isAfterOrEqualTo(secondTimestamp)
  }

  @Test
  fun `retrieve readiness profile with full history, when it is created within specific period`() {
    val expectedProfile = givenProfileWithSupportDeclinedTwiceInHistory()
    val prisonNumber = expectedProfile.offenderId
    val fromDate = expectedProfile.createdDateTime.toLocalDate().minusDays(1)
    val toDate = expectedProfile.modifiedDateTime.toLocalDate().plusDays(1)

    val profile = profileService.getProfileForOffenderFilterByPeriod(prisonNumber, fromDate, toDate)

    assertThat(profile).usingRecursiveComparison().isEqualTo(expectedProfile)
    assertThat(profile.createdDateTime).isBeforeOrEqualTo(toDate.atStartOfDay())
    profile.profileData.findPath("supportDeclined_history").let { declinedHistory ->
      assertThat(declinedHistory).isNotNull().hasSize(2)
    }
  }

  @Test
  fun `retrieve readiness profile with partial history and beginning snapshot, when it is created before specific period and last modified during specific period`() {
    val expectedProfile = givenProfileWithSupportDeclinedTwiceInHistory()
    val prisonNumber = expectedProfile.offenderId
    val fromDate = TestData.modifiedAgainTime.toLocalDate().minusDays(1)
    val toDate = null

    val profile = profileService.getProfileForOffenderFilterByPeriod(prisonNumber, fromDate, toDate)

    assertThat(profile)
      .usingRecursiveComparison().ignoringFields("profileData")
      .isEqualTo(expectedProfile)

    val profileData = objectMapper.treeToValue(profile.profileData, object : TypeReference<Profile>() {})
    assertThat(profileData.supportDeclined_history).isNotNull.hasSize(1)
    assertThat(profileData.supportDeclined_history!![0].modifiedDateTime).isEqualTo(TestData.modifiedTime)
  }

  @Test
  fun `retrieve readiness profile with no history, when it is created and last modified before specific period`() {
    val expectedProfile = givenProfileWithSupportDeclinedTwiceInHistory()
    val prisonNumber = expectedProfile.offenderId
    val fromDate = expectedProfile.modifiedDateTime.toLocalDate().plusDays(1)
    val toDate = null

    val profile = profileService.getProfileForOffenderFilterByPeriod(prisonNumber, fromDate, toDate)

    assertThat(profile.profileData.findPath("supportDeclined_history")).isEmpty()
  }

  @Test
  fun `retrieve readiness profile with no history, when it is created before specific period and only modified afterward`() {
    val expectedProfile = givenProfileWithSupportDeclinedTwiceInHistory()
    val prisonNumber = expectedProfile.offenderId
    val fromDate = expectedProfile.createdDateTime.toLocalDate()
    val toDate = TestData.modifiedTime.toLocalDate().minusDays(1)

    val profile = profileService.getProfileForOffenderFilterByPeriod(prisonNumber, fromDate, toDate)
    assertThat(profile.createdDateTime.toLocalDate()).isBeforeOrEqualTo(fromDate)

    val profileData = objectMapper.treeToValue(profile.profileData, object : TypeReference<Profile>() {})
    assertThat(profileData.supportDeclined_history).isNotNull.hasSize(1)
    assertThat(profileData.supportDeclined_history!![0].modifiedDateTime).isEqualTo(TestData.createdTime)
  }

  @Test
  fun `throws an exception, when retrieve readiness profile with invalid period`() {
    val expectedProfile = givenProfileWithSupportDeclinedTwiceInHistory()
    val prisonNumber = expectedProfile.offenderId
    val fromDate = expectedProfile.modifiedDateTime.toLocalDate().plusDays(1)
    val toDate = expectedProfile.createdDateTime.toLocalDate().minusDays(1)

    val exception = assertFailsWith<IllegalArgumentException> {
      profileService.getProfileForOffenderFilterByPeriod(prisonNumber, fromDate, toDate)
    }
    assertThat(exception.message).isEqualTo("fromDate cannot be after toDate")
  }

  @Test
  fun `throws an exception, when retrieve readiness profile created after specific period`() {
    val expectedProfile = givenProfileWithSupportDeclinedTwiceInHistory()
    val prisonNumber = expectedProfile.offenderId
    val fromDate = null
    val toDate = expectedProfile.createdDateTime.toLocalDate().minusDays(1)

    val exception = assertFailsWith<NotFoundException> {
      profileService.getProfileForOffenderFilterByPeriod(prisonNumber, fromDate, toDate)
    }
    assertThat(exception.message).contains(prisonNumber)
  }

  private fun givenProfileWithSupportDeclinedTwiceInHistory() = TestData.readinessProfileWithSupportDeclinedTwiceAndThenAccepted.also {
    lenient().whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(it.deepCopy()))
  }
}
