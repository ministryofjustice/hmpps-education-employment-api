package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.anotherPRN
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.bookingIdOfKnownPRN
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.knownCRN
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.knownPRN
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileJsonOfKnownPRN
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileRequestOfAnotherPRN
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileRequestOfKnownPRN
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.unknownPRN
import java.time.LocalDate

class SARReadinessProfileGetShould : SARReadinessProfileTestCase() {
  @AfterEach
  fun tearDown() {
    readinessProfileRepository.deleteAll()
  }

  @Test
  fun `reply 204 (No Content), when requesting a SAR with unknown prisoner, and PRN is provided`() {
    assertGetSARResponseStatusAndBody(expectedStatusCode = HttpStatus.NO_CONTENT, prn = unknownPRN)
  }

  @Test
  fun `reply 200 (Ok), when requesting SAR with a profile of known prisoner, and PRN is provided`() {
    val prisonNumber = knownPRN
    assertAddReadinessProfileIsOk(prisonNumber, profileRequestOfKnownPRN)

    val sarResult = assertGetSARResponseIsOk(prn = prisonNumber)
    assertThat(sarResult.body).isNotNull
  }

  @Test
  fun `reply 209 (Subject Identifier is not recognised by this service), when requesting a SAR with CRN only`() {
    assertGetSARResponseStatusAndBody(expectedStatusCodeValue = 209, crn = knownCRN)
  }

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

  @Test
  fun `reply 200 (OK), when requesting a SAR with required role and more irrelevant roles`() {
    val prisonNumber = knownPRN
    assertAddReadinessProfileIsOk(prisonNumber, profileRequestOfKnownPRN)

    assertGetSARResponseIsOk(prn = prisonNumber, roles = listOf(SAR_ROLE, WR_VIEW_ROLE, WR_EDIT_ROLE))
  }

  @Test
  fun `reply 200 (Ok) and data is put inside content, when requesting SAR with known prisoner's PRN`() {
    val prisonNumber = knownPRN
    assertAddReadinessProfileIsOk(prisonNumber, profileRequestOfKnownPRN)
    val expectedProfile = profileJsonOfKnownPRN

    val sarResult = assertGetSARResponseIsOk(expectedProfileAsJson = expectedProfile, prn = prisonNumber)

    assertThat(sarResult.body).isNotNull
    val json = objectMapper.readTree(sarResult.body!!.asJson())
    val jsonContent = json.findPath("content")
    assertThat(jsonContent.isMissingNode).isFalse()
    assertThat(jsonContent.get("offenderId").textValue()).isEqualTo(prisonNumber)
    assertThat(jsonContent.get("bookingId").longValue()).isEqualTo(bookingIdOfKnownPRN)
  }

  @Test
  fun `reply 200(OK), when requesting a SAR with specified period`() {
    val prisonNumber = knownPRN
    assertAddReadinessProfileIsOk(prisonNumber, profileRequestOfKnownPRN)
    val expectedProfile = profileJsonOfKnownPRN
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)

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
    val prisonNumber = anotherPRN
    val expectedHistorySize = 6
    profileRequestOfAnotherPRN.let { request ->
      assertAddReadinessProfileIsOk(prisonNumber, request)
      repeat(expectedHistorySize) {
        request.profileData.supportDeclined!!.let {
          request.profileData.supportDeclined =
            it.copy(supportToWorkDeclinedReasonOther = "modified the n-th ($it) times")
        }
        assertUpdateReadinessProfileIsOk(prisonNumber, request)
      }
    }
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)

    val sarResult = assertGetSARResponseIsOk(prn = prisonNumber, fromDate = today, toDate = tomorrow)
    sarResult.body!!.content.profileData.let {
      assertThat(it.supportDeclined_history).isNotNull.hasSize(expectedHistorySize)
    }
  }

  @Test
  fun `reply 204(No Content), when requesting a SAR with specified period`() {
    val prisonNumber = knownPRN
    assertAddReadinessProfileIsOk(prisonNumber, profileRequestOfKnownPRN)
    val yesterday = LocalDate.now().minusDays(1)

    assertGetSARResponseStatusAndBody(
      expectedStatusCode = HttpStatus.NO_CONTENT,
      prn = prisonNumber,
      fromDate = null,
      toDate = yesterday,
    )
  }

  @Test
  fun `reply 400(Bad Request), when requesting a SAR with invalid date range`() {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
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
      prn = knownPRN,
    )
  }
}
