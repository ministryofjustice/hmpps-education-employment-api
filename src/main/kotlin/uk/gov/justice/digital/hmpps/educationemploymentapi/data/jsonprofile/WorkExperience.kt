package uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile

import java.time.LocalDateTime

data class WorkExperience(
  val author:String,
  val modifiedDateTime: LocalDateTime,

  val previousWorkOrVolunteering:String,
  val qualificationsAndTraining: List<QualificationsAndTraining>,
  val qualificationsAndTrainingOther: String
)

