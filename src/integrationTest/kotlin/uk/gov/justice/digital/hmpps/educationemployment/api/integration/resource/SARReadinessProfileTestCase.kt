package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.educationemployment.api.data.SARReadinessProfileDTO
import java.time.LocalDate

class SARReadinessProfileTestCase : ReadinessProfileTestCase() {
  protected fun assertGetSARResponseIsOk(
    prn: String,
    fromDate: LocalDate? = null,
    toDate: LocalDate? = null,
    expectedBody: String? = null,
    roles: List<String> = listOf(SAR_ROLE),
  ): ResponseEntity<SARReadinessProfileDTO> {
    val url = makeUrl(
      "/subject-access-request",
      makeRequestParamsOfSAR(prn = prn, fromDate = fromDate, toDate = toDate),
    )
    val request = HttpEntity<HttpHeaders>(setAuthorisation(roles = roles))
    val result = restTemplate.exchange(url, HttpMethod.GET, request, SARReadinessProfileDTO::class.java)

    assertThat(result).isNotNull
    assertThat(result.statusCode).isEqualTo(HttpStatus.OK)

    if (!expectedBody.isNullOrEmpty()) {
      assertThat(result.body?.asJson()).isEqualTo(expectedBody)
    }

    return result
  }

  protected fun assertGetSARResponseStatusAndBody(
    expectedStatusCode: HttpStatusCode? = null,
    expectedStatusCodeValue: Int? = null,
    expectedBody: String? = null,
    prn: String? = null,
    crn: String? = null,
    authorised: Boolean = true,
    roles: List<String> = listOf(SAR_ROLE),
  ): ResponseEntity<Any> {
    val request = (if (authorised) setAuthorisation(roles = roles) else null)?.let { HttpEntity<HttpHeaders>(it) }
    val url = makeUrl(
      "/subject-access-request",
      makeRequestParamsOfSAR(prn = prn, crn = crn),
    )
    val result = restTemplate.exchange(url, HttpMethod.GET, request, Any::class.java)

    assertThat(result).isNotNull
    expectedStatusCode?.let { assertThat(result.statusCode).isEqualTo(expectedStatusCode) }
    expectedStatusCodeValue?.let { assertThat(result.statusCode.value()).isEqualTo(expectedStatusCodeValue) }
    if (expectedBody.isNullOrEmpty()) {
      assertThat(result.body).isNull()
    } else {
      assertThat(result.body?.asJson()).isEqualTo(expectedBody)
    }
    return result
  }

  protected fun makeRequestParamsOfSAR(
    prn: String? = null,
    crn: String? = null,
    fromDate: LocalDate? = null,
    toDate: LocalDate? = null,
  ) = mutableMapOf<String, Any>().also { requestParams ->
    prn?.let { requestParams["prn"] = prn }
    crn?.let { requestParams["crn"] = crn }
    fromDate?.let { requestParams["fromDate"] = fromDate }
    toDate?.let { requestParams["toDate"] = toDate }
  }.toMap()

  protected fun SARReadinessProfileDTO.asJson(): String = objectMapper.writeValueAsString(this)
}