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
import org.mockito.kotlin.any
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
    fun `throws an exception, when create a readiness profile with incorrect status`() {
      assertFailsWithInvalidState(profileIncorrectStatus)
    }

    @Test
    fun `throws an exception, when create the readiness profile with both support states`() {
      assertFailsWithInvalidState(profileStatusNewAndBothStateIncorrect)
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

        val actual = assertProfileIsUpdated(userId, prisonNumber, bookingId, profile)

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
    }

    @Nested
    @DisplayName("And the readiness profile with decline is found")
    inner class AndProfileWithDeclineIsFound {
      private val profileWithDecline = V2Profiles.readinessProfileAndDeclined1

      private val profileDataWithAcceptance = V2Profiles.profileAcceptedAndModified
      private val profileWithAcceptance = V2Profiles.updatedReadinessProfileAndAccepted1

      @BeforeEach
      internal fun setUp() {
        givenProfileFound(profileWithDecline)
      }

      @Test
      fun `set statusChangeType to DECLINED_TO_ACCEPTED, on Update of acceptedSupport in readiness profile`() {
        givenSavedProfile(profileWithAcceptance)

        val actual = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataWithAcceptance)

        profileV1JsonToValue(actual.profileData).let {
          assertThat(it.statusChangeType!!).isEqualTo(StatusChange.DECLINED_TO_ACCEPTED)
        }
      }
    }

    @Nested
    @DisplayName("And the readiness profile with acceptance is found")
    inner class AndProfileWithAcceptanceIsFound {
      private val profileWithAcceptance = V2Profiles.readinessProfileAndAccepted1
      private val profileDataWithAcceptance = V2Profiles.profileAcceptedAndModified

      @BeforeEach
      internal fun setUp() {
        givenProfileFound(profileWithAcceptance)
      }

      @Test
      fun `set statusChangeType to ACCEPTED_TO_DECLINED, on Update of declinedSupport in readiness profile`() {
        givenSavedProfile(updatedProfileWithDecline)

        val actual = assertProfileIsUpdated(userId, prisonNumber, bookingId, profileDataWithAcceptance)

        profileJsonToValue(actual.profileData).let {
          assertThat(it.statusChangeType!!.equals(StatusChange.ACCEPTED_TO_DECLINED))
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

  private fun givenProfileFound(profile: ReadinessProfile) = whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(profile))
}
