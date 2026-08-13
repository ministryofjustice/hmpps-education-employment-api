package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.v2

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects

class ProfileStatusChangePutShould : ReadinessProfileV2TestCase() {
  private val knownPrisonNumber = ProfileObjects.knownPrisonNumber
  private val anotherPrisonNumber = ProfileObjects.anotherPrisonNumber
  private val unknownPrisonNumber = ProfileObjects.unknownPrisonNumber

  private val profileOfKnownPrisoner = ProfileObjects.profileOfKnownPrisoner
  private val profileOfAnotherPrisoner = ProfileObjects.profileOfAnotherPrisoner
  private val profileOfUnknownPrisoner = ProfileObjects.profileOfUnknownPrisoner

  private val createProfileRequest = ProfileObjects.createProfileJsonRequest

  private val apiDeprecatedError = "Gone: This API is no longer supported"

  @Nested
  inner class GivenNoProfileExist {
    private val prisonNumber = unknownPrisonNumber
    private val profile = profileOfUnknownPrisoner

    @Test
    fun `NOT change status of non-existing readiness profile, and return API deprecated error`() {
      val expectedError = apiDeprecatedError
      val request = parseProfile(profile.profileData).statusChangeRequestToAccepted()

      val result = assertChangeStatusIsDeprecated(prisonNumber, request)

      assertErrorMessageIsExpected(result, expectedUserMessage = expectedError)
    }
  }

  @Nested
  inner class GivenKnownProfileExists {
    private val prisonNumber = knownPrisonNumber
    private val profile = profileOfKnownPrisoner
    private val anotherProfile = profileOfAnotherPrisoner

    @BeforeEach
    internal fun setUp() {
      assertAddReadinessProfileIsOk(prisonNumber, parseProfileRequestDTO(createProfileRequest))
    }

    @Test
    fun `NOT update status of existing readiness profile, and return API deprecated error`() {
      val expectedError = apiDeprecatedError
      val request = parseProfile(profile.profileData).statusChangeRequestToDeclined()

      val result = assertChangeStatusIsDeprecated(prisonNumber, request)

      assertErrorMessageIsExpected(result, expectedUserMessage = expectedError)
    }

    @Test
    fun `NOT update status of non-existent readiness profile, and return API deprecated error`() {
      val thisPrisonNumber = anotherPrisonNumber
      val expectedError = apiDeprecatedError
      val request = parseProfile(anotherProfile.profileData).statusChangeRequestToDeclined()

      val result = assertChangeStatusIsDeprecated(thisPrisonNumber, request)

      assertErrorMessageIsExpected(result, expectedUserMessage = expectedError)
    }
  }
}
