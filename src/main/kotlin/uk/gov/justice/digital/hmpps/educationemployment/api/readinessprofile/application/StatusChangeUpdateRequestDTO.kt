package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.annotation.Nullable
import jakarta.validation.Valid
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportDeclined

data class StatusChangeUpdateRequestDTO(

  @param:Schema(description = "Work readiness support accepted JSON data", example = "{...}")
  @param:Valid
  @param:Nullable
  val supportAccepted: SupportAccepted?,

  @param:Schema(description = "Work readiness support declined JSON data", example = "{...}")
  @param:Valid
  @param:Nullable
  val supportDeclined: SupportDeclined?,

  @param:Schema(description = "Work readiness status JSON data", example = "{...}")
  @param:Valid
  val status: ProfileStatus,
)
