package uk.gov.justice.digital.hmpps.educationemploymentapi.exceptions

import org.springframework.http.HttpStatus

open class CrdWebException(
  override var message: String,
  var status: HttpStatus,
  var code: String? = null
) : Exception(message)
