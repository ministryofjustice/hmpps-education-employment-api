package uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile

import java.time.LocalDateTime

data class WorkInterests(
  val author:String,
  val modifiedDateTime:LocalDateTime,

  val workTypesOfInterest: List<WorkTypesOfInterest>,
  val workTypesOfInterestOther: String,
  val jobOfParticularInterest: String
)