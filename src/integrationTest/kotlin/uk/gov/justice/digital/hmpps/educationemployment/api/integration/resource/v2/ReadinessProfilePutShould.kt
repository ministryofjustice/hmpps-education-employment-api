package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.v2

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
  }

  @Nested
  inner class GivenKnownProfileExists {
    private val prisonNumber = knownPrisonNumber

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
  }
}
