package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.instantFromZone
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkTypesOfInterest
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.ModificationAudited
import java.time.Instant
import java.time.ZoneId
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkInterests as WorkInterestsEntity

@Schema(name = "SARWorkInterests", description = "Work interests of the SAR Readiness Profile")
data class WorkInterests(
  override val modifiedBy: String,
  override val modifiedDateTime: Instant,

  val workTypesOfInterest: List<WorkTypesOfInterest>,
  val workTypesOfInterestOther: String,
  val jobOfParticularInterest: String,
) : ModificationAudited {
  constructor(entity: WorkInterestsEntity, timeZoneId: ZoneId) : this(
    modifiedBy = entity.modifiedBy,
    modifiedDateTime = entity.modifiedDateTime.instantFromZone(timeZoneId),
    workTypesOfInterest = entity.workTypesOfInterest.toList(), // deep copy
    workTypesOfInterestOther = entity.workTypesOfInterestOther,
    jobOfParticularInterest = entity.jobOfParticularInterest,
  )
}
