package uk.gov.justice.digital.hmpps.educationemployment.api.resource.v2

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.lenient
import org.mockito.Mockito.reset
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isA
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ProfileV2Service
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.V2Profiles
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.V2Profiles.profileAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.V2Profiles.readinessProfileOfAnotherPrisoner
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.V2Profiles.readinessProfileOfKnownPrisoner
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.joinToJsonString
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.resource.ControllerTestBase
import kotlin.test.assertEquals

private const val READINESS_PROFILES_PATH = "/readiness-profiles"
const val SEARCH_ENDPOINT = "$READINESS_PROFILES_PATH/search"
const val PROFILE_ENDPOINT = "$READINESS_PROFILES_PATH/{id}"

@WebMvcTest(controllers = [ProfileResourceController::class])
@ContextConfiguration(classes = [ProfileResourceController::class])
class ProfileResourceControllerTest : ControllerTestBase() {
  @MockitoBean
  private lateinit var profileService: ProfileV2Service

  @BeforeEach
  internal fun reset() {
    reset(profileService)
    initMvcMock(ProfileResourceController(profileService, timeProvider))
  }

  @Nested
  @DisplayName("Given a profile")
  inner class GivenAProfile {
    private val readinessProfile = readinessProfileOfKnownPrisoner
    private val prisonNumber = readinessProfile.offenderId
    private val bookingId = readinessProfile.bookingId
    private val createdBy = readinessProfile.createdBy
    private val profile = V2Profiles.profile

    @BeforeEach
    internal fun setup() {
      lenient().whenever(profileService.parseProfile(eq(readinessProfile.profileData))).thenReturn(profile)
    }

    @Test
    fun `Test Post of a new profile `() {
      whenever(profileService.createProfileForOffender(any(), any(), any(), isA<Profile>())).thenReturn(readinessProfile)
      val createRequest = ProfileObjects.createProfileJsonRequest

      assertCreateProfileIsExpected(prisonNumber, createRequest)
    }

    @Test
    fun `Test Put of an update profile `() {
      whenever(profileService.updateProfileForOffender(any(), any(), any(), isA<Profile>())).thenReturn(readinessProfile)
      val updateRequest = ProfileObjects.createProfileJsonRequest

      assertUpdateProfileIsExpected(prisonNumber, updateRequest)
    }

    @Test
    fun `Test Get profile of an Offender `() {
      whenever(profileService.getProfileForOffender(any())).thenReturn(readinessProfile)

      assertRetrieveProfileIsExpected(prisonNumber)
    }

    private fun assertCreateProfileIsExpected(prisonNumber: String, requestJson: String) = assertCreateOrUpdateProfileIsExpected(
      post(PROFILE_ENDPOINT, prisonNumber),
      requestJson,
    ).also { verify(profileService, times(1)).createProfileForOffender(any(), any(), any(), isA<Profile>()) }

    private fun assertUpdateProfileIsExpected(prisonNumber: String, requestJson: String) = assertCreateOrUpdateProfileIsExpected(
      put(PROFILE_ENDPOINT, prisonNumber),
      requestJson,
    ).also { verify(profileService, times(1)).updateProfileForOffender(any(), any(), any(), isA<Profile>()) }

    private fun assertCreateOrUpdateProfileIsExpected(requestBuilder: MockHttpServletRequestBuilder, requestJson: String) = assertReadWriteApiReplyJson(requestBuilder, requestJson)
      .also { result ->
        readinessProfileToValue(result.response.contentAsString).let {
          assertEquals(createdBy, it.createdBy)
          assertEquals(prisonNumber, it.offenderId)
          assertEquals(bookingId, it.bookingId)
        }
      }

    private fun assertRetrieveProfileIsExpected(prisonNumber: String) = assertReadOnlyApiReplyJson(get(PROFILE_ENDPOINT, prisonNumber))
      .also { result ->
        readinessProfileToValue(result.response.contentAsString).let {
          assertEquals(createdBy, it.createdBy)
          assertEquals(prisonNumber, it.offenderId)
          assertEquals(bookingId, it.bookingId)
        }

        verify(profileService, times(1)).getProfileForOffender(any())
      }
  }

  @Nested
  @DisplayName("Given some profiles")
  inner class GivenSomeProfiles {
    private val prisonNumbers = ProfileObjects.run { listOf(knownPrisonNumber, anotherPrisonNumber) }
    private val readinessProfileMap = mapOf(
      readinessProfileOfKnownPrisoner.profileData to V2Profiles.profile,
      readinessProfileOfAnotherPrisoner.profileData to profileAccepted,
    )
    private val profileList = listOf(readinessProfileOfKnownPrisoner, readinessProfileOfAnotherPrisoner)

    @Test
    fun `Test Get of profile list for offenders `() {
      whenever(profileService.getProfilesForOffenders(any())).thenReturn(profileList)
      readinessProfileMap.forEach { (profileData, profile) ->
        whenever(profileService.parseProfile(eq(profileData))).thenReturn(profile)
      }

      assertSearchProfileIsExpected(prisonNumbers, profileList)
      verify(profileService, times(1)).getProfilesForOffenders(any())
    }

    private fun assertSearchProfileIsExpected(
      prisonNumbers: List<String>,
      expectedProfiles: List<ReadinessProfile>,
    ) = assertReadOnlyApiReplyJson(post(SEARCH_ENDPOINT), prisonNumbers.joinToJsonString()).also { result ->
      readinessProfileToList(result.response.contentAsString).forEachIndexed { i, it ->
        with(expectedProfiles[i]) {
          assertEquals(createdBy, it.createdBy)
          assertEquals(offenderId, it.offenderId)
          assertEquals(bookingId, it.bookingId)
        }
      }
    }
  }
}
