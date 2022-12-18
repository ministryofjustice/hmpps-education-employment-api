package uk.gov.justice.digital.hmpps.educationemploymentapi.data

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.SupportAccepted
import javax.validation.Valid

data class AcceptStatusUpdateRequestDTO(

  @Schema(description = "Work readiness support accepted JSON data", example = "{...}")
  @Valid
  val supportAccepted: SupportAccepted,

  @Schema(description = "Work readiness status JSON data", example = "{...}")
  @Valid
  val status: ProfileStatus

)
