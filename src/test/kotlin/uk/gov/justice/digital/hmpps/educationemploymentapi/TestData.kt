package uk.gov.justice.digital.hmpps.educationemploymentapi.service

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil
import org.mockito.kotlin.mock
import uk.gov.justice.digital.hmpps.educationemploymentapi.TestUtil
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.AbilityToWorkImpactedBy
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Action
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.ActionStatus
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.ActionTodo
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.ActionsRequired
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.CircumstanceChangesRequiredToWork
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Profile
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.QualificationsAndTraining
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.SupportAccepted
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.SupportDeclined
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.SupportToWorkDeclinedReason
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.WorkExperience
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.WorkImpacts
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.WorkInterests
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.WorkTypesOfInterest
import uk.gov.justice.digital.hmpps.educationemploymentapi.entity.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemploymentapi.repository.ReadinessProfileRepository
import java.io.File
import java.time.LocalDateTime
import java.util.Optional

class TestData {
  companion object {
    val createProfileJsonRequest = File("src/test/resources/CreateProfile_correct.json").inputStream().readBytes().toString(Charsets.UTF_8)
    val createProfileJsonResponse = File("src/test/resources/CreateProfile_Correct_Response.json").inputStream().readBytes().toString(Charsets.UTF_8)
    val noteString: String = "Mary had another little lamb"
    private val readinessProfileRepository: ReadinessProfileRepository = mock()
    private lateinit var profileService: ProfileService

    val modifiedTime = LocalDateTime.now()

    val booleanTrue = true
    val booleanFalse = false

    val createdBy = "sacintha-raj"
    val updatedBy = "phil-whils"
    val workTypesOfInterestOther = "freelance"
    val jobOfParticularInterests = "architect"
    val previousWorkOrVolunteering_NONE = "NONE"
    val qualificationAndTrainingOther = "MBA"
    val newOffenderId = "A1245BC"
    val updatedOffenderId = "A1245BD"
    val offenderIdList = listOf<String>(newOffenderId, updatedOffenderId)
    val offenderIdListjson = "[\"".plus(newOffenderId).plus("\",\"").plus(updatedOffenderId).plus("\"]")
    val emptyString = ""
    val createdByString = "createdBy"
    val offenderIdString = "offenderId"
    val bookingIdString = "bookingId"
    val newNotes = "new notes"

    val newBookingId = 123456L
    val updatedBookingId = 123457L
    var actionToDoCV = ActionTodo.CV_AND_COVERING_LETTER

    val action = Action(ActionTodo.BANK_ACCOUNT, ActionStatus.COMPLETED)

    val profileStatus_NO_RIGHT_TO_WORK = ProfileStatus.NO_RIGHT_TO_WORK

    val supportDeclinedReasonList = listOf(SupportToWorkDeclinedReason.FULL_TIME_CARER)
    val circumstanceChangesRequiredToWorkList = listOf(CircumstanceChangesRequiredToWork.DEPENDENCY_SUPPORT)
    val actionList = listOf(action)
    val abilityToWorkImpactedByList = listOf(AbilityToWorkImpactedBy.CARING_RESPONSIBILITIES)
    val workTypesOfInterestList = listOf(WorkTypesOfInterest.CONSTRUCTION)
    val qualificationsAndTrainingList = listOf(QualificationsAndTraining.ADVANCED_EDUCATION)

    val actionsRequired = ActionsRequired(
      updatedBy, modifiedTime, actionList
    )
    val workImpacts = WorkImpacts(
      updatedBy, modifiedTime, abilityToWorkImpactedByList, booleanTrue, booleanTrue, booleanTrue
    )
    val workInterests = WorkInterests(
      updatedBy, modifiedTime, workTypesOfInterestList, workTypesOfInterestOther, jobOfParticularInterests
    )
    val workExperience = WorkExperience(
      updatedBy,
      modifiedTime,
      previousWorkOrVolunteering_NONE,
      qualificationsAndTrainingList,
      qualificationAndTrainingOther
    )

    val supportDeclined: SupportDeclined = SupportDeclined(
      createdBy,
      modifiedTime,
      supportDeclinedReasonList,
      emptyString,
      circumstanceChangesRequiredToWorkList,
      emptyString
    )
    val supportAccepted: SupportAccepted = SupportAccepted(
      actionsRequired, workImpacts, workInterests, workExperience
    )
    val profile: Profile = Profile(profileStatus_NO_RIGHT_TO_WORK, supportDeclined, supportAccepted)
    val readinessProfile = ReadinessProfile(
      newOffenderId,
      newBookingId,
      createdBy,
      modifiedTime,
      createdBy,
      modifiedTime,
      "1.0",
      JacksonUtil.toJsonNode(TestUtil.objectMapper().writeValueAsString(profile)),
      JacksonUtil.toJsonNode("{}"),
      booleanTrue
    )
    val updatedReadinessProfile = Optional.of(
      ReadinessProfile(
        newOffenderId,
        updatedBookingId,
        updatedBy,
        modifiedTime,
        updatedBy,
        modifiedTime,
        "1.0",
        JacksonUtil.toJsonNode(TestUtil.objectMapper().writeValueAsString(profile)),
        JacksonUtil.toJsonNode("[]"),
        booleanTrue
      )
    )
    val updatedReadinessProfileNotes = Optional.of(
      ReadinessProfile(
        newOffenderId,
        updatedBookingId,
        updatedBy,
        modifiedTime,
        updatedBy,
        modifiedTime,
        "1.0",
        JacksonUtil.toJsonNode(TestUtil.objectMapper().writeValueAsString(profile)),
        JacksonUtil.toJsonNode(
          "[{\n" +
            "        \"createdBy\": \"sacintha-raj\",\n" +
            "        \"createdDateTime\": \"2022-09-22T09:52:53.422898\",\n" +
            "        \"attribute\": \"CV_AND_COVERING_LETTER\",\n" +
            "        \"text\": \"Mary had another little lamb\"\n" +
            "    }]"
        ),
        booleanTrue
      )
    )

    var profileList = listOf<ReadinessProfile>(readinessProfile, updatedReadinessProfileNotes.get())
    var noteFreeText = "Mary had another little lamb"
    var noteFreeTextJson = "{\n" +
      "    \"text\": \"Mary had another little lamb\"\n" +
      "}"
    val noteListJson = "[\n" +
      "    {\n" +
      "        \"createdBy\": \"sacintha-raj\",\n" +
      "        \"createdDateTime\": \"2022-09-19T15:39:17.114676\",\n" +
      "        \"attribute\": \"DISCLOSURE_LETTER\",\n" +
      "        \"text\": \"Mary had another little lamb\"\n" +
      "    },\n" +
      "    {\n" +
      "        \"createdBy\": \"sacintha-raj\",\n" +
      "        \"createdDateTime\": \"2022-09-19T15:39:20.873604\",\n" +
      "        \"attribute\": \"DISCLOSURE_LETTER\",\n" +
      "        \"text\": \"Mary had another little lamb\"\n" +
      "    }\n" +
      "]"
  }
}
