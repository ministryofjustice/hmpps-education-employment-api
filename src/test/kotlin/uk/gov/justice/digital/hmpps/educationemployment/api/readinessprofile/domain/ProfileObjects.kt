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
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.Profile
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
import java.io.FileNotFoundException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object ProfileObjects {
  private val objectMapper = CapturedSpringConfigValues.objectMapper

  private val emptyJsonArray: JsonNode get() = objectMapper.readTree("[]")

  val knownPrisonNumber = "A1234BB"
  val anotherPrisonNumber = "K9876BC"
  val unknownPrisonNumber = "A1234BD"

  val newOffenderId = "A1245BC"
  val updatedOffenderId = "A1245BD"

  val newBookingId = 123456L
  val updatedBookingId = 123457L
  var actionToDoCV = ActionTodo.CV_AND_COVERING_LETTER

  val noteString = "Mary had another little lamb"

  val createProfileJsonRequest = readJsonProfile("CreateProfile_correct.json")

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

  val profileWithStatusNoRightToWork = ProfileStatus.NO_RIGHT_TO_WORK
  val profileWithStatusSupportNeeded = ProfileStatus.SUPPORT_NEEDED

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
  val supportDeclinedModifiedOther: SupportDeclined = SupportDeclined(
    createdBy,
    createdTime,
    supportDeclinedReasonModifiedList,
    "ModifiedString",
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

  val profile: Profile = Profile(
    profileWithStatusNoRightToWork, false, null, "prison2", StatusChange.NEW,
    mutableListOf(supportDeclined),
    mutableListOf(supportAccepted), supportDeclined, null,
  )

  val profileDeclined: Profile =
    Profile(profileWithStatusNoRightToWork, false, null, "prison2", StatusChange.NEW, null, null, supportDeclined, null)
  val profileDeclinedAndDeclinedList: Profile = Profile(
    profileWithStatusNoRightToWork, false, null, "prison2", StatusChange.NEW,
    mutableListOf(
      supportDeclinedModifiedOther,
    ),
    null, supportDeclined, null,
  )
  val profileDeclinedModified: Profile = Profile(
    profileWithStatusNoRightToWork, false, null, "prison2", StatusChange.NEW, mutableListOf(supportDeclined), null,
    supportDeclinedModified, null,
  )

  val profileAccpeted: Profile =
    Profile(profileWithStatusSupportNeeded, false, null, "prison2", StatusChange.NEW, null, null, null, supportAccepted)
  val profileAccpetedAndModified: Profile = Profile(
    profileWithStatusSupportNeeded,
    false,
    null,
    "prison2",
    StatusChange.NEW,
    null,
    null,
    null,
    supportAcceptedModified,
  )

  val profileThatWasDeclinedTwiceAndAccepted = Profile(
    status = ProfileStatus.SUPPORT_NEEDED,
    statusChangeDate = modifiedAgainTime,
    statusChange = true,
    statusChangeType = StatusChange.DECLINED_TO_ACCEPTED,
    prisonName = "prison2",
    supportDeclined = null,
    supportAccepted = supportAccepted,
    supportDeclined_history = mutableListOf(supportDeclined, supportDeclinedModified),
    supportAccepted_history = mutableListOf(),
  )

  val profileStatusNewAndBothStateIncorrect: Profile = Profile(
    profileWithStatusNoRightToWork, false, null, "prison2", StatusChange.NEW,
    mutableListOf(supportDeclined),
    mutableListOf(supportAccepted), supportDeclined, supportAccepted,
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

  val profileIncorrectStatus: Profile = Profile(
    profileWithStatusSupportNeeded, false, null, "prison1", StatusChange.NEW, mutableListOf(supportDeclined),
    mutableListOf(supportAccepted),
    supportDeclined, null,
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

  val updatedReadinessProfileNotes = ReadinessProfile(
    offenderId = newOffenderId,
    bookingId = updatedBookingId,
    createdBy = createdBy,
    createdDateTime = createdTime,
    modifiedBy = updatedBy,
    modifiedDateTime = modifiedTime,
    schemaVersion = "1.0",
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

  val readinessProfileAndAccepted1 = ReadinessProfile(
    newOffenderId,
    newBookingId,
    createdBy,
    createdTime,
    createdBy,
    modifiedTime,
    "1.0",
    objectMapper.valueToTree(profileAccpeted),
    emptyJsonArray,
    true,
  )

  val updatedReadinessProfileAndAccepted1 = ReadinessProfile(
    newOffenderId,
    updatedBookingId,
    updatedBy,
    modifiedTime,
    updatedBy,
    modifiedTime,
    "1.0",
    objectMapper.valueToTree(profileAccpetedAndModified),
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

  val readinessProfileAndDeclined1AndDeclinedList = ReadinessProfile(
    newOffenderId,
    newBookingId,
    createdBy,
    createdTime,
    createdBy,
    modifiedTime,
    "1.0",
    objectMapper.valueToTree(profileDeclinedAndDeclinedList),
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

  val readinessProfileWithSupportDeclinedTwiceAndThenAccepted = ReadinessProfile(
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

  val readinessProfileOfKnownPrisoner = readinessProfile.copy(offenderId = knownPrisonNumber, bookingId = newBookingId, createdBy = createdBy)

  var profileList = listOf(readinessProfile, updatedReadinessProfileNotes)

  private fun readJsonProfile(fileName: String) = readTextFromResource("jsonprofile/$fileName")

  private fun readTextFromResource(filePath: String) = this.javaClass.classLoader.getResource(filePath)?.readText()
    ?: run { throw FileNotFoundException("$filePath not found") }

  internal fun ReadinessProfile.deepCopy() = this.copy(
    profileData = this.profileData.deepCopy(),
    notesData = this.notesData.deepCopy(),
  )

  internal fun List<String>.joinToJsonString() = joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
}
