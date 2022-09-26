package uk.gov.justice.digital.hmpps.educationemploymentapi.validator

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.VALUE_PARAMETER)
@MustBeDocumented
@Constraint(validatedBy = [OffenderIdConstraintPatternValidator::class])
annotation class OffenderIdConstraint(
  val message: String = "offender id {javax.validation.constraints.Pattern.message}",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = []
)
