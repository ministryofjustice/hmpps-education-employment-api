package uk.gov.justice.digital.hmpps.educationemploymentapi.integration.health

import io.hypersistence.utils.hibernate.type.json.internal.JacksonUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.educationemploymentapi.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HealthCheckIntTest : IntegrationTestBase() {

  @Test
  fun `Health page reports ok`() {
    val result = restTemplate.getForEntity("/health", String.javaClass)
    assert(result != null)
    assert(result.hasBody())
    assert(result.statusCode.is2xxSuccessful)
  }

  @Test
  fun `Health info reports version`() {
    val result = restTemplate.exchange("/health", HttpMethod.GET, HttpEntity<HttpHeaders>(setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), String::class.java)
    assert(result != null)
    assert(result.hasBody())
    assert(result.statusCode.is2xxSuccessful)
    var stringcompanion = JacksonUtil.toJsonNode(result.body.toString())
    var version = stringcompanion.get("components").get("healthInfo").get("details").get("version")
    assertThat(version.asText().toString()).startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
  }

  @Test
  fun `Health ping page is accessible`() {
    val result = restTemplate.exchange("/health/ping", HttpMethod.GET, HttpEntity<HttpHeaders>(setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), String::class.java)
    assert(result != null)
    assert(result.hasBody())
    assert(result.statusCode.is2xxSuccessful)
    var stringcompanion = JacksonUtil.toJsonNode(result.body.toString())
    var status = stringcompanion.get("status")
    assertThat(status.asText().toString()).isEqualTo("UP")
  }

  @Test
  fun `readiness reports ok`() {
    val result = restTemplate.exchange("/health/readiness", HttpMethod.GET, HttpEntity<HttpHeaders>(setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), String::class.java)
    assert(result != null)
    assert(result.hasBody())
    assert(result.statusCode.is2xxSuccessful)
    var stringcompanion = JacksonUtil.toJsonNode(result.body.toString())
    var status = stringcompanion.get("status")
    assertThat(status.asText().toString()).isEqualTo("UP")
  }

  @Test
  fun `liveness reports ok`() {
    val result = restTemplate.exchange("/health/liveness", HttpMethod.GET, HttpEntity<HttpHeaders>(setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), String::class.java)
    assert(result != null)
    assert(result.hasBody())
    assert(result.statusCode.is2xxSuccessful)
    var stringcompanion = JacksonUtil.toJsonNode(result.body.toString())
    var status = stringcompanion.get("status")
    assertThat(status.asText().toString()).isEqualTo("UP")
  }
}
