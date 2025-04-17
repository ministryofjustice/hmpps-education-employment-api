package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.v2

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileJsonRequest

class ReadinessProfileGetShould : ReadinessProfileV2TestCase() {
  private val knownPrisonNumber = ProfileObjects.knownPrisonNumber
  private val unknownPrisonNumber = ProfileObjects.unknownPrisonNumber

  @Nested
  inner class GivenNoProfileExist {
    @Test
    fun `return error when retrieve non-existing profile`() {
      val prisonNumber = unknownPrisonNumber
      val expectedError = "Readiness profile does not exist for offender $prisonNumber"
      assertGetReadinessProfileFailed(prisonNumber, expectedUserMessage = expectedError)
    }
  }

  @Nested
  inner class GivenKnownProfileExists {
    private val prisonNumber = knownPrisonNumber
    private lateinit var knownProfileDTO: ReadinessProfileDTO

    @BeforeEach
    internal fun setUp() {
      val result = assertAddReadinessProfileIsOk(prisonNumber, parseProfileRequestDTO(createProfileJsonRequest))
      assertThat(result.body).isNotNull
      knownProfileDTO = result.body!!
    }

    @Test
    fun `retrieve existing profile`() {
      val expected = knownProfileDTO

      val result = assertGetReadinessProfileIsOk(prisonNumber)

      assertThat(result.body).isNotNull
      val actual = result.body!!
      assertEquals(prisonNumber, actual.offenderId)
      assertEquals(expected.bookingId, actual.bookingId)
      assertEquals(expected.createdBy, actual.createdBy)
      assertEquals(expected.modifiedBy, actual.modifiedBy)
      assertEquals(expected.createdDateTime, actual.createdDateTime)
      assertEquals(expected.modifiedDateTime, actual.modifiedDateTime)
      assertEquals(expected.schemaVersion, actual.schemaVersion)
      assertEquals(expected.profileData, actual.profileData)
    }
  }
}
