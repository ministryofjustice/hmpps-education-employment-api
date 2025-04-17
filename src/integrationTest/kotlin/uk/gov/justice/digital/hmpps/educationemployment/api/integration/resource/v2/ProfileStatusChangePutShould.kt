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

  @Nested
  inner class GivenNoProfileExist {
    private val prisonNumber = unknownPrisonNumber
    private val profile = profileOfUnknownPrisoner

    @Test
    fun `NOT change status of non-existing readiness profile, and return error`() {
      val expectedError = "Readiness profile does not exist for offender $prisonNumber"
      val request = parseProfile(profile.profileData).statusChangeRequestToAccepted()

      val result = assertChangeStatusFailed(prisonNumber, request)

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
    fun `update status of readiness profile`() {
      val request = parseProfile(profile.profileData).statusChangeRequestToDeclined()

      assertChangeStatusIsOk(prisonNumber, request)
    }

    @Test
    fun `NOT update status of another readiness profile, that is new and yet exist`() {
      val thisPrisonNumber = anotherPrisonNumber
      val expectedError = "Readiness profile does not exist for offender $thisPrisonNumber"
      val request = parseProfile(anotherProfile.profileData).statusChangeRequestToDeclined()

      val result = assertChangeStatusFailed(thisPrisonNumber, request)

      assertErrorMessageIsExpected(result, expectedUserMessage = expectedError)
    }
  }
}
