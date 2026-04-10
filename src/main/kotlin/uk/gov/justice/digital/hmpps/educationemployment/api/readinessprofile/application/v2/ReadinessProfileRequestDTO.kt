package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.ProfileDTO

data class ReadinessProfileRequestDTO(

  @field:Schema(description = "Booking Id", example = "12345678")
  val bookingId: Long,

  @field:Schema(description = "Work readiness profile JSON data")
  @field:Valid
  val profileData: ProfileDTO,
)
