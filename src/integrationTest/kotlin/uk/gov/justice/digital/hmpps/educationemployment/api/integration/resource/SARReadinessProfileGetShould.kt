package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import com.fasterxml.jackson.databind.JsonNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.data.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.anotherPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.bookingIdOfKnownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.knownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.knownnCaseReferenceNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.makeProfileRequestOfAnotherPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileJsonOfAnotherPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileJsonOfKnownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileOfAnotherPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileRequestOfKnownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.unknownPrisonNumber
import java.time.LocalDate

class SARReadinessProfileGetShould : SARReadinessProfileTestCase() {
  @AfterEach
  fun tearDown() {
    readinessProfileRepository.deleteAll()
  }

  @Nested
  @DisplayName("Given an unknown prisoner without readiness profile")
  inner class GivenAnUnknownPrisoner {
    @Test
    fun `reply 204 (No Content), when requesting a SAR with unknown prisoner, and PRN is provided`() {
      assertGetSARResponseStatusAndBody(expectedStatusCode = HttpStatus.NO_CONTENT, prn = unknownPrisonNumber)
    }

    @Test
    fun `reply 209 (Subject Identifier is not recognised by this service), when requesting a SAR with CRN only`() {
      assertGetSARResponseStatusAndBody(expectedStatusCodeValue = 209, crn = knownnCaseReferenceNumber)
    }
  }

  @Nested
  @DisplayName("Given different role(s) or access(es) has/have been provided")
  inner class GivenDifferentRolesOrAccess {
    @Test
    fun `reply 401 (Unauthorized), when requesting a SAR without authorization`() {
      assertGetSARResponseStatusAndBody(expectedStatusCode = HttpStatus.UNAUTHORIZED, authorised = false)
    }

    @Test
    fun `reply 403 (Forbidden), when requesting a SAR without required role`() {
      assertGetSARResponseStatusAndBody(
        expectedStatusCode = HttpStatus.FORBIDDEN,
        expectedBody = """
          {"status":403,"errorCode":null,"userMessage":"Authentication problem. Check token and roles - Access Denied","developerMessage":"Access Denied","moreInfo":null}
        """.trimIndent(),
        roles = listOf(INCORRECT_SAR_ROLE),
      )
    }
  }

  @Nested
  @DisplayName("Given the known readiness profile")
  inner class GivenTheKnownProfile {
    @Test
    fun `reply 200 (Ok), when requesting SAR with a profile of known prisoner, and PRN is provided`() {
      val prisonNumber = givenTheKnownProfile().offenderId

      val sarResult = assertGetSARResponseIsOk(prn = prisonNumber)
      assertThat(sarResult.body).isNotNull
    }

    @Test
    fun `reply 200 (OK), when requesting a SAR with required role and more irrelevant roles`() {
      val prisonNumber = givenTheKnownProfile().offenderId

      assertGetSARResponseIsOk(prn = prisonNumber, roles = listOf(SAR_ROLE, WR_VIEW_ROLE, WR_EDIT_ROLE))
    }

    @Test
    fun `reply 200 (Ok) and data is put inside content, when requesting SAR with known prisoner's PRN`() {
      val prisonNumber = givenTheKnownProfile(withNotes = true).offenderId
      val expectedProfile = profileJsonOfKnownPrisonNumber

      val sarResult = assertGetSARResponseIsOk(expectedProfileAsJson = expectedProfile, prn = prisonNumber)

      assertThat(sarResult.body).isNotNull
      val json = objectMapper.readTree(sarResult.body!!.asJson())
      val jsonContent = json.findPath("content")
      assertThat(jsonContent.isMissingNode).isFalse()
      assertThat(jsonContent.get("offenderId").textValue()).isEqualTo(prisonNumber)
      assertThat(jsonContent.get("bookingId").longValue()).isEqualTo(bookingIdOfKnownPrisonNumber)
      jsonContent.findPath("profileData").let { jsonProfile ->
        assertThat(jsonProfile.isMissingNode).isFalse()
        assertThat(jsonProfile.get("supportDeclined")).isNotEmpty
      }
      jsonContent.findPath("noteData").let { jsonNotes ->
        assertThat(jsonNotes.isMissingNode).isFalse()
        assertThat(jsonNotes).hasSize(3)
      }
    }
  }

  @Nested
  @DisplayName("Given another readiness profile")
  inner class GivenAnotherProfile {
    private lateinit var expectedProfileDTO: ReadinessProfileDTO
    private lateinit var expectedPrisonNumber: String
    private lateinit var expectedProfileJson: JsonNode
    private lateinit var expectedProfileData: Profile

    @BeforeEach
    fun beforeEach() {
      expectedProfileDTO = givenAnotherProfileWithDeclinedHistory()
      expectedPrisonNumber = expectedProfileDTO.offenderId
      expectedProfileJson = profileJsonOfAnotherPrisonNumber
      expectedProfileData = profileOfAnotherPrisonNumber
    }

    @Nested
    @DisplayName("And period filter (fromDate, toDate) has been set")
    inner class AndPeriodFilterHasBeenSet {
      private val today = LocalDate.now()
      private val tomorrow = today.plusDays(1)
      private val yesterday = today.minusDays(1)

      @Test
      fun `reply 200(OK), when requesting a SAR with specified period`() {
        val prisonNumber = expectedPrisonNumber
        val expectedProfile = expectedProfileJson

        val sarResult = assertGetSARResponseIsOk(
          expectedProfileAsJson = expectedProfile,
          prn = prisonNumber,
          fromDate = today,
          toDate = tomorrow,
        )
        assertThat(sarResult.body).isNotNull
      }

      @Test
      fun `reply 200(OK) with history, when requesting a SAR with specified period`() {
        val prisonNumber = expectedPrisonNumber
        val expectedHistorySize = expectedProfileData.supportDeclined_history!!.size

        val sarResult = assertGetSARResponseIsOk(prn = prisonNumber, fromDate = today, toDate = tomorrow)
        sarResult.body!!.content.profileData.let {
          assertThat(it.supportDeclined_history).isNotNull.hasSize(expectedHistorySize)
        }
      }

      @Test
      fun `reply 204(No Content), when requesting a SAR with specified period`() {
        val prisonNumber = expectedPrisonNumber

        assertGetSARResponseStatusAndBody(
          expectedStatusCode = HttpStatus.NO_CONTENT,
          prn = prisonNumber,
          fromDate = null,
          toDate = yesterday,
        )
      }

      @Test
      fun `reply 400(Bad Request), when requesting a SAR with invalid date range`() {
        val expectedErrorBody = "fromDate ($tomorrow) cannot be after toDate ($today)".let { errorMessage ->
          """
            {"status":400,"errorCode":null,"userMessage":"$errorMessage","developerMessage":"$errorMessage","moreInfo":null}
          """.trimIndent()
        }

        assertGetSARResponseStatusAndBody(
          expectedStatusCode = HttpStatus.BAD_REQUEST,
          expectedBody = expectedErrorBody,
          fromDate = tomorrow,
          toDate = today,
          prn = knownPrisonNumber,
        )
      }
    }
  }

  private fun givenTheKnownProfile(withNotes: Boolean = false): ReadinessProfileDTO {
    val prisonNumber = knownPrisonNumber
    val addProfileResult = assertAddReadinessProfileIsOk(prisonNumber, profileRequestOfKnownPrisonNumber)
    if (withNotes) {
      assertAddNoteIsOk(prisonNumber, ActionTodo.DISCLOSURE_LETTER, "disclosure letter is missing")
      assertAddNoteIsOk(prisonNumber, ActionTodo.ID, "ID document is not yet ready")
      assertAddNoteIsOk(prisonNumber, ActionTodo.INTERVIEW_CLOTHING, "Need to buy some clothes for interview")
    }
    return addProfileResult.body!!
  }

  private fun givenAnotherProfileWithDeclinedHistory(): ReadinessProfileDTO {
    val prisonNumber = anotherPrisonNumber
    val request = makeProfileRequestOfAnotherPrisonNumber()
    var result = assertAddReadinessProfileIsOk(prisonNumber, request)
    repeat(6) { times ->
      request.profileData.supportDeclined!!.let {
        request.profileData.supportDeclined =
          it.copy(supportToWorkDeclinedReasonOther = "modified the n-th (${times + 1}) times")
      }
      result = assertUpdateReadinessProfileIsOk(prisonNumber, request)
    }
    return result.body!!
  }
}
