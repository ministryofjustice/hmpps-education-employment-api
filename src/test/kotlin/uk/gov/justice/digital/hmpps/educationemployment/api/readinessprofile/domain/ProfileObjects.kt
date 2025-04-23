@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain

import com.fasterxml.jackson.databind.JsonNode
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.notesdata.domain.Note
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.AbilityToWorkImpactedBy
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.Action
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionsRequired
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.CircumstanceChangesRequiredToWork
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.QualificationsAndTraining
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportDeclined
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkExperience
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkImpacts
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkInterests
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkTypesOfInterest
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import java.io.FileNotFoundException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v1.Profile as ProfileV1

object ProfileObjects {
  private val objectMapper = CapturedSpringConfigValues.objectMapper

  private val emptyJsonArray: JsonNode get() = objectMapper.readTree("[]")

  val knownPrisonNumber = "A1234BB"
  val anotherPrisonNumber = "K9876BC"
  val unknownPrisonNumber = "A1234BD"

  val prisonId = "prison2"
  val prisonName = "Prison 2"

  val newOffenderId = "A1245BC"
  val updatedOffenderId = "A1245BD"

  val newBookingId = 123456L
  val updatedBookingId = 123457L
  var actionToDoCV = ActionTodo.CV_AND_COVERING_LETTER

  val noteString = "Mary had another little lamb"

  val createProfileJsonRequest = readJsonProfile("CreateProfile_correct.json")
  val createProfileV1JsonRequest = readJsonProfile("CreateProfile_correct_v1.json")

  val createdBy = "sacintha-raj"
  val updatedBy = "phil-whils"

  val createdTime = LocalDateTime.of(2024, 1, 1, 12, 10, 20)
  val modifiedTime = createdTime.plusDays(10)
  val modifiedAgainTime = modifiedTime.plusMonths(1)

  val workTypesOfInterestOther = "freelance"
  val jobOfParticularInterests = "architect"
  val previousWorkOrVolunteeringNone = "NONE"
  val qualificationAndTrainingOther = "MBA"

  val offenderIdList = listOf(newOffenderId, updatedOffenderId)
  val newNotes = "new notes"

  val action = Action(ActionTodo.BANK_ACCOUNT, ActionStatus.COMPLETED, null, null)
  val actionModified = Action(ActionTodo.CV_AND_COVERING_LETTER, ActionStatus.IN_PROGRESS, null, null)

  val profileStatusNoRightToWork = ProfileStatus.NO_RIGHT_TO_WORK
  val profileStatusSupportNeeded = ProfileStatus.SUPPORT_NEEDED

  val supportDeclinedReasonList = listOf(SupportToWorkDeclinedReason.FULL_TIME_CARER)
  val supportDeclinedReasonModifiedList = listOf(SupportToWorkDeclinedReason.HEALTH)
  val circumstanceChangesRequiredToWorkList = listOf(CircumstanceChangesRequiredToWork.DEPENDENCY_SUPPORT)
  val actionList = listOf(action)
  val actionModifiedList = listOf(actionModified)
  val abilityToWorkImpactedByList = listOf(AbilityToWorkImpactedBy.CARING_RESPONSIBILITIES)
  val workTypesOfInterestList = listOf(WorkTypesOfInterest.CONSTRUCTION)
  val qualificationsAndTrainingList = listOf(QualificationsAndTraining.ADVANCED_EDUCATION)

  var noteFreeTextJson = """
    {
    "text": "$noteString"
    }
  """.trimIndent()

  val noteListJson = listOf("2022-09-19T15:39:17.114676", "2022-09-19T15:39:20.873604").map { createdDateTimeText ->
    """
    {
      "createdBy": "$createdBy",
      "createdDateTime": "$createdDateTimeText",
      "attribute": "${ActionTodo.DISCLOSURE_LETTER}",
      "text": "$noteString"
    }
    """.trimIndent()
  }.joinToString(separator = ",\n", prefix = "[", postfix = "]")

  val actionsRequired = ActionsRequired(
    updatedBy,
    modifiedTime,
    actionList,
  )

  val actionsModifedRequired = ActionsRequired(
    updatedBy,
    modifiedTime,
    actionModifiedList,
  )
  val workImpacts = WorkImpacts(
    updatedBy,
    modifiedTime,
    abilityToWorkImpactedByList,
    true,
    true,
    true,
  )
  val workInterests = WorkInterests(
    updatedBy,
    modifiedTime,
    workTypesOfInterestList,
    workTypesOfInterestOther,
    jobOfParticularInterests,
  )
  val workExperience = WorkExperience(
    updatedBy,
    modifiedTime,
    previousWorkOrVolunteeringNone,
    qualificationsAndTrainingList,
    qualificationAndTrainingOther,
  )

  val supportDeclined: SupportDeclined = SupportDeclined(
    createdBy,
    createdTime,
    supportDeclinedReasonList,
    "",
    circumstanceChangesRequiredToWorkList,
    "",
  )

  val supportDeclinedModified: SupportDeclined = SupportDeclined(
    updatedBy,
    modifiedTime,
    supportDeclinedReasonModifiedList,
    "",
    circumstanceChangesRequiredToWorkList,
    "",
  )

  val supportAccepted: SupportAccepted = SupportAccepted(
    null,
    null,
    actionsRequired,
    workImpacts,
    workInterests,
    workExperience,
  )
  val supportAcceptedModified: SupportAccepted = SupportAccepted(
    null,
    null,
    actionsModifedRequired,
    workImpacts,
    workInterests,
    workExperience,
  )

  val note: Note = Note(
    createdBy,
    LocalDateTime.of(LocalDate.of(2024, 1, 1), LocalTime.of(0, 0)),
    ActionTodo.DISCLOSURE_LETTER,
    "test comment",
  )

  // Latest V2 profiles
  object V2Profiles {
    val profile: Profile = makeProfile(
      status = profileStatusNoRightToWork,
      supportDeclined = supportDeclined,
    )

    val profileDeclined = profile.copy()

    val profileDeclinedModified = makeProfile(
      status = profileStatusNoRightToWork,
      supportDeclined = supportDeclinedModified,
    )

    val profileAccepted = makeProfile(
      status = profileStatusSupportNeeded,
      supportAccepted = supportAccepted,
    )

    val profileAcceptedAndModified = makeProfile(
      status = profileStatusSupportNeeded,
      supportAccepted = supportAcceptedModified,
      supportDeclined = supportDeclined,
      statusChangeType = StatusChange.DECLINED_TO_ACCEPTED,
    )

    val profileStatusNewAndBothStateIncorrect = makeProfile(
      status = profileStatusNoRightToWork,
      supportDeclined = supportDeclined,
      supportAccepted = supportAccepted,
    )

    val profileIncorrectStatus = makeProfile(
      status = profileStatusSupportNeeded,
      supportDeclined = supportDeclined,
    )

    // readiness profiles - latest v2
    val readinessProfile = ReadinessProfile(
      offenderId = newOffenderId,
      bookingId = newBookingId,
      createdBy = createdBy,
      createdDateTime = createdTime,
      modifiedBy = createdBy,
      modifiedDateTime = modifiedTime,
      schemaVersion = "2.0",
      profileData = objectMapper.valueToTree(profile),
      notesData = emptyJsonArray,
      new = true,
    )

    val updatedReadinessProfile = ReadinessProfile(
      offenderId = newOffenderId,
      bookingId = updatedBookingId,
      createdBy = createdBy,
      createdDateTime = modifiedTime,
      modifiedBy = updatedBy,
      modifiedDateTime = modifiedTime,
      schemaVersion = "2.0",
      profileData = objectMapper.valueToTree(profile),
      notesData = emptyJsonArray,
      new = true,
    )

    val updatedReadinessProfileNotes = ReadinessProfile(
      offenderId = newOffenderId,
      bookingId = updatedBookingId,
      createdBy = createdBy,
      createdDateTime = createdTime,
      modifiedBy = updatedBy,
      modifiedDateTime = modifiedTime,
      schemaVersion = "2.0",
      profileData = objectMapper.valueToTree(profile),
      notesData = objectMapper.readTree(
        """
        [{
          "createdBy": "$createdBy",
          "createdDateTime": "2022-09-22T09:52:53.422898",
          "attribute": "$actionToDoCV",
          "text": "$noteString"
         }],
        """.trimIndent(),
      ),
      new = true,
    )

    val updatedReadinessProfileAndAccepted1 = ReadinessProfile(
      newOffenderId,
      updatedBookingId,
      updatedBy,
      modifiedTime,
      updatedBy,
      modifiedTime,
      "2.0",
      objectMapper.valueToTree(profileAcceptedAndModified),
      emptyJsonArray,
      true,
    )

    val readinessProfileAndDeclined1 = ReadinessProfile(
      newOffenderId,
      newBookingId,
      createdBy,
      createdTime,
      createdBy,
      modifiedTime,
      "2.0",
      objectMapper.valueToTree(profileDeclined),
      emptyJsonArray,
      true,
    )

    val readinessProfileAndAccepted1 = ReadinessProfile(
      newOffenderId,
      newBookingId,
      createdBy,
      createdTime,
      createdBy,
      modifiedTime,
      "2.0",
      objectMapper.valueToTree(profileAccepted),
      emptyJsonArray,
      true,
    )

    val updatedReadinessProfileAndDeclined1 = ReadinessProfile(
      newOffenderId,
      updatedBookingId,
      updatedBy,
      modifiedTime,
      updatedBy,
      modifiedTime,
      "2.0",
      objectMapper.valueToTree(profileDeclinedModified),
      emptyJsonArray,
      true,
    )

    val readinessProfileOfKnownPrisoner =
      readinessProfile.copy(offenderId = knownPrisonNumber, bookingId = newBookingId, createdBy = createdBy)

    val readinessProfileOfAnotherPrisoner =
      readinessProfileAndAccepted1.copy(offenderId = anotherPrisonNumber, bookingId = newBookingId, createdBy = createdBy)

    var profileList = listOf(readinessProfile, updatedReadinessProfileNotes)
  }

  // old v1 - profiles and readiness profiles
  object V1Profiles {
    // V1 profiles
    val profile = makeProfileV1(
      status = profileStatusNoRightToWork,
      supportDeclined = supportDeclined,
      supportDeclined_history = mutableListOf(supportDeclined),
      supportAccepted_history = mutableListOf(supportAccepted),
    )

    val profileDeclined = profile.copy()

    val profileAccepted = makeProfile(
      status = profileStatusSupportNeeded,
      supportAccepted = supportAccepted,
    )

    val profileAcceptedAndModified = makeProfileV1(
      status = profileStatusSupportNeeded,
      supportAccepted = supportAcceptedModified,
      supportDeclined = supportDeclined,
      statusChangeType = StatusChange.DECLINED_TO_ACCEPTED,
    )

    val profileDeclinedModified = makeProfileV1(
      status = profileStatusNoRightToWork,
      supportDeclined = supportDeclinedModified,
    )

    val profileThatWasDeclinedTwiceAndAccepted = makeProfileV1(
      status = profileStatusSupportNeeded,
      statusChangeDate = modifiedAgainTime,
      statusChange = true,
      statusChangeType = StatusChange.DECLINED_TO_ACCEPTED,
      supportAccepted = supportAccepted,
      supportDeclined_history = mutableListOf(supportDeclined, supportDeclinedModified),
      supportAccepted_history = mutableListOf(),
    )

    val profileStatusNewAndBothStateIncorrect = makeProfileV1(
      status = profileStatusNoRightToWork,
      supportDeclined = supportDeclined,
      supportAccepted = supportAccepted,
    )

    val profileIncorrectStatus = makeProfileV1(
      status = profileStatusSupportNeeded,
      supportDeclined = supportDeclined,
    )

    val readinessProfile = ReadinessProfile(
      offenderId = newOffenderId,
      bookingId = newBookingId,
      createdBy = createdBy,
      createdDateTime = createdTime,
      modifiedBy = createdBy,
      modifiedDateTime = modifiedTime,
      schemaVersion = "1.0",
      profileData = objectMapper.valueToTree(profile),
      notesData = emptyJsonArray,
      new = true,
    )

    val updatedReadinessProfile = ReadinessProfile(
      offenderId = newOffenderId,
      bookingId = updatedBookingId,
      createdBy = createdBy,
      createdDateTime = modifiedTime,
      modifiedBy = updatedBy,
      modifiedDateTime = modifiedTime,
      schemaVersion = "1.0",
      profileData = objectMapper.valueToTree(profile),
      notesData = emptyJsonArray,
      new = true,
    )

    val updatedReadinessProfileNotes = V2Profiles.updatedReadinessProfileNotes.copy(
      schemaVersion = "1.0",
      profileData = objectMapper.valueToTree(profile),
    )

    val updatedReadinessProfileAndAccepted1 = ReadinessProfile(
      newOffenderId,
      updatedBookingId,
      updatedBy,
      modifiedTime,
      updatedBy,
      modifiedTime,
      "1.0",
      objectMapper.valueToTree(profileAcceptedAndModified),
      emptyJsonArray,
      true,
    )

    val readinessProfileAndDeclined1 = ReadinessProfile(
      newOffenderId,
      newBookingId,
      createdBy,
      createdTime,
      createdBy,
      modifiedTime,
      "1.0",
      objectMapper.valueToTree(profileDeclined),
      emptyJsonArray,
      true,
    )

    val updatedReadinessProfileAndDeclined1 = ReadinessProfile(
      newOffenderId,
      updatedBookingId,
      updatedBy,
      modifiedTime,
      updatedBy,
      modifiedTime,
      "1.0",
      objectMapper.valueToTree(profileDeclinedModified),
      emptyJsonArray,
      true,
    )

    val readinessProfileV1WithSupportDeclinedTwiceAndThenAccepted = ReadinessProfile(
      newOffenderId,
      updatedBookingId,
      createdBy,
      createdTime,
      updatedBy,
      modifiedAgainTime,
      "1.0",
      objectMapper.valueToTree(profileThatWasDeclinedTwiceAndAccepted),
      objectMapper.readTree(
        """
        [{
          "createdBy": "$createdBy",
          "createdDateTime": "$createdTime",
          "attribute": "$actionToDoCV",
          "text": "$noteString"
        }]
        """.trimIndent(),
      ),
      true,
    )

    val readinessProfileAndAccepted1 = ReadinessProfile(
      newOffenderId,
      newBookingId,
      createdBy,
      createdTime,
      createdBy,
      modifiedTime,
      "1.0",
      objectMapper.valueToTree(profileAccepted),
      emptyJsonArray,
      true,
    )

    val readinessProfileForSAR = ReadinessProfile(
      anotherPrisonNumber,
      newBookingId,
      createdBy,
      createdTime,
      modifiedBy = createdBy,
      modifiedTime,
      schemaVersion = "1.0",
      profileData = objectMapper.valueToTree(profile),
      notesData = objectMapper.valueToTree(listOf(note)),
      new = true,
    )

    var readinessProfileList = listOf(readinessProfile, updatedReadinessProfileNotes)

    val readinessProfileOfKnownPrisoner = readinessProfile.copy(offenderId = knownPrisonNumber, bookingId = newBookingId, createdBy = createdBy)

    var profileList = listOf(readinessProfile, updatedReadinessProfileNotes)
  }

  private fun readJsonProfile(fileName: String) = readTextFromResource("jsonprofile/$fileName")

  private fun readTextFromResource(filePath: String) = this.javaClass.classLoader.getResource(filePath)?.readText()
    ?: run { throw FileNotFoundException("$filePath not found") }

  private fun makeProfile(
    status: ProfileStatus,
    statusChange: Boolean? = false,
    statusChangeDate: LocalDateTime? = null,
    statusChangeType: StatusChange? = StatusChange.NEW,
    supportDeclined: SupportDeclined? = null,
    supportAccepted: SupportAccepted? = null,
  ) = Profile(
    status = status,
    statusChange = statusChange,
    statusChangeDate = statusChangeDate,
    prisonId = prisonId,
    prisonName = prisonName,
    within12Weeks = true,
    statusChangeType = statusChangeType,
    supportDeclined = supportDeclined,
    supportAccepted = supportAccepted,
  )

  private fun makeProfileV1(
    status: ProfileStatus,
    statusChange: Boolean? = false,
    statusChangeDate: LocalDateTime? = null,
    statusChangeType: StatusChange? = StatusChange.NEW,
    supportDeclined: SupportDeclined? = null,
    supportAccepted: SupportAccepted? = null,
    supportDeclined_history: MutableList<SupportDeclined>? = null,
    supportAccepted_history: MutableList<SupportAccepted>? = null,
  ) = ProfileV1(status, statusChange, statusChangeDate, prisonName, statusChangeType, supportDeclined_history, supportAccepted_history, supportDeclined, supportAccepted)

  internal fun ReadinessProfile.deepCopy() = this.copy(
    profileData = this.profileData.deepCopy(),
    notesData = this.notesData.deepCopy(),
  )

  internal fun List<String>.joinToJsonString() = joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
}
