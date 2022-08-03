package uk.gov.justice.digital.hmpps.educationemploymentapi.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import reactor.core.publisher.Mono
import javax.validation.ValidationException

@RestControllerAdvice
class ExceptionHandler {
  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: Exception): Mono<ResponseEntity<ErrorResponse>> {
    log.info("Validation exception: {}", e.message)
    return Mono.just(
      ResponseEntity
        .status(BAD_REQUEST)
        .body(
          ErrorResponse(
            status = BAD_REQUEST,
            userMessage = "Validation failure: ${e.message}",
            developerMessage = e.message
          )
        )
    )
  }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException): Mono<ResponseEntity<ErrorResponse>> {
    log.debug("Forbidden (403) returned with message {}", e.message)
    return Mono.just(
      ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(ErrorResponse(status = (HttpStatus.FORBIDDEN.value())))
    )
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException::class)
  fun handleMethodArgumentTypeMismatchException(e: Exception): Mono<ResponseEntity<ErrorResponse>> {
    log.info("Validation exception: {}", e.message)
    return Mono.just(
      ResponseEntity
        .status(BAD_REQUEST)
        .body(
          ErrorResponse(
            status = BAD_REQUEST,
            userMessage = "Invalid Argument: ${e.cause?.message}",
            developerMessage = e.message
          )
        )
    )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

data class ErrorResponse(
  val status: Int,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo)
}
