package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InfoIntTest : IntegrationTestBase() {

  @Test
  fun `Info page is accessible`() {
    val result = restTemplate.exchange("/info", HttpMethod.GET, HttpEntity<HttpHeaders>(setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), String::class.java)
    assert(result != null)
    assert(result.hasBody())
    assert(result.statusCode.is2xxSuccessful)
    val stringcompanion = CapturedSpringConfigValues.objectMapper.readTree(result.body!!)
    val name = stringcompanion.get("build").get("name")
    Assertions.assertThat(name.asText()).isEqualTo("hmpps-education-employment-api")
  }

  @Test
  fun `Info page reports version`() {
    val result = restTemplate.exchange("/info", HttpMethod.GET, HttpEntity<HttpHeaders>(setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), String::class.java)
    assert(result != null)
    assert(result.hasBody())
    assert(result.statusCode.is2xxSuccessful)
    val stringcompanion = CapturedSpringConfigValues.objectMapper.readTree(result.body!!)
    val version = stringcompanion.get("build").get("version")
    Assertions.assertThat(version.asText()).startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
  }
}
