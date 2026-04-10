package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.AbilityToWorkImpactedBy
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.Action
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionsRequired
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.QualificationsAndTraining
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkExperience
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkImpacts
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkInterests
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkTypesOfInterest
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.EntityConvertible
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.ModificationAuditable
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.ModificationAudited
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class SupportAcceptedDTO(
  override var modifiedBy: String? = null,
  override var modifiedDateTime: Instant? = null,

  @get:Schema(description = "Things they have in place or need")
  val actionsRequired: ActionsRequiredDTO,
  @get:Schema(description = "Things that might affect their ability to work")
  val workImpacts: WorkImpactsDTO,
  @get:Schema(description = "")
  val workInterests: WorkInterestsDTO,
  @get:Schema(description = "")
  val workExperience: WorkExperienceDTO,
) : ModificationAuditable,
  EntityConvertible<SupportAccepted> {
  constructor(entity: SupportAccepted, timeZoneId: ZoneId) : this(
    modifiedBy = entity.modifiedBy,
    modifiedDateTime = entity.modifiedDateTime?.instantFromZone(timeZoneId),
    actionsRequired = ActionsRequiredDTO(entity.actionsRequired, timeZoneId),
    workImpacts = WorkImpactsDTO(entity.workImpacts, timeZoneId),
    workInterests = WorkInterestsDTO(entity.workInterests, timeZoneId),
    workExperience = WorkExperienceDTO(entity.workExperience, timeZoneId),
  )

  override fun entity(timeZoneId: ZoneId) = SupportAccepted(
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime?.localDateTimeAtZone(timeZoneId),
    actionsRequired = actionsRequired.entity(timeZoneId),
    workImpacts = workImpacts.entity(timeZoneId),
    workInterests = workInterests.entity(timeZoneId),
    workExperience = workExperience.entity(timeZoneId),
  )
}

data class ActionsRequiredDTO(
  override val modifiedBy: String,
  override val modifiedDateTime: Instant,

  @get:Schema(description = "To do / In place already")
  val actions: List<Action>,
) : ModificationAudited,
  EntityConvertible<ActionsRequired> {
  constructor(entity: ActionsRequired, timeZoneId: ZoneId) : this(
    modifiedBy = entity.modifiedBy,
    modifiedDateTime = entity.modifiedDateTime.instantFromZone(timeZoneId),
    actions = entity.actions.map { it.copy(id = it.id?.toList()) }, // deep copy
  )

  override fun entity(timeZoneId: ZoneId) = ActionsRequired(
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime.localDateTimeAtZone(timeZoneId),
    actions = actions.map { it.copy(id = it.id?.toList()) }, // deep copy
  )
}

data class WorkImpactsDTO(
  override val modifiedBy: String,
  override val modifiedDateTime: Instant,

  @get:Schema(description = "May be affected by")
  val abilityToWorkImpactedBy: List<AbilityToWorkImpactedBy>,
  @get:Schema(description = "Full-time caring responsibilities")
  val caringResponsibilitiesFullTime: Boolean,
  @get:Schema(description = "Able to manage mental health; Not in use", deprecated = true)
  val ableToManageMentalHealth: Boolean,
  @get:Schema(description = "Able to manage dependencies (Reliant on drugs or alcohol); Not in use", deprecated = true)
  val ableToManageDependencies: Boolean,
) : ModificationAudited,
  EntityConvertible<WorkImpacts> {
  constructor(entity: WorkImpacts, timeZoneId: ZoneId) : this(
    modifiedBy = entity.modifiedBy,
    modifiedDateTime = entity.modifiedDateTime.instantFromZone(timeZoneId),
    abilityToWorkImpactedBy = entity.abilityToWorkImpactedBy.toList(), // deep copy
    caringResponsibilitiesFullTime = entity.caringResponsibilitiesFullTime,
    ableToManageMentalHealth = entity.ableToManageMentalHealth,
    ableToManageDependencies = entity.ableToManageDependencies,
  )

  override fun entity(timeZoneId: ZoneId) = WorkImpacts(
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime.localDateTimeAtZone(timeZoneId),
    abilityToWorkImpactedBy = abilityToWorkImpactedBy.toList(), // deep copy
    caringResponsibilitiesFullTime = caringResponsibilitiesFullTime,
    ableToManageMentalHealth = ableToManageMentalHealth,
    ableToManageDependencies = ableToManageDependencies,
  )
}

data class WorkInterestsDTO(
  override val modifiedBy: String,
  override val modifiedDateTime: Instant,

  @get:Schema(description = "Type of work")
  val workTypesOfInterest: List<WorkTypesOfInterest>,
  @get:Schema(description = "Other type of work")
  val workTypesOfInterestOther: String,
  @get:Schema(description = "Specific job role of interest")
  val jobOfParticularInterest: String,
) : ModificationAudited,
  EntityConvertible<WorkInterests> {
  constructor(entity: WorkInterests, timeZoneId: ZoneId) : this(
    modifiedBy = entity.modifiedBy,
    modifiedDateTime = entity.modifiedDateTime.instantFromZone(timeZoneId),
    workTypesOfInterest = entity.workTypesOfInterest.toList(), // deep copy
    workTypesOfInterestOther = entity.workTypesOfInterestOther,
    jobOfParticularInterest = entity.jobOfParticularInterest,
  )

  override fun entity(timeZoneId: ZoneId) = WorkInterests(
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime.localDateTimeAtZone(timeZoneId),
    workTypesOfInterest = workTypesOfInterest.toList(), // deep copy
    workTypesOfInterestOther = workTypesOfInterestOther,
    jobOfParticularInterest = jobOfParticularInterest,
  )
}

data class WorkExperienceDTO(
  override val modifiedBy: String,
  override val modifiedDateTime: Instant,

  @get:Schema(description = "Work or volunteering experience")
  val previousWorkOrVolunteering: String,
  @get:Schema(description = "Qualifications and training")
  val qualificationsAndTraining: List<QualificationsAndTraining>,
  @get:Schema(description = "Other qualifications and training")
  val qualificationsAndTrainingOther: String,
) : ModificationAudited,
  EntityConvertible<WorkExperience> {
  constructor(entity: WorkExperience, timeZoneId: ZoneId) : this(
    modifiedBy = entity.modifiedBy,
    modifiedDateTime = entity.modifiedDateTime.instantFromZone(timeZoneId),
    previousWorkOrVolunteering = entity.previousWorkOrVolunteering,
    qualificationsAndTraining = entity.qualificationsAndTraining.toList(), // deep copy
    qualificationsAndTrainingOther = entity.qualificationsAndTrainingOther,
  )

  override fun entity(timeZoneId: ZoneId) = WorkExperience(
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime.localDateTimeAtZone(timeZoneId),
    previousWorkOrVolunteering = previousWorkOrVolunteering,
    qualificationsAndTraining = qualificationsAndTraining.toList(), // deep copy
    qualificationsAndTrainingOther = qualificationsAndTrainingOther,
  )
}

internal fun LocalDateTime.instantFromZone(zoneId: ZoneId): Instant = atZone(zoneId).toInstant()
internal fun Instant.localDateTimeAtZone(zoneId: ZoneId): LocalDateTime = atZone(zoneId).toLocalDateTime()
