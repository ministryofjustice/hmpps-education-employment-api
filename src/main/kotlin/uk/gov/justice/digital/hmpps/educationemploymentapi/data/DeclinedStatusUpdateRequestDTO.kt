package uk.gov.justice.digital.hmpps.educationemploymentapi.data

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.SupportDeclined
import javax.validation.Valid

data class DeclinedStatusUpdateRequestDTO(

  @Schema(description = "Work readiness support declined JSON data", example = "{...}")
  @Valid
  val supportDeclined: SupportDeclined,

  @Schema(description = "Work readiness status JSON data", example = "{...}")
  @Valid
  val status: ProfileStatus
)
