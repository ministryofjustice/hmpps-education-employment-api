package uk.gov.justice.digital.hmpps.educationemploymentapi.integration.health

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.educationemploymentapi.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InfoIntTest : IntegrationTestBase() {

  @Test
  fun `Info page is accessible`() {
    val result = restTemplate.exchange("/info", HttpMethod.GET, HttpEntity<HttpHeaders>(setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), String::class.java)
    assert(result != null)
    assert(result.hasBody())
    assert(result.statusCode.is2xxSuccessful)
    var stringcompanion = JacksonUtil.toJsonNode(result.body.toString())
    var name = stringcompanion.get("build").get("name")
    Assertions.assertThat(name.asText().toString()).isEqualTo("hmpps-education-employment-api")
  }

  @Test
  fun `Info page reports version`() {
    val result = restTemplate.exchange("/info", HttpMethod.GET, HttpEntity<HttpHeaders>(setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), String::class.java)
    assert(result != null)
    assert(result.hasBody())
    assert(result.statusCode.is2xxSuccessful)
    var stringcompanion = JacksonUtil.toJsonNode(result.body.toString())
    var version = stringcompanion.get("build").get("version")
    Assertions.assertThat(version.asText().toString()).startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
  }
}
