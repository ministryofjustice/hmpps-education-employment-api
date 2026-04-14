package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.instantFromZone
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.QualificationsAndTraining
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.ModificationAudited
import java.time.Instant
import java.time.ZoneId
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkExperience as WorkExperienceEntity

@Schema(name = "SARWorkExperience", description = "Work experience of the SAR Readiness Profile")
data class WorkExperience(
  override val modifiedBy: String,
  override val modifiedDateTime: Instant,

  val previousWorkOrVolunteering: String,
  val qualificationsAndTraining: List<QualificationsAndTraining>,
  val qualificationsAndTrainingOther: String,
) : ModificationAudited {
  constructor(entity: WorkExperienceEntity, timeZoneId: ZoneId) : this(
    modifiedBy = entity.modifiedBy,
    modifiedDateTime = entity.modifiedDateTime.instantFromZone(timeZoneId),
    previousWorkOrVolunteering = entity.previousWorkOrVolunteering,
    qualificationsAndTraining = entity.qualificationsAndTraining.toList(), // deep copy
    qualificationsAndTrainingOther = entity.qualificationsAndTrainingOther,
  )
}
