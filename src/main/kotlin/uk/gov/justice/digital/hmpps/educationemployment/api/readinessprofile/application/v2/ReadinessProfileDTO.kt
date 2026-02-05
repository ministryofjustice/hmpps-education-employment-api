package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2

import com.fasterxml.jackson.module.kotlin.treeToValue
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import java.time.LocalDateTime

data class ReadinessProfileDTO(
  @param:Schema(description = "Offender Id", example = "ABC12345")
  val offenderId: String,

  @param:Schema(description = "Booking Id", example = "1234567")
  val bookingId: Long,

  @param:Schema(description = "Author of original profile", example = "user4")
  val createdBy: String,

  @param:Schema(description = "Created date time", type = "string", pattern = "yyyy-MM-dd'T'HH:mm:ss", example = "2018-12-01T13:45:00", required = true)
  val createdDateTime: LocalDateTime,

  @param:Schema(description = "Author of last modification", example = "user4")
  val modifiedBy: String,

  @param:Schema(description = "Last modified date time", type = "string", pattern = "yyyy-MM-dd'T'HH:mm:ss", example = "2018-12-01T13:45:00", required = true)
  val modifiedDateTime: LocalDateTime,

  @param:Schema(description = "Version of the JSON schema", example = "1.1.1")
  val schemaVersion: String,

  @param:Schema(description = "Work readiness profile JSON data", example = "{...}")
  val profileData: Profile,
) {
  constructor(profileEntity: ReadinessProfile) : this(
    offenderId = profileEntity.offenderId,
    bookingId = profileEntity.bookingId,
    createdBy = profileEntity.createdBy,
    createdDateTime = profileEntity.createdDateTime,
    modifiedBy = profileEntity.modifiedBy,
    modifiedDateTime = profileEntity.modifiedDateTime,
    schemaVersion = profileEntity.schemaVersion,
    profileData = CapturedSpringConfigValues.objectMapper.treeToValue(profileEntity.profileData),
  )
}
