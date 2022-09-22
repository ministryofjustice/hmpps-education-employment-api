package uk.gov.justice.digital.hmpps.educationemploymentapi.validator

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class OffenderIdConstraintPatternValidator : ConstraintValidator<OffenderIdConstraint, List<String>> {
  override fun isValid(offenderIds: List<String>?, context: ConstraintValidatorContext?): Boolean {
    offenderIds?.forEach {
      if (!validateOffenderId(it)) {
        return false
      }
    }
    return true
  }

  private fun validateOffenderId(offenderId: String): Boolean {
    return offenderId.matches(Regex("^[A-Z]\\d{4}[A-Z]{2}\$"))
  }
}
