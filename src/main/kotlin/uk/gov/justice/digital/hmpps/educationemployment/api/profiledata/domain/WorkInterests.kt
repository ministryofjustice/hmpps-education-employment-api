package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain

import java.time.LocalDateTime

data class WorkInterests(
  val modifiedBy: String = "",
  val modifiedDateTime: LocalDateTime,

  val workTypesOfInterest: List<WorkTypesOfInterest>,
  val workTypesOfInterestOther: String,
  val jobOfParticularInterest: String,
)
