package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.v2

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileJsonRequest

class ReadinessProfilePostShould : ReadinessProfileV2TestCase() {
  private val knownPrisonNumber = ProfileObjects.knownPrisonNumber
  private val anotherPrisonNumber = ProfileObjects.anotherPrisonNumber

  @Nested
  inner class GivenNoProfileExist {
    private val prisonNumber = knownPrisonNumber

    @Test
    fun `create readiness profile`() {
      val request = parseProfileRequestDTO(createProfileJsonRequest)
      assertAddReadinessProfileIsOk(prisonNumber, request)
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
    fun `NOT re-create existing readiness profile, and return error`() {
      val expectedError = "Readiness profile already exists for offender $prisonNumber"
      val request = parseProfileRequestDTO(createProfileJsonRequest)

      val result = assertAddReadinessProfileFailed(prisonNumber, request)

      assertErrorMessageIsExpected(result, expectedUserMessage = expectedError)
    }

    @Test
    fun `create another readiness profile, that is new and yet exist`() {
      val prisonNumber = anotherPrisonNumber
      val request = parseProfileRequestDTO(createProfileJsonRequest)

      assertAddReadinessProfileIsOk(prisonNumber, request)
    }
  }
}
