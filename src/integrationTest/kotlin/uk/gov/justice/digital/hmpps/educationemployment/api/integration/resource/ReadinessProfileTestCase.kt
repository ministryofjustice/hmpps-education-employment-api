package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.application.ApplicationTestCase
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.NoteDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.NoteRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.ReadinessProfileRequestDTO

abstract class ReadinessProfileTestCase : ApplicationTestCase() {
  @Autowired
  protected lateinit var objectMapper: ObjectMapper

  protected fun assertAddReadinessProfileIsOk(prisonNumber: String, request: ReadinessProfileRequestDTO) = assertAddReadinessProfileIsCreated(prisonNumber, request, HttpStatus.OK)

  protected fun assertAddReadinessProfileIsCreated(
    prisonNumber: String,
    request: ReadinessProfileRequestDTO,
    expectedStatus: HttpStatus = HttpStatus.CREATED,
  ): ResponseEntity<ReadinessProfileDTO> {
    val result = restTemplate.exchange(
      "$READINESS_PROFILE_ENDPOINT/$prisonNumber",
      HttpMethod.POST,
      HttpEntity(request, setAuthorisation(roles = listOf(WR_EDIT_ROLE, WR_VIEW_ROLE))),
      ReadinessProfileDTO::class.java,
    )
    assertThat(result).isNotNull
    assertThat(result.statusCode).isEqualTo(expectedStatus)
    return result
  }

  protected fun assertUpdateReadinessProfileIsOk(
    prisonNumber: String,
    request: ReadinessProfileRequestDTO,
  ): ResponseEntity<ReadinessProfileDTO> {
    val result = restTemplate.exchange(
      "$READINESS_PROFILE_ENDPOINT/$prisonNumber",
      HttpMethod.PUT,
      HttpEntity(request, setAuthorisation(roles = listOf(WR_EDIT_ROLE, WR_VIEW_ROLE))),
      ReadinessProfileDTO::class.java,
    )
    assertThat(result).isNotNull
    assertThat(result.statusCode).isEqualTo(HttpStatus.OK)

    return result
  }

  protected fun assertAddNoteIsOk(prisonNumber: String, attribute: ActionTodo, noteText: String) = assertAddNoteIsOk(prisonNumber, attribute.toString(), noteText)

  protected fun assertAddNoteIsOk(
    prisonNumber: String,
    attribute: String,
    noteText: String,
  ): ResponseEntity<List<NoteDTO>> {
    val noteRequestDTO = noteText.replace("\"", "\\\"").let { "{\"text\": \"$it\"}" }
      .let { noteTextJson -> objectMapper.readValue(noteTextJson, object : TypeReference<NoteRequestDTO>() {}) }
    val result = restTemplate.exchange(
      "/$READINESS_PROFILE_ENDPOINT/$prisonNumber/notes/$attribute",
      HttpMethod.POST,
      HttpEntity(noteRequestDTO, setAuthorisation(roles = listOf(WR_EDIT_ROLE))),
      object : ParameterizedTypeReference<List<NoteDTO>>() {},
    )
    assertThat(result).isNotNull
    assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
    return result!!
  }

  protected fun makeUrl(path: String, requestParams: Map<String, Any>) = "$path${
    when {
      requestParams.isNotEmpty() -> requestParams.entries.joinToString(separator = "&", prefix = "?")
      else -> ""
    }
  }"

  protected fun Any.asJson(): String = objectMapper.writeValueAsString(this)
}

private const val READINESS_PROFILE_ENDPOINT = "/readiness-profiles"
val WR_EDIT_ROLE = "ROLE_WORK_READINESS_EDIT"
val WR_VIEW_ROLE = "ROLE_WORK_READINESS_VIEW"
