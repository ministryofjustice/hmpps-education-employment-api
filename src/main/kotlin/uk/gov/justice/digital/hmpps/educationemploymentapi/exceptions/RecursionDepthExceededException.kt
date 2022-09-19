package uk.gov.justice.digital.hmpps.educationemploymentapi.exceptions

import org.springframework.http.HttpStatus

class RecursionDepthExceededException(message: String) : CrdWebException(message, HttpStatus.INTERNAL_SERVER_ERROR)
