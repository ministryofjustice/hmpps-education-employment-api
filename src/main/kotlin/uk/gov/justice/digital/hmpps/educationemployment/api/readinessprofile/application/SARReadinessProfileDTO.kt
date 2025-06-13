package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application

import com.fasterxml.jackson.core.type.TypeReference
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues.Companion.objectMapperSAR
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.v2.Profile
import java.time.LocalDateTime

data class SARReadinessProfileDTO(
  val content: SARContent,
) {
  constructor(profileEntity: ReadinessProfile) : this(
    content = SARContent(profileEntity),
  )
}

data class SARContent(
  @Schema(description = "Offender Id", example = "ABC12345")
  val offenderId: String,

  @Schema(description = "Created date time", type = "string", pattern = "yyyy-MM-dd'T'HH:mm:ss", example = "2018-12-01T13:45:00", required = true)
  val createdDateTime: LocalDateTime,

  @Schema(description = "Last modified date time", type = "string", pattern = "yyyy-MM-dd'T'HH:mm:ss", example = "2018-12-01T13:45:00", required = true)
  val modifiedDateTime: LocalDateTime,

  @Schema(description = "Work readiness profile JSON data", example = "{...}")
  val profileData: Profile,
) {
  constructor(profileEntity: ReadinessProfile) : this(
    offenderId = profileEntity.offenderId,
    createdDateTime = profileEntity.createdDateTime,
    modifiedDateTime = profileEntity.modifiedDateTime,
    profileData = objectMapperSAR.treeToValue(profileEntity.profileData, object : TypeReference<Profile>() {}),
  )
}
