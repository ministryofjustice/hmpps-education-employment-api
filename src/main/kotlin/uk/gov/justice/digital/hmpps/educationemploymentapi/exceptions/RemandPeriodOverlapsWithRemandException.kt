package uk.gov.justice.digital.hmpps.educationemploymentapi.exceptions

import uk.gov.justice.digital.hmpps.educationemploymentapi.validation.ValidationCode

/*
  This exception occurs when a remand period overlaps with another remand period.
 */
class RemandPeriodOverlapsWithRemandException(message: String) : CrdCalculationValidationException(message, ValidationCode.REMAND_OVERLAPS_WITH_REMAND)
