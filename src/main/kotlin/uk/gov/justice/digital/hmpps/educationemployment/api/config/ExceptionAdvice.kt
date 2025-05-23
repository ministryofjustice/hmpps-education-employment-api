package uk.gov.justice.digital.hmpps.educationemployment.api.config

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.AuthorizationServiceException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.AlreadyExistsException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.CustomValidationException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.DeprecatedApiException
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import java.util.*

@RestControllerAdvice
class ControllerAdvice {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
    log.info("Access denied exception: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(
        ErrorResponse(
          status = HttpStatus.FORBIDDEN.value(),
          userMessage = "Authentication problem. Check token and roles - ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(AuthorizationServiceException::class)
  fun handleAuthorizationServiceException(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
    log.info("Auth service exception: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(
        ErrorResponse(
          status = HttpStatus.UNAUTHORIZED.value(),
          userMessage = "Authentication problem. Check token and roles - ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(RestClientResponseException::class)
  fun handleRestClientException(e: RestClientResponseException): ResponseEntity<ErrorResponse> {
    log.error("RestClientResponseException: ${e.message}", e)
    return ResponseEntity
      .status(e.statusCode)
      .body(
        ErrorResponse(
          status = e.statusCode,
          userMessage = "Rest client exception ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(RestClientException::class)
  fun handleRestClientException(e: RestClientException): ResponseEntity<ErrorResponse> {
    log.error("RestClientException: ${e.message}", e)
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
          userMessage = "Rest client exception ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: ValidationException): ResponseEntity<ErrorResponse> {
    log.info("Validation exception: ${e.message}", e)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse(
          status = HttpStatus.BAD_REQUEST.value(),
          userMessage = "Validation failure: ${e.message?.let { e.message!!.substring(it.indexOf(":") + 2) }}",
          developerMessage = e.message?.let { e.message!!.substring(it.indexOf(":") + 2) },
        ),
      )
  }

  @ExceptionHandler(value = [MismatchedInputException::class])
  fun handleMissingKotlinParameter(exception: MismatchedInputException): ResponseEntity<ErrorResponse> {
    val fieldName = exception.path.joinToString(separator = ".") { it.fieldName }
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse(
          status = HttpStatus.BAD_REQUEST.value(),
          userMessage = "Validation failure: $fieldName",
          developerMessage = "Missing $fieldName",
        ),
      )
  }

  @ExceptionHandler(HttpMessageNotReadableException::class)
  fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
    log.info("Validation exception: ${e.message}", e)
    val genericMessage = "Unacceptable JSON " + e.message
    var errorDetails: String? = genericMessage
    if (e.cause is MismatchedInputException) {
      val mkpx: MismatchedInputException = e.cause as MismatchedInputException
      val fieldName = mkpx.path.joinToString(separator = ".") { it.fieldName }
      errorDetails = "Missing $fieldName"
    }
    if (e.cause is InvalidFormatException) {
      val ifx: InvalidFormatException = e.cause as InvalidFormatException
      if (ifx.targetType != null && ifx.targetType.isEnum) {
        errorDetails = java.lang.String.format(
          "Invalid enum value: '%s' for the field: '%s'. The value must be one of: %s.",
          ifx.value,
          ifx.path[ifx.path.size - 1].fieldName,
          Arrays.toString(ifx.targetType.enumConstants),
        )
      }
    }
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse(
          status = HttpStatus.BAD_REQUEST.value(),
          userMessage = "Validation failure: $errorDetails",
          developerMessage = errorDetails,
        ),
      )
  }

  @ExceptionHandler(MissingServletRequestParameterException::class)
  fun handleMissingParams(e: MissingServletRequestParameterException) = "Missing required parameter: ${e.message}"
    .also { log.info(it) }
    .let { makeErrorResponse(e, userMessage = it) }

  @ExceptionHandler(MethodArgumentTypeMismatchException::class)
  fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
    log.info("Method argument type mismatch exception: ${e.message}", e)
    val errorMessage = "Type mismatch: parameter '${e.name}' with value '${e.value}'"
    return makeErrorResponse(e, userMessage = "Validation failure: $errorMessage", developerMessage = errorMessage)
  }

  @ExceptionHandler(IllegalArgumentException::class)
  fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
    log.info("IllegalArgumentException: ${e.message}", e)
    return makeErrorResponse(e)
  }

  @ExceptionHandler(NotFoundException::class)
  fun handleNotFoundException(e: NotFoundException): ResponseEntity<ErrorResponse> {
    log.info("NotFoundException: ${e.message}", e)
    return makeErrorResponse(e)
  }

  @ExceptionHandler(AlreadyExistsException::class)
  fun handleAlreadyExistsException(e: AlreadyExistsException): ResponseEntity<ErrorResponse> {
    log.info("AlreadyExistsException: ${e.message}", e)
    return makeErrorResponse(e)
  }

  @ExceptionHandler(DeprecatedApiException::class)
  fun handleDeprecatedApiException(e: DeprecatedApiException): ResponseEntity<ErrorResponse> {
    log.warn("DeprecatedApiException: ${e.message}", e)
    return HttpStatus.GONE.let {
      makeErrorResponse(e, it, userMessage = "${it.reasonPhrase}: This API is no longer supported")
    }
  }

  @ExceptionHandler(CustomValidationException::class)
  fun handleCustomValidationException(e: CustomValidationException): ResponseEntity<ErrorResponse> {
    log.info("CustomValidationException: ${e.message}", e)
    return makeErrorResponse(e, userMessage = "Validation failure: ${e.message}")
  }

  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ResponseEntity<ErrorResponse?>? {
    log.error("Unexpected exception: ${e.message}", e)
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = HttpStatus.INTERNAL_SERVER_ERROR,
          userMessage = "Unexpected error: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  private fun makeErrorResponse(
    e: Exception,
    httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
    userMessage: String? = null,
    developerMessage: String? = null,
  ) = ResponseEntity.status(httpStatus).body(
    ErrorResponse(
      status = httpStatus.value(),
      userMessage = "${userMessage ?: e.message}",
      developerMessage = "${developerMessage ?: e.message}",
    ),
  )
}

data class ErrorResponse(
  val status: Int,
  val errorCode: String? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null,
) {
  constructor(
    status: HttpStatusCode,
    errorCode: String? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null,
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo)
}
