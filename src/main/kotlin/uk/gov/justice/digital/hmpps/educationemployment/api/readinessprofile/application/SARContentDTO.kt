package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime

data class SARContentDTO(
  val offenderId: String,
  val createdDateTime: LocalDateTime,
  val modifiedDateTime: LocalDateTime,
  val profileData: JsonNode,
)
