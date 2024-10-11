package uk.gov.justice.digital.hmpps.educationemployment.api.data.sarprofile

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.WorkTypesOfInterest
import java.time.LocalDateTime

@Schema(name = "SARWorkInterests", description = "Work interests of the SAR Readiness Profile")
data class WorkInterests(
  val modifiedDateTime: LocalDateTime,

  val workTypesOfInterest: List<WorkTypesOfInterest>,
  val workTypesOfInterestOther: String,
  val jobOfParticularInterest: String,
)
