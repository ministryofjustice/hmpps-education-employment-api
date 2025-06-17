package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain

import java.time.LocalDateTime

data class WorkExperience(
  val modifiedBy: String = "",
  val modifiedDateTime: LocalDateTime,

  val previousWorkOrVolunteering: String,
  val qualificationsAndTraining: List<QualificationsAndTraining>,
  val qualificationsAndTrainingOther: String,
)
