package uk.gov.justice.digital.hmpps.educationemploymentapi.exceptions

import uk.gov.justice.digital.hmpps.educationemploymentapi.validation.ValidationCode

class AdjustmentIsAfterReleaseDateException(message: String, arguments: List<String>) : CrdCalculationValidationException(message, ValidationCode.ADJUSTMENT_AFTER_RELEASE, arguments)
