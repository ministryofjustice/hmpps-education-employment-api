package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.config.ErrorResponse
import uk.gov.justice.digital.hmpps.educationemployment.api.data.NoteRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.data.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.data.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.util.TestData

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ReadinessProfileIntTest : IntegrationTestBase() {

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @AfterEach
  fun tearDown() {
    readinessProfileRepository.deleteAll()
  }

  @Test
  fun `Get the exception for profile for a unknown offender`() {
    val result = restTemplate.exchange("/readiness-profiles/A1234AB", HttpMethod.GET, HttpEntity<HttpHeaders>(setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), ErrorResponse::class.java)
    assertThat(result).isNotNull
    val erroResponse = result.body
    assert(erroResponse.status.equals(HttpStatus.BAD_REQUEST.value()))
    assert(erroResponse.userMessage.equals("Readiness profile does not exist for offender A1234AB"))
  }

  @Test
  fun `Post a profile for an offender`() {
    val actualReadinessProfileRequestDTO = objectMapper.readValue(
      TestData.createProfileJsonRequest,
      object : TypeReference<ReadinessProfileRequestDTO>() {},
    )
    val result = restTemplate.exchange("/readiness-profiles/A1234AB", HttpMethod.POST, HttpEntity<ReadinessProfileRequestDTO>(actualReadinessProfileRequestDTO, setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), ReadinessProfileDTO::class.java)
    assertThat(result).isNotNull
  }

  @Test
  fun `Update a profile for an offender`() {
    val actualReadinessProfileRequestDTO = objectMapper.readValue(
      TestData.createProfileJsonRequest,
      object : TypeReference<ReadinessProfileRequestDTO>() {},
    )
    restTemplate.exchange("/readiness-profiles/A1234AC", HttpMethod.POST, HttpEntity<ReadinessProfileRequestDTO>(actualReadinessProfileRequestDTO, setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), ReadinessProfileDTO::class.java)

    val result = restTemplate.exchange("/readiness-profiles/A1234AC", HttpMethod.PUT, HttpEntity<ReadinessProfileRequestDTO>(actualReadinessProfileRequestDTO, setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), ReadinessProfileDTO::class.java)
    assertThat(result).isNotNull
    assert(result.statusCode.is2xxSuccessful)
  }

  @Test
  fun `Get a profile for an offender`() {
    val actualReadinessProfileRequestDTO = objectMapper.readValue(
      TestData.createProfileJsonRequest,
      object : TypeReference<ReadinessProfileRequestDTO>() {},
    )

    restTemplate.exchange("/readiness-profiles/A1234BB", HttpMethod.POST, HttpEntity<ReadinessProfileRequestDTO>(actualReadinessProfileRequestDTO, setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), ReadinessProfileDTO::class.java)
    val getResult = restTemplate.exchange("/readiness-profiles/A1234BB", HttpMethod.GET, HttpEntity<ReadinessProfileRequestDTO>(actualReadinessProfileRequestDTO, setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), ReadinessProfileDTO::class.java)
    assertThat(getResult).isNotNull
    getResult.statusCode.is2xxSuccessful
  }

  @Test
  fun `Get an empty list when a profile note is not present for an offender`() {
    val actualReadinessProfileRequestDTO = objectMapper.readValue(
      TestData.createProfileJsonRequest,
      object : TypeReference<ReadinessProfileRequestDTO>() {},
    )
    restTemplate.exchange("/readiness-profiles/A1234AE", HttpMethod.POST, HttpEntity<ReadinessProfileRequestDTO>(actualReadinessProfileRequestDTO, setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), ReadinessProfileDTO::class.java)

    val result = restTemplate.exchange("/readiness-profiles/A1234AE/notes/DISCLOSURE_LETTER", HttpMethod.GET, HttpEntity<HttpHeaders>(setAuthorisation("lest", roles = listOf("ROLE_WORK_READINESS_VIEW"))), List::class.java)
    assert(result != null)
    assert(result.statusCode.is2xxSuccessful)
    assert(result.body.isEmpty())
  }

  @Test
  fun `Post a profile note for an offender`() {
    val actualReadinessProfileRequestDTO = objectMapper.readValue(
      TestData.createProfileJsonRequest,
      object : TypeReference<ReadinessProfileRequestDTO>() {},
    )
    restTemplate.exchange("/readiness-profiles/A1234AT", HttpMethod.POST, HttpEntity<ReadinessProfileRequestDTO>(actualReadinessProfileRequestDTO, setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), ReadinessProfileDTO::class.java)
    val noteRequestDTO = objectMapper.readValue(
      TestData.noteFreeTextJson,
      object : TypeReference<NoteRequestDTO>() {},
    )
    val result = restTemplate.exchange("/readiness-profiles/A1234AT/notes/DISCLOSURE_LETTER", HttpMethod.POST, HttpEntity<NoteRequestDTO>(noteRequestDTO, setAuthorisation("lest", roles = listOf("ROLE_WORK_READINESS_EDIT"))), List::class.java)
    assertThat(result).isNotNull
    assert(result.statusCode.is2xxSuccessful)
    assert(result.body.size > 0)
  }

  @Test
  fun `Retrieve a profile note for an offender`() {
    val actualReadinessProfileRequestDTO = objectMapper.readValue(
      TestData.createProfileJsonRequest,
      object : TypeReference<ReadinessProfileRequestDTO>() {},
    )
    restTemplate.exchange("/readiness-profiles/A1234AZ", HttpMethod.POST, HttpEntity<ReadinessProfileRequestDTO>(actualReadinessProfileRequestDTO, setAuthorisation(roles = listOf("ROLE_WORK_READINESS_EDIT", "ROLE_WORK_READINESS_VIEW"))), ReadinessProfileDTO::class.java)
    val noteRequestDTO = objectMapper.readValue(
      TestData.noteFreeTextJson,
      object : TypeReference<NoteRequestDTO>() {},
    )
    val result = restTemplate.exchange("/readiness-profiles/A1234AZ/notes/DISCLOSURE_LETTER", HttpMethod.POST, HttpEntity<NoteRequestDTO>(noteRequestDTO, setAuthorisation("lest", roles = listOf("ROLE_WORK_READINESS_EDIT"))), List::class.java)
    assertThat(result).isNotNull
    assert(result.statusCode.is2xxSuccessful)
    assert(result.body.size > 0)

    val noteList = restTemplate.exchange("/readiness-profiles/A1234AZ/notes/DISCLOSURE_LETTER", HttpMethod.GET, HttpEntity<HttpHeaders>(setAuthorisation("lest", roles = listOf("ROLE_WORK_READINESS_EDIT"))), List::class.java)
    assertThat(result).isNotNull
    assert(result.statusCode.is2xxSuccessful)
    assert(result.body.size > 0)
  }
}
