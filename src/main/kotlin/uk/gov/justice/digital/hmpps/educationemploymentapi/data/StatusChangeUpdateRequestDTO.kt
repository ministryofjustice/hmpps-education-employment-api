package uk.gov.justice.digital.hmpps.educationemploymentapi.data

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import org.springframework.lang.Nullable
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.SupportAccepted
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.SupportDeclined

data class StatusChangeUpdateRequestDTO(

  @Schema(description = "Work readiness support accepted JSON data", example = "{...}")
  @Valid
  @Nullable
  val supportAccepted: SupportAccepted?,

  @Schema(description = "Work readiness support declined JSON data", example = "{...}")
  @Valid
  @Nullable
  val supportDeclined: SupportDeclined?,

  @Schema(description = "Work readiness status JSON data", example = "{...}")
  @Valid
  val status: ProfileStatus,

)
