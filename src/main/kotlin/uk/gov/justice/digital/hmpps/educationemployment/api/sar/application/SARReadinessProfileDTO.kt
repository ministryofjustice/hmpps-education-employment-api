package uk.gov.justice.digital.hmpps.educationemployment.api.sar.application

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.v2.Profile as SARProfile

data class SARReadinessProfileDTO(
  val content: List<SARContentDTO>,
)

data class SARContentDTO(
  @param:Schema(description = "Offender Id", example = "ABC12345")
  val offenderId: String,

  @param:Schema(description = "Created date time", type = "string", pattern = "yyyy-MM-dd'T'HH:mm:ss", example = "2018-12-01T13:45:00", required = true)
  val createdDateTime: LocalDateTime,

  @param:Schema(description = "Last modified date time", type = "string", pattern = "yyyy-MM-dd'T'HH:mm:ss", example = "2018-12-01T13:45:00", required = true)
  val modifiedDateTime: LocalDateTime,

  @param:Schema(description = "Work readiness profile JSON data", example = "{...}")
  val profileData: SARProfile,
)
