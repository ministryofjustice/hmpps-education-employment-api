@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v1

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.AlreadyExistsException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.InvalidStateException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v1.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.V1Profiles
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.V1Profiles.profileIncorrectStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.V1Profiles.profileStatusNewAndBothStateIncorrect
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createdTime
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.deepCopy
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.modifiedAgainTime
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.offenderIdList
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfileRepository
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.UnitTestBase
import java.util.*
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ProfileV1ServiceTest : UnitTestBase() {
  @Mock
  private lateinit var readinessProfileRepository: ReadinessProfileRepository

  @InjectMocks
  private lateinit var profileService: ProfileV1Service

  private val typeRefProfileV1 by lazy { object : TypeReference<Profile>() {} }

  @Nested
  @DisplayName("Given a new readiness profile")
  inner class GivenNewProfile {
    private val prisonNumber = ProfileObjects.newOffenderId
    private val bookingId = ProfileObjects.newBookingId
    private val userId = ProfileObjects.createdBy
    private val profile = V1Profiles.profile
    private val readinessProfile = V1Profiles.readinessProfile

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
    private val profile = V1Profiles.profile
    private val userIdCreator = ProfileObjects.createdBy

    private val updatedBookingId = ProfileObjects.updatedBookingId
    private val updatedProfile = V1Profiles.updatedReadinessProfile
    private val userId = ProfileObjects.updatedBy

    private val updatedProfileWithDecline = V1Profiles.updatedReadinessProfileAndDeclined1

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
      private val profileWithDecline = V1Profiles.readinessProfileAndDeclined1

      private val profileDataWithAcceptance = V1Profiles.profileAcceptedAndModified
      private val profileWithAcceptance = V1Profiles.updatedReadinessProfileAndAccepted1

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
      private val profileWithAcceptance = V1Profiles.readinessProfileAndAccepted1
      private val profileDataWithAcceptance = V1Profiles.profileAcceptedAndModified

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
    private val profile = V1Profiles.profile

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

  @Nested
  @DisplayName("Given readiness profile (v1) with history")
  inner class GivenProfileWithHistory {
    private lateinit var expectedProfile: ReadinessProfile
    private lateinit var prisonNumber: String
    private lateinit var profile: Profile
    private val modifiedTime = ProfileObjects.modifiedTime

    @BeforeEach
    internal fun setUp() {
      expectedProfile = givenProfileWithSupportDeclinedTwiceInHistory()
      prisonNumber = expectedProfile.offenderId
      profile = parseProfileV1(expectedProfile.profileData)
//      lenient().whenever(profileService.parseProfile(expectedProfile.profileData)).thenReturn(profile)
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
      profileV1JsonToValue(profile.profileData).let {
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
      profileV1JsonToValue(profile.profileData).let {
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

      profileV1JsonToValue(profile.profileData).let {
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

  @Test
  fun `retrieve a list of readiness profiles for list offender ids`() {
    val expectedProfiles = V1Profiles.profileList
    val prisonNumbers = offenderIdList
    whenever(readinessProfileRepository.findAllById(any())).thenReturn(expectedProfiles)

    val profileList = profileService.getProfilesForOffenders(prisonNumbers)

    assertThat(profileList).containsAll(expectedProfiles)
  }

  private fun assertProfileIsUpdated(userId: String, prisonNumber: String, bookingId: Long, profile: Profile) = profileService.updateProfileForOffender(userId, prisonNumber, bookingId, profile)
    .also { verify(readinessProfileRepository).save(any()) }

  private fun givenProfileNotExist(prisonNumber: String) = givenProfileExistence(prisonNumber, false)
  private fun givenProfileExist(prisonNumber: String) = givenProfileExistence(prisonNumber, true)
  private fun givenProfileExistence(prisonNumber: String, isExisting: Boolean) = whenever(readinessProfileRepository.existsById(prisonNumber)).thenReturn(isExisting)

  private fun givenSavedProfile(profile: ReadinessProfile) = whenever(readinessProfileRepository.save(any())).thenReturn(profile)

  private fun givenProfileFound(profile: ReadinessProfile) = whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(profile))

  private fun givenProfileWithSupportDeclinedTwiceInHistory() = V1Profiles.readinessProfileV1WithSupportDeclinedTwiceAndThenAccepted.also {
    lenient().whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(it.deepCopy()))
  }

  private fun parseProfileV1(profileData: JsonNode): Profile = objectMapper.treeToValue(profileData, typeRefProfileV1)
}
