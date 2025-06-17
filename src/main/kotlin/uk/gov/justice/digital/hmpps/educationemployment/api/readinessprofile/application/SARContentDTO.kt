package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application

import com.fasterxml.jackson.databind.JsonNode

data class SARContentDTO(
  val offenderId: String,
  val bookingId: Long,
  val schemaVersion: String,
  val profileData: JsonNode,
)
