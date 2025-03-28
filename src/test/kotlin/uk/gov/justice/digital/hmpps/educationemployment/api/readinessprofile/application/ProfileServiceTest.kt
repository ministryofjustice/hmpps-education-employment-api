package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData.createdTime
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData.modifiedAgainTime
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData.modifiedTime
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData.newNotes
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData.profile_accpeted_modified
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData.profile_declinedModified
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData.readinessProfileWithSupportDeclinedTwiceAndThenAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData.readinessProfile_accepted_1
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData.readinessProfile_declined_1
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData.readinessProfile_declined_1_declined_list
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData.updatedReadinessProfile_accpeted_1
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData.updatedReadinessProfile_declined_1
import uk.gov.justice.digital.hmpps.educationemployment.api.deepCopy
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.AlreadyExistsException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.InvalidStateException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfileRepository
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.UnitTestBase
import java.util.*
import kotlin.test.assertFailsWith

class ProfileServiceTest : UnitTestBase() {
  @Mock
  private lateinit var readinessProfileRepository: ReadinessProfileRepository

  @InjectMocks
  private lateinit var profileService: ProfileService

  @Nested
  @DisplayName("Given a new readiness profile")
  inner class GivenNewProfile {
    private val prisonNumber = TestData.newOffenderId
    private val bookingId = TestData.newBookingId
    private val userId = TestData.createdBy
    private val profile = TestData.profile
    private val readinessProfile = TestData.readinessProfile

    @BeforeEach
    internal fun setUp() {
      givenProfileNotExist(prisonNumber)
    }

    @Test
    fun `save the readiness profile`() {
      givenSavedProfile(readinessProfile)

      val savedProfile = profileService.createProfileForOffender(userId, prisonNumber, bookingId, profile)

      verify(readinessProfileRepository).save(any())
      savedProfile.let {
        assertThat(it.createdBy).isEqualTo(userId)
        assertThat(it.offenderId).isEqualTo(prisonNumber)
        assertThat(it.bookingId).isEqualTo(bookingId)
      }
    }

    @Test
    fun `throws an exception, when create a readiness profile with incorrect status`() {
      assertFailsWithInvalidState(TestData.profile_IncorrectStatus)
    }

    @Test
    fun `throws an exception, when create the readiness profile with both support states`() {
      assertFailsWithInvalidState(TestData.profile_NEW_BOTHSTATE_INCOORECT)
    }

    private fun assertFailsWithInvalidState(profile: Profile) {
      assertFailsWith<InvalidStateException> {
        profileService.createProfileForOffender(userId, prisonNumber, bookingId, profile)
      }.let {
        assertThat(it.message).contains("Readiness profile is in an invalid state for")
      }
    }
  }

  @Nested
  @DisplayName("Given an existing readiness profile")
  inner class GivenExistingProfile {
    private val prisonNumber = TestData.newOffenderId
    private val bookingId = TestData.newBookingId
    private val profile = TestData.profile
    private val userIdCreator = TestData.createdBy

    private val updatedBookingId = TestData.updatedBookingId
    private val updatedProfile = TestData.updatedReadinessProfile
    private val userId = TestData.updatedBy

    @Nested
    @DisplayName("And the readiness profile is found")
    inner class AndProfileIsFound {
      @BeforeEach
      internal fun setUp() {
        givenProfileFound(updatedProfile)
      }

      @Test
      fun `update the readiness profile`() {
        givenSavedProfile(updatedProfile)

        val actual = assertProfileIsUpdated(userId, prisonNumber, bookingId, profile)

        actual.let {
          assertThat(it.offenderId).isEqualTo(prisonNumber)
          assertThat(it.bookingId).isEqualTo(updatedBookingId)
          assertThat(it.modifiedBy).isEqualTo(userId)
          assertThat(it.createdBy).isEqualTo(userIdCreator)
        }
      }

      @Test
      fun `update the readiness profile Prison location`() {
        givenSavedProfile(updatedProfile)

        val actual = assertProfileIsUpdated(userId, prisonNumber, bookingId, profile)

        profileJsonToValue(actual.profileData).let {
          assertThat(it.prisonName).isEqualTo(profile.prisonName)
        }
      }
    }

    @Nested
    @DisplayName("And the readiness profile with decline is found")
    inner class AndProfileWithDeclineIsFound {
      private val profileWithDecline = readinessProfile_declined_1

      @BeforeEach
      internal fun setUp() {
        givenProfileFound(profileWithDecline)
      }

      @Test
      fun `add supportDeclined to empty supportDeclined List, on Update of declined State in readiness profile`() {
        givenSavedProfile(updatedReadinessProfile_declined_1)

        val actual = assertProfileIsUpdated(userId, prisonNumber, bookingId, profile_declinedModified)

        profileJsonToValue(actual.profileData).let {
          assertThat(it.supportDeclined_history!!.size == 1)
        }
      }

      @Test
      fun `add supportDeclined to empty supportDeclined List, on Update of acceptedSupport  in readiness profile`() {
        givenSavedProfile(updatedReadinessProfile_accpeted_1)

        val actual = assertProfileIsUpdated(userId, prisonNumber, bookingId, profile_accpeted_modified)

        profileJsonToValue(actual.profileData).let {
          assertThat(it.supportDeclined_history!!.size == 1)
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.DECLINED_TO_ACCEPTED)
        }
      }

      @Test
      fun `makes a call to the repository to verify support declined is not added to supportDeclineList when declinedsupport has not changed in readiness profile`() {
        givenSavedProfile(profileWithDecline)

        val actual = assertProfileIsUpdated(userId, prisonNumber, bookingId, profile_declinedModified)

        profileJsonToValue(actual.profileData).let {
          assertThat(it.supportDeclined_history!!.size == 0)
        }
      }
    }

    @Nested
    @DisplayName("And the readiness profile with acceptance is found")
    inner class AndProfileWithAcceptanceIsFound {
      private val profileWithAcceptance = readinessProfile_accepted_1

      @BeforeEach
      internal fun setUp() {
        givenProfileFound(profileWithAcceptance)
      }

      @Test
      fun `add supportAccepted to empty supportAccepted List, on Update of declinedSupport in readiness profile`() {
        givenSavedProfile(updatedReadinessProfile_declined_1)

        val actual = assertProfileIsUpdated(userId, prisonNumber, bookingId, profile_accpeted_modified)

        profileJsonToValue(actual.profileData).let {
          assertThat(it.supportAccepted_history!!.size == 1)
          assertThat(it.statusChangeType!!.equals(StatusChange.ACCEPTED_TO_DECLINED))
        }
      }

      @Test
      fun `add support accepted to empty supportAccpetedList on Update of declined in readiness profile`() {
        givenSavedProfile(updatedReadinessProfile_accpeted_1)

        val actual = assertProfileIsUpdated(userId, prisonNumber, bookingId, profile_accpeted_modified)

        assertThat(profileJsonToValue(actual.profileData).supportAccepted_history!!.size == 1)
      }
    }

    @Test
    fun `add supportDeclined to supportDeclineList on Update of declinedSupport in readiness profile`() {
      val profileDeclinedTwice = readinessProfile_declined_1_declined_list
      givenProfileFound(profileDeclinedTwice)
      givenSavedProfile(updatedReadinessProfile_declined_1)

      val actual = assertProfileIsUpdated(userId, prisonNumber, bookingId, profile_declinedModified)

      assertThat(profileJsonToValue(actual.profileData).supportDeclined_history!!.size == 2)
    }

    @Test
    fun `throws an exception, when re-create an existing readiness profile`() {
      givenProfileExist(prisonNumber)
      assertFailsWith<AlreadyExistsException> {
        profileService.createProfileForOffender(userIdCreator, prisonNumber, bookingId, profile)
      }.let {
        assertThat(it.message).contains("Readiness profile already exists for offender")
      }
    }
  }

  @Nested
  @DisplayName("Given an existing readiness profile, and some notes")
  inner class GivenExistingProfileAndNotes {
    private val prisonNumber = TestData.newOffenderId
    private val userIdCreator = TestData.createdBy

    private val updatedProfile = TestData.updatedReadinessProfile

    private val updatedProfileWithNotes = TestData.updatedReadinessProfileNotes

    private val actionToDoCV = TestData.actionToDoCV
    private val notesText = TestData.noteString

    @Test
    fun `save a readiness profile note`() {
      givenProfileFound(updatedProfile)
      givenSavedProfile(updatedProfile)

      val listNote = profileService.addProfileNoteForOffender(userIdCreator, prisonNumber, actionToDoCV, notesText)

      verify(readinessProfileRepository).save(any())
      assertThat(listNote[0].text).isEqualTo(notesText)
    }

    @Test
    fun `retrieve a readiness profile note`() {
      givenProfileFound(updatedProfileWithNotes)

      val listNote = profileService.getProfileNotesForOffender(prisonNumber, actionToDoCV)

      assertThat(listNote[0].text).isEqualTo(notesText)
    }

    @Test
    fun `retrieve a readiness profile for an offender id`() {
      givenProfileFound(updatedProfileWithNotes)

      val profile = profileService.getProfileForOffender(prisonNumber)

      assertThat(profile).isEqualTo(updatedProfileWithNotes)
    }
  }

  @Test
  fun `retrieve a list of readiness profiles for list offender ids`() {
    val expectedProfiles = TestData.profileList
    val prisonNumbers = TestData.offenderIdList
    whenever(readinessProfileRepository.findAllById(any())).thenReturn(expectedProfiles)

    val profileList = profileService.getProfilesForOffenders(prisonNumbers)

    assertThat(profileList).containsAll(expectedProfiles)
  }

  @Nested
  @DisplayName("Given a non-existing readiness profile")
  inner class GivenNonExistingProfile {
    private val prisonNumber = TestData.newOffenderId
    private val bookingId = TestData.newBookingId
    private val userId = TestData.createdBy
    private val profile = TestData.profile

    @BeforeEach
    internal fun setUp() {
      givenNoProfileFound()
    }

    @Test
    fun `throws an exception, when update a non-existing readiness profile`() {
      assertFailsWith<NotFoundException> {
        profileService.updateProfileForOffender(userId, prisonNumber, bookingId, profile)
      }.let {
        assertThat(it.message).contains("Readiness profile does not exist for offender")
      }
    }

    @Test
    fun `throws an exception, when add a note to a non-existing readiness profile`() {
      assertFailsWith<NotFoundException> {
        profileService.addProfileNoteForOffender(userId, prisonNumber, ActionTodo.BANK_ACCOUNT, newNotes)
      }.let {
        assertThat(it.message).contains("Readiness profile does not exist for offender")
      }
    }

    @Test
    fun `throws an exception, when retrieve a note from a non-existing readiness profile`() {
      assertFailsWith<NotFoundException> {
        profileService.getProfileNotesForOffender(userId, ActionTodo.BANK_ACCOUNT)
      }.let {
        assertThat(it.message).contains("Readiness profile does not exist for offender")
      }
    }

    private fun givenNoProfileFound() = whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.empty())
  }

  @Nested
  @DisplayName("Given profile with history")
  inner class GivenProfileWithHistory {
    private lateinit var expectedProfile: ReadinessProfile
    private lateinit var prisonNumber: String

    @BeforeEach
    internal fun setUp() {
      expectedProfile = givenProfileWithSupportDeclinedTwiceInHistory()
      prisonNumber = expectedProfile.offenderId
    }

    @Test
    fun `retrieve readiness profile with full history, when no period is specified`() {
      val profile = profileService.getProfileForOffenderFilterByPeriod(prisonNumber)

      assertThat(profile).usingRecursiveComparison().isEqualTo(expectedProfile)
    }

    @Test
    fun `retrieve readiness profile with full history, sorted in descending order`() {
      val profile = profileService.getProfileForOffenderFilterByPeriod(prisonNumber)

      assertThat(profile)
        .usingRecursiveComparison().ignoringFields("profileData")
        .isEqualTo(expectedProfile)
      profileJsonToValue(profile.profileData).let {
        assertThat(it.supportDeclined_history).isNotNull.hasSize(2)
        val firstTimestamp = it.supportDeclined_history!![0].modifiedDateTime
        val secondTimestamp = it?.supportDeclined_history!![1].modifiedDateTime
        assertThat(firstTimestamp).isAfterOrEqualTo(secondTimestamp)
      }
    }

    @Test
    fun `retrieve readiness profile with full history, when it is created within specific period`() {
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
      val prisonNumber = expectedProfile.offenderId
      val fromDate = modifiedAgainTime.toLocalDate().minusDays(1)
      val toDate = null

      val profile = profileService.getProfileForOffenderFilterByPeriod(prisonNumber, fromDate, toDate)

      assertThat(profile)
        .usingRecursiveComparison().ignoringFields("profileData")
        .isEqualTo(expectedProfile)
      profileJsonToValue(profile.profileData).let {
        assertThat(it.supportDeclined_history).isNotNull.hasSize(1)
        assertThat(it.supportDeclined_history!![0].modifiedDateTime).isEqualTo(modifiedTime)
      }
    }

    @Test
    fun `retrieve readiness profile with no history, when it is created and last modified before specific period`() {
      val fromDate = expectedProfile.modifiedDateTime.toLocalDate().plusDays(1)
      val toDate = null

      val profile = profileService.getProfileForOffenderFilterByPeriod(prisonNumber, fromDate, toDate)

      assertThat(profile.profileData.findPath("supportDeclined_history")).isEmpty()
    }

    @Test
    fun `retrieve readiness profile with no history, when it is created before specific period and only modified afterward`() {
      val fromDate = expectedProfile.createdDateTime.toLocalDate()
      val toDate = modifiedTime.toLocalDate().minusDays(1)

      val profile = profileService.getProfileForOffenderFilterByPeriod(prisonNumber, fromDate, toDate)
      assertThat(profile.createdDateTime.toLocalDate()).isBeforeOrEqualTo(fromDate)

      profileJsonToValue(profile.profileData).let {
        assertThat(it.supportDeclined_history).isNotNull.hasSize(1)
        assertThat(it.supportDeclined_history!![0].modifiedDateTime).isEqualTo(createdTime)
      }
    }

    @Test
    fun `throws an exception, when retrieve readiness profile with invalid period`() {
      val fromDate = expectedProfile.modifiedDateTime.toLocalDate().plusDays(1)
      val toDate = expectedProfile.createdDateTime.toLocalDate().minusDays(1)

      assertFailsWith<IllegalArgumentException> {
        profileService.getProfileForOffenderFilterByPeriod(prisonNumber, fromDate, toDate)
      }.let {
        assertThat(it.message).isEqualTo("fromDate cannot be after toDate")
      }
    }

    @Test
    fun `throws an exception, when retrieve readiness profile created after specific period`() {
      val fromDate = null
      val toDate = expectedProfile.createdDateTime.toLocalDate().minusDays(1)

      assertFailsWith<NotFoundException> {
        profileService.getProfileForOffenderFilterByPeriod(prisonNumber, fromDate, toDate)
      }.let {
        assertThat(it.message).contains(prisonNumber)
      }
    }
  }

  private fun assertProfileIsUpdated(userId: String, prisonNumber: String, bookingId: Long, profile: Profile) = profileService.updateProfileForOffender(userId, prisonNumber, bookingId, profile)
    .also { verify(readinessProfileRepository).save(any()) }

  private fun givenProfileNotExist(prisonNumber: String) = givenProfileExistence(prisonNumber, false)
  private fun givenProfileExist(prisonNumber: String) = givenProfileExistence(prisonNumber, true)
  private fun givenProfileExistence(prisonNumber: String, isExisting: Boolean) = whenever(readinessProfileRepository.existsById(prisonNumber)).thenReturn(isExisting)

  private fun givenSavedProfile(profile: ReadinessProfile) = whenever(readinessProfileRepository.save(any())).thenReturn(profile)

  private fun givenProfileFound(profile: ReadinessProfile) = whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(profile))

  private fun givenProfileWithSupportDeclinedTwiceInHistory() = readinessProfileWithSupportDeclinedTwiceAndThenAccepted.also {
    Mockito.lenient().whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(it.deepCopy()))
  }
}
