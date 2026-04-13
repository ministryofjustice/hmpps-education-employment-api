package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.annotation.Nullable
import jakarta.validation.Valid
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.SupportAcceptedDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.SupportDeclinedDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus

data class StatusChangeUpdateRequestDTO(

  @param:Schema(description = "Work readiness support accepted JSON data")
  @param:Valid
  @param:Nullable
  val supportAccepted: SupportAcceptedDTO?,

  @param:Schema(description = "Work readiness support declined JSON data")
  @param:Valid
  @param:Nullable
  val supportDeclined: SupportDeclinedDTO?,

  @param:Schema(description = "Work readiness status JSON data")
  @param:Valid
  val status: ProfileStatus,
)
