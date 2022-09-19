package uk.gov.justice.digital.hmpps.educationemploymentapi.exceptions

import uk.gov.justice.digital.hmpps.educationemploymentapi.validation.ValidationCode

class CustodialPeriodExtinguishedException(message: String, arguments: List<String>) : CrdCalculationValidationException(message, ValidationCode.CUSTODIAL_PERIOD_EXTINGUISHED, arguments)
