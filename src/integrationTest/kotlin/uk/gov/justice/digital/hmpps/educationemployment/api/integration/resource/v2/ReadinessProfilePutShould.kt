package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.v2

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus.NO_RIGHT_TO_WORK
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus.SUPPORT_DECLINED
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus.SUPPORT_NEEDED
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileJsonRequest

class ReadinessProfilePutShould : ReadinessProfileV2TestCase() {
  private val knownPrisonNumber = ProfileObjects.knownPrisonNumber
  private val anotherPrisonNumber = ProfileObjects.anotherPrisonNumber
  private val unknownPrisonNumber = ProfileObjects.unknownPrisonNumber

  @Nested
  inner class GivenNoProfileExist {
    private val prisonNumber = unknownPrisonNumber

    @Test
    fun `NOT update non-existing readiness profile, and return error`() {
      val expectedError = "Readiness profile does not exist for offender $prisonNumber"
      val request = parseProfileRequestDTO(createProfileJsonRequest)

      val result = assertUpdateReadinessProfileFailed(prisonNumber, request)

      assertErrorMessageIsExpected(result, expectedUserMessage = expectedError)
    }

    @Test
    fun `NOT change status of non-existing readiness profile, and return error`() {
      val expectedError = "Readiness profile does not exist for offender $prisonNumber"
      val profile = ProfileObjects.profileOfUnknownPrisoner
      val request = parseProfile(profile.profileData)
        .apply { status = SUPPORT_NEEDED }
        .toUpdateRequest(profile.bookingId)

      val result = assertUpdateReadinessProfileFailed(prisonNumber, request)

      assertErrorMessageIsExpected(result, expectedUserMessage = expectedError)
    }
  }

  @Nested
  inner class GivenKnownProfileExists {
    private val prisonNumber = knownPrisonNumber
    private val profile = ProfileObjects.profileOfKnownPrisoner

    @BeforeEach
    internal fun setUp() {
      assertAddReadinessProfileIsOk(prisonNumber, parseProfileRequestDTO(createProfileJsonRequest))
    }

    @Test
    fun `update readiness profile`() {
      val request = parseProfileRequestDTO(createProfileJsonRequest)
      assertUpdateReadinessProfileIsOk(prisonNumber, request)
    }

    @Test
    fun `NOT update another readiness profile, that is new and yet exist`() {
      val expectedError = "Readiness profile does not exist for offender $anotherPrisonNumber"
      val request = parseProfileRequestDTO(createProfileJsonRequest)

      val result = assertUpdateReadinessProfileFailed(anotherPrisonNumber, request)

      assertErrorMessageIsExpected(result, expectedUserMessage = expectedError)
    }

    @Test
    fun `update status of readiness profile`() {
      val request = parseProfile(profile.profileData)
        .apply { status = SUPPORT_DECLINED }
        .toUpdateRequest(profile.bookingId)

      assertUpdateReadinessProfileIsOk(prisonNumber, request)
    }

    @Test
    fun `NOT update status of another readiness profile, that is new and yet exist`() {
      val anotherProfile = ProfileObjects.profileOfAnotherPrisoner
      val expectedError = "Readiness profile does not exist for offender $anotherPrisonNumber"
      val request = parseProfile(anotherProfile.profileData)
        .apply { status = NO_RIGHT_TO_WORK }
        .toUpdateRequest(profile.bookingId)

      val result = assertUpdateReadinessProfileFailed(anotherPrisonNumber, request)

      assertErrorMessageIsExpected(result, expectedUserMessage = expectedError)
    }
  }
}
