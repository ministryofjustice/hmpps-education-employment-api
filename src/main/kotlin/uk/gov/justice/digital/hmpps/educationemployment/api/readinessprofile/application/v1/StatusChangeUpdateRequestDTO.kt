package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v1

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.annotation.Nullable
import jakarta.validation.Valid
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportDeclined

@Deprecated(
  message = "Use v2 instead",
  replaceWith = ReplaceWith("StatusChangeUpdateRequestDTO", "uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application"),
)
data class StatusChangeUpdateRequestDTO(

  @field:Schema(description = "Work readiness support accepted JSON data")
  @field:Valid
  @field:Nullable
  val supportAccepted: SupportAccepted?,

  @field:Schema(description = "Work readiness support declined JSON data")
  @field:Valid
  @field:Nullable
  val supportDeclined: SupportDeclined?,

  @field:Schema(description = "Work readiness status JSON data")
  @field:Valid
  val status: ProfileStatus,
)
