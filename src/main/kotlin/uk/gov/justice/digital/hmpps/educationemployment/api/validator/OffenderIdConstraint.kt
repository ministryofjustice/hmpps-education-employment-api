package uk.gov.justice.digital.hmpps.educationemployment.api.validator

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.VALUE_PARAMETER)
@MustBeDocumented
@Constraint(validatedBy = [OffenderIdConstraintPatternValidator::class])
annotation class OffenderIdConstraint(
  val message: String = "offender id {jakarta.validation.constraints.Pattern.message}",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
