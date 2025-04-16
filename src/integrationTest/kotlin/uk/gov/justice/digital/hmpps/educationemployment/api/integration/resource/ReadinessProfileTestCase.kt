package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.educationemployment.api.config.ErrorResponse
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.application.ApplicationTestCase
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.NoteDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.NoteRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.StatusChangeUpdateRequestDTO

abstract class ReadinessProfileTestCase<RP, REQ>(
  protected val endpoint: String = READINESS_PROFILE_ENDPOINT,
  val responseType: Class<RP>,
  val requestTypeReference: TypeReference<REQ>,
  val profileListTypeReference: ParameterizedTypeReference<List<RP>>,
) : ApplicationTestCase() {
  protected val errorResponseType = ErrorResponse::class.java
  protected val noteRequestTypeReference by lazy { object : TypeReference<NoteRequestDTO>() {} }
  protected val noteListTypeReference by lazy { object : ParameterizedTypeReference<List<NoteDTO>>() {} }
  protected val searchUrl by lazy { "$endpoint/search" }

  @Autowired
  protected lateinit var objectMapper: ObjectMapper

  private val readWriteHeaders get() = setAuthorisationOfRoles(WR_EDIT_ROLE, WR_VIEW_ROLE)
  private val readOnlyHeaders get() = setAuthorisationOfRoles(WR_VIEW_ROLE)
  private val readOnlyRequestNoBody get() = HttpEntity<HttpHeaders>(readOnlyHeaders)

  protected fun assertAddReadinessProfileIsOk(prisonNumber: String, request: REQ) = assertAddReadinessProfileIsSuccesful(prisonNumber, request, HttpStatus.OK)
  protected fun assertAddReadinessProfileIsSuccesful(prisonNumber: String, request: REQ, expectedStatus: HttpStatus = HttpStatus.OK) = assertCreateOrUpdateReadinessProfileIsExpected(prisonNumber, HttpMethod.POST, request, responseType, expectedStatus)
  protected fun assertAddReadinessProfileFailed(prisonNumber: String, request: REQ, expectedStatus: HttpStatus = HttpStatus.BAD_REQUEST) = assertCreateOrUpdateReadinessProfileIsExpected(prisonNumber, HttpMethod.POST, request, errorResponseType, expectedStatus)

  protected fun assertUpdateReadinessProfileIsOk(prisonNumber: String, request: REQ) = assertCreateOrUpdateReadinessProfileIsExpected(prisonNumber, HttpMethod.PUT, request, responseType, HttpStatus.OK)
  protected fun assertUpdateReadinessProfileFailed(prisonNumber: String, request: REQ, expectedStatus: HttpStatus = HttpStatus.BAD_REQUEST) = assertCreateOrUpdateReadinessProfileIsExpected(prisonNumber, HttpMethod.PUT, request, errorResponseType, expectedStatus)

  private fun <R> assertCreateOrUpdateReadinessProfileIsExpected(
    prisonNumber: String,
    method: HttpMethod,
    request: REQ,
    responseClass: Class<R>,
    expectedStatus: HttpStatus,
  ) = assertRequest(profileUrl(prisonNumber), method, HttpEntity(request, readWriteHeaders), responseClass, expectedStatus)

  protected fun assertGetReadinessProfileIsOk(prisonNumber: String): ResponseEntity<RP> = assertGetReadinessProfileIsExpected(prisonNumber, responseType, HttpStatus.OK)
  protected fun assertGetReadinessProfileFailed(
    prisonNumber: String,
    expectedStatus: HttpStatus = HttpStatus.BAD_REQUEST,
    expectedUserMessage: String? = null,
    expectedDeveloperMessage: String? = null,
  ): ResponseEntity<ErrorResponse> = assertGetReadinessProfileIsExpected(prisonNumber, errorResponseType, expectedStatus)
    .also { assertErrorMessageIsExpected(it, expectedStatus, expectedUserMessage, expectedDeveloperMessage) }

  private fun <R> assertGetReadinessProfileIsExpected(
    prisonNumber: String,
    responseClass: Class<R>,
    expectedStatus: HttpStatus,
  ): ResponseEntity<R> = assertRequest(profileUrl(prisonNumber), HttpMethod.GET, readOnlyRequestNoBody, responseClass, expectedStatus)

  protected fun assertSearchReadinessProfileIsOk(prisonNumber: String) = assertSearchReadinessProfileIsOk(listOf(prisonNumber))
  protected fun assertSearchReadinessProfileIsOk(prisonNumbers: List<String>): ResponseEntity<List<RP>> = assertRequest(
    searchUrl,
    HttpMethod.POST,
    HttpEntity(prisonNumbers, readOnlyHeaders),
    profileListTypeReference,
    HttpStatus.OK,
  )

  protected fun assertChangeStatusIsOk(prisonNumber: String, request: StatusChangeUpdateRequestDTO) = assertChangeStatusIsExpected(prisonNumber, request, responseType, HttpStatus.OK)
  protected fun assertChangeStatusFailed(prisonNumber: String, request: StatusChangeUpdateRequestDTO) = assertChangeStatusIsExpected(prisonNumber, request, errorResponseType, HttpStatus.BAD_REQUEST)
  private fun <R> assertChangeStatusIsExpected(
    prisonNumber: String,
    request: StatusChangeUpdateRequestDTO,
    responseClass: Class<R>,
    expectedStatus: HttpStatus,
  ): ResponseEntity<R> = assertRequest(profileStatusUrl(prisonNumber), HttpMethod.PUT, HttpEntity(request, readWriteHeaders), responseClass, expectedStatus)

  protected fun assertAddNoteIsOk(prisonNumber: String, attribute: ActionTodo, noteText: String) = assertAddNoteIsOk(prisonNumber, attribute.name, noteText)
  protected fun assertAddNoteIsOk(prisonNumber: String, attribute: String, noteText: String) = assertRequest(
    notesUrl(prisonNumber, attribute),
    HttpMethod.POST,
    HttpEntity(makeNoteRequestDTO(noteText), readWriteHeaders),
    noteListTypeReference,
    HttpStatus.OK,
  )

  protected fun assertGetNotesIsOk(prisonNumber: String, attribute: ActionTodo): ResponseEntity<out List<NoteDTO>> = assertGetNotesIsOk(prisonNumber, attribute.name)
  protected fun assertGetNotesIsOk(prisonNumber: String, attribute: String): ResponseEntity<out List<NoteDTO>> = assertRequest(
    notesUrl(prisonNumber, attribute),
    HttpMethod.GET,
    readOnlyRequestNoBody,
    noteListTypeReference,
    HttpStatus.OK,
  )

  protected fun assertGetNotesFailed(prisonNumber: String, attribute: ActionTodo) = assertGetNotesFailed(prisonNumber, attribute.name)
  protected fun assertGetNotesFailed(prisonNumber: String, attribute: String) = assertRequest(
    notesUrl(prisonNumber, attribute),
    HttpMethod.GET,
    readOnlyRequestNoBody,
    errorResponseType,
    HttpStatus.BAD_REQUEST,
  )

  protected fun assertErrorMessageIsExpected(
    result: ResponseEntity<ErrorResponse>,
    expectedStatus: HttpStatus? = null,
    expectedUserMessage: String? = null,
    expectedDeveloperMessage: String? = null,
  ) {
    val errorResponse = result.body.also { assertNotNull(it) }!!
    with(errorResponse) {
      expectedStatus?.let { assertThat(status).isEqualTo(expectedStatus.value()) }
      expectedUserMessage?.let { assertThat(userMessage).isEqualTo(expectedUserMessage) }
      expectedDeveloperMessage?.let { assertThat(developerMessage).isEqualTo(expectedDeveloperMessage) }
    }
  }

  protected fun makeUrl(path: String, requestParams: Map<String, Any>) = "$path${
    when {
      requestParams.isNotEmpty() -> requestParams.entries.joinToString(separator = "&", prefix = "?")
      else -> ""
    }
  }"

  protected fun parseProfileRequestDTO(profileJson: String) = objectMapper.readValue(profileJson, requestTypeReference)

  protected fun parseProfileNoteDTO(notesJson: String) = objectMapper.readValue(notesJson, noteRequestTypeReference)

  private fun makeNoteRequestDTO(noteText: String) = noteText.replace("\"", "\\\"").let { "{\"text\": \"$it\"}" }
    .let { noteTextJson -> parseProfileNoteDTO(noteTextJson) }

  private fun profileUrl(prisonNumber: String) = "/$endpoint/$prisonNumber"
  private fun profileStatusUrl(prisonNumber: String) = "/$endpoint/status-change/$prisonNumber"
  private fun notesUrl(prisonNumber: String, attribute: String) = "/$READINESS_PROFILE_ENDPOINT/$prisonNumber/notes/$attribute"

  protected fun Any.asJson(): String = objectMapper.writeValueAsString(this)
}

const val READINESS_PROFILE_ENDPOINT = "/readiness-profiles"
const val WR_EDIT_ROLE = "ROLE_WORK_READINESS_EDIT"
const val WR_VIEW_ROLE = "ROLE_WORK_READINESS_VIEW"
