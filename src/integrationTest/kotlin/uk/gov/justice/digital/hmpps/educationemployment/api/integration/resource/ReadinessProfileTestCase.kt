package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.data.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.data.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.IntegrationTestBase

abstract class ReadinessProfileTestCase : IntegrationTestBase() {
  @Autowired
  lateinit var objectMapper: ObjectMapper

  protected fun assertAddReadinessProfileIsOk(prisonNumber: String, request: ReadinessProfileRequestDTO) = assertAddReadinessProfileIsCreated(prisonNumber, request, HttpStatus.OK)

  protected fun assertAddReadinessProfileIsCreated(prisonNumber: String, request: ReadinessProfileRequestDTO, expectedStatus: HttpStatus = HttpStatus.CREATED) {
    val result = restTemplate.exchange(
      "/readiness-profiles/$prisonNumber",
      HttpMethod.POST,
      HttpEntity(
        request,
        setAuthorisation(roles = listOf(WR_EDIT_ROLE, WR_VIEW_ROLE)),
      ),
      ReadinessProfileDTO::class.java,
    )
    assertThat(result).isNotNull
    assertThat(result.statusCode).isEqualTo(expectedStatus)
  }

  protected fun makeUrl(path: String, requestParams: Map<String, Any>) = "$path${
    when {
      requestParams.isNotEmpty() -> requestParams.entries.joinToString(separator = "&", prefix = "?")
      else -> ""
    }
  }"

  protected fun Any.asJson(): String = objectMapper.writeValueAsString(this)
}

val WR_EDIT_ROLE = "ROLE_WORK_READINESS_EDIT"
val WR_VIEW_ROLE = "ROLE_WORK_READINESS_VIEW"
