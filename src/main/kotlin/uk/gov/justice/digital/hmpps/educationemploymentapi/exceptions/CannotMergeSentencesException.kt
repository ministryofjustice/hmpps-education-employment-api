package uk.gov.justice.digital.hmpps.educationemploymentapi.exceptions

import org.springframework.http.HttpStatus

class CannotMergeSentencesException(message: String) : CrdWebException(message, HttpStatus.UNPROCESSABLE_ENTITY)
