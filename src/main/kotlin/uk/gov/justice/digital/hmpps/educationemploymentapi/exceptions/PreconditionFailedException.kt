package uk.gov.justice.digital.hmpps.educationemploymentapi.exceptions

import org.springframework.http.HttpStatus

class PreconditionFailedException(message: String) : CrdWebException(message, HttpStatus.PRECONDITION_FAILED)
