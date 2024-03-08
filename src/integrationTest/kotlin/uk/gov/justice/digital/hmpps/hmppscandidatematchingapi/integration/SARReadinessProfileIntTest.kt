package uk.gov.justice.digital.hmpps.educationemploymentapi.integration

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.educationemploymentapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.SARReadinessProfileDTO
import uk.gov.justice.digital.hmpps.hmppscandidatematchingapi.integration.TestData

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SARReadinessProfileIntTest : IntegrationTestBase() {
  @Autowired
  lateinit var objectMapper: ObjectMapper

  @Test
  fun `Get the exception for profile for a unknown offender when requesting  a SAR `() {
    val result = restTemplate.exchange("/subject-access-request/A1234BD", HttpMethod.GET, HttpEntity<HttpHeaders>(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS"))), ErrorResponse::class.java)
    assertThat(result).isNotNull
    val erroResponse = result.body
    assert(erroResponse.status.equals(HttpStatus.BAD_REQUEST.value()))
    assert(erroResponse.userMessage.equals("Readiness profile does not exist for offender A1234BD"))
  }

  @Test
  fun `Get a profile for an offender For SAR`() {
    val actualReadinessProfileRequestDTO = objectMapper.readValue(
      TestData.createProfileJsonRequest,
      object : TypeReference<ReadinessProfileRequestDTO>() {},
    )
    val result = restTemplate.exchange("/readiness-profiles/A1234BB", HttpMethod.POST, HttpEntity<ReadinessProfileRequestDTO>(actualReadinessProfileRequestDTO, setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), ReadinessProfileDTO::class.java)
    assertThat(result).isNotNull
    val sarResult = restTemplate.exchange("/subject-access-request/A1234BB", HttpMethod.GET, HttpEntity<HttpHeaders>(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS"))), SARReadinessProfileDTO::class.java)
    assertThat(sarResult).isNotNull
    sarResult.statusCode.is2xxSuccessful
  }
}
