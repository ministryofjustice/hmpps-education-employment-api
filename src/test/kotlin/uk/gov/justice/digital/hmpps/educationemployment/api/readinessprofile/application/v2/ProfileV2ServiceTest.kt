package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2

import com.fasterxml.jackson.databind.node.BooleanNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.AlreadyExistsException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.InvalidStateException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.V1Profiles
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.V2Profiles
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.V2Profiles.profileIncorrectStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.V2Profiles.profileStatusNewAndBothStateIncorrect
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.offenderIdList
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfileRepository
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.UnitTestBase
import java.util.*
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ProfileV2ServiceTest : UnitTestBase() {
  @Mock
  private lateinit var readinessProfileRepository: ReadinessProfileRepository

  @InjectMocks
  private lateinit var profileService: ProfileV2Service

  private val expectedVersion = PROFILE_SCHEMA_VERSION

  @Nested
  @DisplayName("Given a new readiness profile")
  inner class GivenNewProfile {
    private val prisonNumber = ProfileObjects.newOffenderId
    private val bookingId = ProfileObjects.newBookingId
    private val userId = ProfileObjects.createdBy
    private val profile = V2Profiles.profile
    private val readinessProfile = V2Profiles.readinessProfile

    @Test
    fun `save the readiness profile`() {
      givenProfileNotExist(prisonNumber)
      givenSavedProfile(readinessProfile)

      val savedProfile = profileService.createProfileForOffender(userId, prisonNumber, bookingId, profile)

      verify(readinessProfileRepository).save(any())
      savedProfile.let {
        assertThat(it.createdBy).isEqualTo(userId)
        assertThat(it.offenderId).isEqualTo(prisonNumber)
        assertThat(it.bookingId).isEqualTo(bookingId)
        assertThat(it.schemaVersion).isEqualTo(expectedVersion)
      }
    }

    @Test
    fun `save the readiness profile, and set within-12-weeks to true by default, when value is missing`() {
      givenProfileNotExist(prisonNumber)
      givenSavedProfile(readinessProfile)
      val newProfile = profile.copy(within12Weeks = null)

      profileService.createProfileForOffender(userId, prisonNumber, bookingId, newProfile)

      val actualCaptor = argumentCaptor<ReadinessProfile>().also { verify(readinessProfileRepository).save(it.capture()) }
      with(actualCaptor.firstValue.profileData) {
        assertThat(get("within12Weeks").booleanValue()).isEqualTo(true)
      }
    }

    @Test
    fun `throws an exception, when create a readiness profile with incorrect status`() {
      assertFailsWithInvalidState(profileIncorrectStatus)
    }

    @Test
    fun `throws an exception, when create the readiness profile with both support states`() {
      assertFailsWithInvalidState(profileStatusNewAndBothStateIncorrect)
    }

    @Test
    fun `throws an exception, when create the readiness profile without prisonId`() {
      val newProfile = profile.copy(prisonId = null)

      assertFailsWith<IllegalArgumentException> {
        profileService.createProfileForOffender(userId, prisonNumber, bookingId, newProfile)
      }.let {
        assertThat(it.message).isEqualTo("prisonId is missing")
      }
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
    private val prisonNumber = ProfileObjects.newOffenderId
    private val bookingId = ProfileObjects.newBookingId
    private val profile = V2Profiles.profile
    private val userIdCreator = ProfileObjects.createdBy

    private val updatedBookingId = ProfileObjects.updatedBookingId
    private val updatedProfile = V2Profiles.updatedReadinessProfile
    private val userId = ProfileObjects.updatedBy

    private val updatedProfileWithDecline = V2Profiles.updatedReadinessProfileAndDeclined1

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

        val actual = assertProfileIsUpdated(userId, prisonNumber, bookingId, profile.copy())

        actual.let {
          assertThat(it.offenderId).isEqualTo(prisonNumber)
          assertThat(it.bookingId).isEqualTo(updatedBookingId)
          assertThat(it.modifiedBy).isEqualTo(userId)
          assertThat(it.createdBy).isEqualTo(userIdCreator)
          assertThat(it.schemaVersion).isEqualTo(expectedVersion)
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

      @Test
      fun `throws an exception, when update the readiness profile without prisonId`() {
        val revisedProfile = profile.copy(prisonId = null)

        assertFailsWith<IllegalArgumentException> {
          profileService.createProfileForOffender(userId, prisonNumber, bookingId, revisedProfile)
        }.let {
          assertThat(it.message).isEqualTo("prisonId is missing")
        }
      }

      @Test
      fun `update the readiness profile, and set within-12-weeks to true by default, when value is missing`() {
        val revisedProfile = profile.copy(within12Weeks = null)
        givenSavedProfile(updatedProfile.copy())

        assertProfileIsUpdated(userId, prisonNumber, bookingId, revisedProfile)

        val actualCaptor = argumentCaptor<ReadinessProfile>().also { verify(readinessProfileRepository).save(it.capture()) }
        with(actualCaptor.firstValue.profileData) {
          assertThat(get("within12Weeks").booleanValue()).isEqualTo(true)
        }
      }
    }

    @Nested
    @DisplayName("And the readiness profile with 'support declined' is found")
    inner class AndProfileWithDeclineIsFound {
      private val profileWithDecline = V2Profiles.readinessProfileAndDeclined1
      private val profileDataWithAcceptance = V2Profiles.profileAcceptedAndModified.copy()
      private val profileDataReadyToWork = V2Profiles.profileReadyToWorkAndModified.copy()
      private val profileDataNoRightToWork = V2Profiles.profileNoRightToWorkAndModified.copy()
      private val profileDataWithDecline = V2Profiles.profileDeclinedAndModified.copy()

      @BeforeEach
      internal fun setUp() {
        givenProfileFound(profileWithDecline.copy())
        mockSaveProfile()
      }

      @Test
      fun `set statusChangeType to DECLINED_TO_ACCEPTED, on Update of 'support needed' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithDecline
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataWithAcceptance)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.DECLINED_TO_ACCEPTED)
          assertThat(it.statusChange!!).isEqualTo(true)
        }
      }

      @Test
      fun `set statusChangeType to DECLINED_TO_ACCEPTED, on Update of 'ready to work' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithDecline
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataReadyToWork)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.DECLINED_TO_ACCEPTED)
          assertThat(it.statusChange!!).isEqualTo(true)
        }
      }

      @Test
      fun `set statusChangeType to NULL, on Update of 'no right to work' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithDecline
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataNoRightToWork)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertNull(it.statusChangeType)
          assertThat(it.statusChange!!).isEqualTo(true)
        }
      }

      @Test
      fun `set statusChangeType to NULL, on no change from 'support declined' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithDecline
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataWithDecline)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertNull(it.statusChangeType)
          assertThat(it.statusChange!!).isEqualTo(false)
        }
      }
    }

    @Nested
    @DisplayName("And the readiness profile with 'support accepted' is found")
    inner class AndProfileWithAcceptanceIsFound {
      private val profileWithAcceptance = V2Profiles.readinessProfileAndAccepted1
      private val profileDataWithDecline = V2Profiles.profileDeclinedAndModified.copy()
      private val profileDataWithAcceptance = V2Profiles.profileAcceptedAndModified.copy()
      private val profileDataReadyToWork = V2Profiles.profileReadyToWorkAndModified.copy()
      private val profileDataNoRightToWork = V2Profiles.profileNoRightToWorkAndModified.copy()


      @BeforeEach
      internal fun setUp() {
        givenProfileFound(profileWithAcceptance.copy())
        mockSaveProfile()
      }

      @Test
      fun `set statusChangeType to ACCEPTED_TO_DECLINED, on Update of 'support declined' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithAcceptance
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataWithDecline)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.ACCEPTED_TO_DECLINED)
          assertThat(it.statusChange!!).isEqualTo(true)
        }
      }

      @Test
      fun `set statusChangeType to NULL, on Update of 'ready to work' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithAcceptance
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataReadyToWork)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertNull(it.statusChangeType)
          assertThat(it.statusChange!!).isEqualTo(true)
        }
      }

      @Test
      fun `set statusChangeType to ACCEPTED_TO_DECLINED, on Update of 'no right to work' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithAcceptance
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataNoRightToWork)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.ACCEPTED_TO_DECLINED)
          assertThat(it.statusChange!!).isEqualTo(true)
        }
      }

      @Test
      fun `set statusChangeType to NULL, on no change from 'support needed' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithAcceptance
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataWithAcceptance)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertNull(it.statusChangeType)
          assertThat(it.statusChange!!).isEqualTo(false)
        }
      }
    }

    @Nested
    @DisplayName("And the readiness profile with 'no right to work' is found")
    inner class AndProfileWithNoRightToWorkIsFound {
      private val profileWithNoRightToWork = V2Profiles.readinessProfileAndNoRightToWork1
      private val profileDataWithAcceptance = V2Profiles.profileAcceptedAndModified.copy()
      private val profileDataWithDecline = V2Profiles.profileDeclinedAndModified.copy()
      private val profileDataReadyToWork = V2Profiles.profileReadyToWorkAndModified.copy()
      private val profileDataNoRightToWork = V2Profiles.profileNoRightToWorkAndModified.copy()

      @BeforeEach
      internal fun setUp() {
        givenProfileFound(profileWithNoRightToWork.copy())
        mockSaveProfile()
      }

      @Test
      fun `set statusChangeType to DECLINED_TO_ACCEPTED, on Update of 'support needed' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithNoRightToWork
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataWithAcceptance)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.DECLINED_TO_ACCEPTED)
          assertThat(it.statusChange!!).isEqualTo(true)
        }
      }

      @Test
      fun `set statusChangeType to DECLINED_TO_ACCEPTED, on Update of 'ready to work' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithNoRightToWork
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataReadyToWork)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.DECLINED_TO_ACCEPTED)
          assertThat(it.statusChange!!).isEqualTo(true)
        }
      }

      @Test
      fun `set statusChangeType to NULL, on Update of 'support declined' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithNoRightToWork
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataWithDecline)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertNull(it.statusChangeType)
          assertThat(it.statusChange!!).isEqualTo(true)
        }
      }

      @Test
      fun `set statusChangeType to NULL, on no change from 'no right to work' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithNoRightToWork
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataNoRightToWork)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertNull(it.statusChangeType)
          assertThat(it.statusChange!!).isEqualTo(false)
        }
      }
    }

    @Nested
    @DisplayName("And the readiness profile with 'ready to work' is found")
    inner class AndProfileWithReadyToWorkIsFound {
      private val profileWithReadyToWork = V2Profiles.readinessProfileAndReadyToWork1
      private val profileDataWithDecline = V2Profiles.profileDeclinedAndModified.copy()
      private val profileDataWithAcceptance = V2Profiles.profileAcceptedAndModified.copy()
      private val profileDataReadyToWork = V2Profiles.profileReadyToWorkAndModified.copy()
      private val profileDataNoRightToWork = V2Profiles.profileNoRightToWorkAndModified.copy()

      @BeforeEach
      internal fun setUp() {
        givenProfileFound(profileWithReadyToWork.copy())
        mockSaveProfile()
      }

      @Test
      fun `set statusChangeType to ACCEPTED_TO_DECLINED, on Update of 'support declined' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithReadyToWork
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataWithDecline)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.ACCEPTED_TO_DECLINED)
          assertThat(it.statusChange!!).isEqualTo(true)
        }
      }

      @Test
      fun `set statusChangeType to NULL, on Update of 'support needed' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithReadyToWork
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataWithAcceptance)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertNull(it.statusChangeType)
          assertThat(it.statusChange!!).isEqualTo(true)
        }
      }

      @Test
      fun `set statusChangeType to ACCEPTED_TO_DECLINED, on Update of 'no right to work' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithReadyToWork
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataNoRightToWork)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.ACCEPTED_TO_DECLINED)
          assertThat(it.statusChange!!).isEqualTo(true)
        }
      }

      @Test
      fun `set statusChangeType to NULL, on no change from 'ready to work' in readiness profile`() {
        // Before update, expect StatusChangeType is NEW and statusChange is false
        val profileBefore: ReadinessProfile = profileWithReadyToWork
        profileJsonToValue(profileBefore.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.NEW)
          assertThat(it.statusChange!!).isEqualTo(false)
        }

        val profileAfter = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataReadyToWork)

        // After update, expect StatusChangeType and statusChange to have been updated correctly.
        profileJsonToValue(profileAfter.profileData).let {
          assertNull(it.statusChangeType)
          assertThat(it.statusChange!!).isEqualTo(false)
        }
      }
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
  @DisplayName("Given a non-existing readiness profile")
  inner class GivenNonExistingProfile {
    private val prisonNumber = ProfileObjects.newOffenderId
    private val bookingId = ProfileObjects.newBookingId
    private val userId = ProfileObjects.createdBy
    private val profile = V2Profiles.profile

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

    private fun givenNoProfileFound() = whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.empty())
  }

  @Test
  fun `retrieve a list of readiness profiles for list offender ids`() {
    val expectedProfiles = V2Profiles.profileList
    val prisonNumbers = offenderIdList
    whenever(readinessProfileRepository.findAllById(any())).thenReturn(expectedProfiles)

    val profileList = profileService.getProfilesForOffenders(prisonNumbers)

    assertThat(profileList).containsAll(expectedProfiles)
  }

  @Nested
  inner class GivenProfilesWithPreviousVersion {
    private val readinessProfile1 = V2Profiles.readinessProfileOfAnotherPrisoner
    private val readinessProfile2PreviousVersion = V1Profiles.readinessProfileOfKnownPrisoner
    private val readinessProfiles = listOf(readinessProfile1, readinessProfile2PreviousVersion)
    private val readinessProfile2CurrentVersion = V2Profiles.readinessProfileOfKnownPrisoner
    private val expectedReadinessProfiles = listOf(readinessProfile1, readinessProfile2CurrentVersion)
    private val prisonNumbers = readinessProfiles.map { it.offenderId }.toList()
    private val excludedFields = listOf("supportDeclined_history", "supportAccepted_history", "prisonId", "within12Weeks")
    private val excludedMetaData = listOf(".*createdBy", ".*createdDateTime", ".*modifiedBy", ".*modifiedDateTime")
    private val excludedFieldPatterns = (excludedFields + excludedMetaData).map { ".*$it.*" }.toTypedArray()

    @Test
    fun `retrieve a list of readiness profiles for list offender ids, and migrate profiles of previous version on the fly`() {
      whenever(readinessProfileRepository.findAllById(any())).thenReturn(readinessProfiles)
      val expectedProfilesData = expectedReadinessProfiles.map { it.profileData }

      val profileList = profileService.getProfilesForOffenders(prisonNumbers)

      assertThat(profileList).extracting("schemaVersion").containsOnly(expectedVersion)

      assertThat(profileList).extracting("profileData")
        .usingRecursiveComparison().ignoringFieldsMatchingRegexes(*excludedFieldPatterns)
        .isEqualTo(expectedProfilesData)

      profileList.map { it.profileData.get("prisonId") }.forEach { assertThat(it).isNotNull() }
      profileList.map { it.profileData.get("within12Weeks") }.forEach { assertThat(it).isEqualTo(BooleanNode.TRUE) }
    }

    @Test
    fun `retrieve and migrate a readiness profile of previous version`() {
      val prisonNumber = readinessProfile1.offenderId
      val expected = readinessProfile1.profileData
      whenever(readinessProfileRepository.findById(prisonNumber)).thenReturn(Optional.of(readinessProfile1))

      val profile = profileService.getProfileForOffender(prisonNumber)

      assertThat(profile.schemaVersion).isEqualTo(expectedVersion)

      val actual = profile.profileData
      assertThat(actual)
        .usingRecursiveComparison().ignoringFieldsMatchingRegexes(*excludedFieldPatterns)
        .isEqualTo(expected)
      assertThat(actual.get("prisonId")).isNotNull
      assertThat(actual.get("within12Weeks")).isEqualTo(BooleanNode.TRUE)
    }
  }

  private fun assertProfileIsUpdated(userId: String, prisonNumber: String, bookingId: Long, profile: Profile) = profileService.updateProfileForOffender(userId, prisonNumber, bookingId, profile)
    .also { verify(readinessProfileRepository).save(any()) }

  private fun givenProfileNotExist(prisonNumber: String) = givenProfileExistence(prisonNumber, false)
  private fun givenProfileExist(prisonNumber: String) = givenProfileExistence(prisonNumber, true)
  private fun givenProfileExistence(prisonNumber: String, isExisting: Boolean) = whenever(readinessProfileRepository.existsById(prisonNumber)).thenReturn(isExisting)

  private fun givenSavedProfile(profile: ReadinessProfile) = whenever(readinessProfileRepository.save(any())).thenReturn(profile)

  private fun mockSaveProfile() {
    whenever(readinessProfileRepository.save(any())).thenAnswer { it.arguments[0] as ReadinessProfile }
  }

  private fun givenProfileFound(profile: ReadinessProfile) = lenient().whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(profile))
}
