package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile

data class ReadinessProfileRequestDTO(

  @param:Schema(description = "Booking Id", example = "12345678")
  val bookingId: Long,

  @param:Schema(description = "Work readiness profile JSON data", example = "{...}")
  @param:Valid
  val profileData: Profile,
)
