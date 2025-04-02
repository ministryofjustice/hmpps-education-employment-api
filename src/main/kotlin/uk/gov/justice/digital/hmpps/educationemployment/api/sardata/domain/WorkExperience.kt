package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.QualificationsAndTraining
import java.time.LocalDateTime

@Schema(name = "SARWorkExperience", description = "Work experience of the SAR Readiness Profile")
data class WorkExperience(
  val modifiedDateTime: LocalDateTime,

  val previousWorkOrVolunteering: String,
  val qualificationsAndTraining: List<QualificationsAndTraining>,
  val qualificationsAndTrainingOther: String,
)
