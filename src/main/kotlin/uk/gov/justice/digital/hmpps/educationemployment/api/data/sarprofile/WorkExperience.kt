package uk.gov.justice.digital.hmpps.educationemployment.api.data.sarprofile

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.QualificationsAndTraining
import java.time.LocalDateTime

@Schema(name = "SARWorkExperience", description = "Work experience of the SAR Readiness Profile")
data class WorkExperience(
  val modifiedDateTime: LocalDateTime,

  val previousWorkOrVolunteering: String,
  val qualificationsAndTraining: List<QualificationsAndTraining>,
  val qualificationsAndTrainingOther: String,
)
