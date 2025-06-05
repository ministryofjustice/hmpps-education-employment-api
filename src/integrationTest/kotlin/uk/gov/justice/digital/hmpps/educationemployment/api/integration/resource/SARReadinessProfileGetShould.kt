@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.AbilityToWorkImpactedBy
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.Action
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionsRequired
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.CircumstanceChangesRequiredToWork
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.IDocs
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
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.SARReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.knownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.unknownPrisonNumber
import java.time.LocalDate
import java.time.LocalDateTime

class SARReadinessProfileGetShould : SARReadinessProfileTestCase() {
  @Nested
  @DisplayName("Given an unknown prisoner without readiness profile")
  inner class GivenAnUnknownPrisoner {
    @Test
    fun `reply 204 (No Content), when requesting a SAR with unknown prisoner, and PRN is provided`() {
      assertGetSARResponseStatusAndBody(expectedStatusCode = HttpStatus.NO_CONTENT, prn = unknownPrisonNumber)
    }

    @Test
    fun `reply 209 (Subject Identifier is not recognised by this service), when requesting a SAR with CRN only`() {
      assertGetSARResponseStatusAndBody(expectedStatusCodeValue = 209, crn = "A5058DY")
    }
  }

  @Nested
  @DisplayName("Given different role(s) or access(es) has/have been provided")
  inner class GivenDifferentRolesOrAccess {
    @Test
    fun `reply 401 (Unauthorized), when requesting a SAR without authorization`() {
      assertGetSARResponseStatusAndBody(expectedStatusCode = HttpStatus.UNAUTHORIZED, authorised = false)
    }

    @Test
    fun `reply 403 (Forbidden), when requesting a SAR without required role`() {
      assertGetSARResponseStatusAndBody(
        expectedStatusCode = HttpStatus.FORBIDDEN,
        expectedBody = """
        {"status":403,"errorCode":null,"userMessage":"Authentication problem. Check token and roles - Access Denied","developerMessage":"Access Denied","moreInfo":null}
        """.trimIndent(),
        roles = listOf(INCORRECT_SAR_ROLE),
      )
    }
  }

  @Nested
  @DisplayName("Given the known readiness profile")
  inner class GivenTheKnownProfile {
    @Test
    fun `reply 200 (OK), when requesting SAR with a profile of known prisoner, and PRN is provided`() {
      val sarDto = givenTheKnownDeclinedProfileSARVersion2()
      val prisonNumber = sarDto.content.offenderId
      val sarResult = assertGetSARResponseIsOk(prn = prisonNumber)

      assertThat(sarResult.body).isNotNull
    }

    @Test
    fun `reply 200 (OK), when requesting a SAR with required role and more irrelevant roles`() {
      val sarDto = givenTheKnownDeclinedProfileSARVersion2()
      val prisonNumber = sarDto.content.offenderId

      val roles = listOf(SAR_ROLE, WR_VIEW_ROLE, WR_EDIT_ROLE)
      assertGetSARResponseIsOk(prn = prisonNumber, roles = roles)
    }
  }

  @Nested
  @DisplayName("Given another readiness profile with support declined history")
  inner class GivenAnotherProfileWithSupportDeclined {
//    @Test
//    fun `reply 200 (OK) and no unexpected data exposed via SAR response (supportDeclined)`() {
//      val prisonNumber = knownPrisonNumber
//      val sarResult = assertGetSARResponseIsOk(prn = prisonNumber)
//      val jsonContent = objectMapper.readTree(sarResult.body!!.asJson()).get("content")
//
//      listOf("bookingId", "createdBy", "modifiedBy", "noteData").forEach {
//        val node = jsonContent.findParent(it)
//        assertThat(node).withFailMessage { "$it was not excluded! Found at:\n $node" }.isNull()
//      }
//    }

    @Nested
    @DisplayName("And period filter (fromDate, toDate) has been set")
    inner class AndPeriodFilterHasBeenSet {
      private val today = defaultCurrentTimeLocal.toLocalDate()
      private val tomorrow = today.plusDays(1)
      private val yesterday = today.minusDays(1)

      @Test
      fun `reply 200 (OK), when requesting a SAR with specified period`() {
        val profileDTO = givenTheKnownDeclinedProfileSARVersion2()

        val sarResult = assertGetSARResponseIsOk(
          prn = profileDTO.content.offenderId,
          fromDate = today,
          toDate = tomorrow,
        )
        assertThat(sarResult.body).isNotNull
      }

      @Test
      fun `reply 400(Bad Request), when requesting a SAR with invalid date range`() {
        val expectedErrorBody = "fromDate ($tomorrow) cannot be after toDate ($today)".let { errorMessage ->
          """
          {"status":400,"errorCode":null,"userMessage":"$errorMessage","developerMessage":"$errorMessage","moreInfo":null}
          """.trimIndent()
        }

        assertGetSARResponseStatusAndBody(
          expectedStatusCode = HttpStatus.BAD_REQUEST,
          expectedBody = expectedErrorBody,
          fromDate = tomorrow,
          toDate = today,
          prn = knownPrisonNumber,
        )
      }
    }
  }

  @Nested
  @DisplayName("Given a readiness profile with support accepted")
  inner class GivenAProfileWithSupportAccepted {
    @Test
    fun `reply 200 (OK) and no unexpected data exposed via SAR response (supportAccepted)`() {
      val sarDto = givenTheKnownAcceptedProfileSARVersion2()
      val prisonNumber = sarDto.content.offenderId

      val sarResult = assertGetSARResponseIsOk(prn = prisonNumber)
      val jsonContent = objectMapper.readTree(sarResult.body!!.asJson()).get("content")

      listOf("bookingId", "createdBy", "modifiedBy", "noteData").forEach {
        val node = jsonContent.findParent(it)
        assertThat(node).withFailMessage { "$it was not excluded! Found at:\n $node" }.isNull()
      }
    }
  }

  @Test
  fun `reply 200 (OK) and supportAccepted actions fields are lists`() {
    val sarDto = givenTheKnownAcceptedProfileSARVersion2()
    val prisonNumber = sarDto.content.offenderId

    val sarResult = assertGetSARResponseIsOk(prn = prisonNumber)
    val contentJson = objectMapper.readTree(sarResult.body!!.asJson()).get("content")

    val actionsArray = contentJson
      .get("profileData")
      .get("supportAccepted")
      .get("actionsRequired")
      .get("actions")

    assertThat(actionsArray.isArray).isTrue
    assertThat(actionsArray).isNotEmpty

    val firstAction = actionsArray.first()

    assertThat(firstAction.get("todoItem").isArray).isTrue
    assertThat(firstAction.get("status").isArray).isTrue
    val idNode = firstAction.get("id")
    if (!idNode.isNull) {
      assertThat(idNode.isArray).isTrue
    }
  }

  @Test
  fun `rreply 204 (No Content), when requesting SAR with unknown prisoner`() {
    assertGetSARResponseStatusAndBody(expectedStatusCode = HttpStatus.NO_CONTENT, prn = "A9999ZZ")
  }

  @Test
  fun `reply 400 (Bad Request), when fromDate is after toDate`() {
    val today = defaultCurrentTimeLocal.toLocalDate()
    val tomorrow = today.plusDays(1)
    val errorMessage = "fromDate ($tomorrow) cannot be after toDate ($today)"
    val expectedBody = """
      {"status":400,"errorCode":null,"userMessage":"$errorMessage","developerMessage":"$errorMessage","moreInfo":null}
    """.trimIndent()

    assertGetSARResponseStatusAndBody(
      expectedStatusCode = HttpStatus.BAD_REQUEST,
      expectedBody = expectedBody,
      fromDate = tomorrow,
      toDate = today,
      prn = knownPrisonNumber,
    )
  }

  private fun buildDeclinedSupportProfile(): ReadinessProfileRequestDTO = ReadinessProfileRequestDTO(
    bookingId = 123456L,
    profileData = Profile(
      status = ProfileStatus.NO_RIGHT_TO_WORK,
      statusChange = false,
      statusChangeDate = null,
      prisonId = "C012",
      prisonName = "Sample Prison",
      statusChangeType = StatusChange.NEW,
      supportDeclined = SupportDeclined(
        modifiedDateTime = LocalDateTime.of(2025, 6, 1, 10, 15, 30),
        supportToWorkDeclinedReason = listOf(SupportToWorkDeclinedReason.FULL_TIME_CARER),
        supportToWorkDeclinedReasonOther = "",
        circumstanceChangesRequiredToWork = listOf(CircumstanceChangesRequiredToWork.DEPENDENCY_SUPPORT),
        circumstanceChangesRequiredToWorkOther = "",
        modifiedBy = "A User",
      ),
      supportAccepted = null,
      within12Weeks = true,
    ),
  )

  private fun buildAcceptedSupportProfile(): ReadinessProfileRequestDTO = ReadinessProfileRequestDTO(
    bookingId = 123456L,
    profileData = Profile(
      status = ProfileStatus.NO_RIGHT_TO_WORK,
      statusChange = false,
      statusChangeDate = null,
      prisonId = "C012",
      prisonName = "Sample Prison",
      statusChangeType = StatusChange.NEW,
      within12Weeks = true,
      supportDeclined = null,
      supportAccepted = SupportAccepted(
        modifiedDateTime = LocalDateTime.of(2025, 6, 1, 10, 15, 30),
        workInterests = WorkInterests(
          modifiedDateTime = LocalDateTime.of(2025, 6, 1, 10, 15, 30),
          workTypesOfInterest = listOf(WorkTypesOfInterest.CONSTRUCTION),
          workTypesOfInterestOther = "",
          jobOfParticularInterest = "Look after horses",
          modifiedBy = "A User",
        ),
        actionsRequired = ActionsRequired(
          modifiedBy = "A User",
          modifiedDateTime = LocalDateTime.of(2025, 6, 1, 10, 15, 30),
          actions = listOf(
            Action(
              todoItem = ActionTodo.CV_AND_COVERING_LETTER,
              status = ActionStatus.COMPLETED,
              other = "job offer",
              id = listOf(IDocs.PASSPORT),
            ),
          ),
        ),
        modifiedBy = "Some one",
        workExperience = WorkExperience(
          modifiedBy = "an Other",
          modifiedDateTime = LocalDateTime.of(2025, 6, 1, 10, 15, 30),
          previousWorkOrVolunteering = "",
          qualificationsAndTraining = listOf(QualificationsAndTraining.CSCS),
          qualificationsAndTrainingOther = "",
        ),
        workImpacts = WorkImpacts(
          modifiedBy = "The User",
          modifiedDateTime = LocalDateTime.of(2025, 6, 1, 10, 15, 30),
          abilityToWorkImpactedBy = listOf(
            AbilityToWorkImpactedBy.DEPENDENCY_ISSUES,
            AbilityToWorkImpactedBy.MENTAL_HEALTH_ISSUES,
          ),
          caringResponsibilitiesFullTime = false,
          ableToManageMentalHealth = false,
          ableToManageDependencies = true,
        ),
      ),
    ),
  )

  private fun givenTheKnownDeclinedProfileSARVersion2(): SARReadinessProfileDTO {
    val prisonNumber = knownPrisonNumber
    val profileRequest = buildDeclinedSupportProfile()
    addProfileV2(prisonNumber, profileRequest)
    val profileEntity = getProfileForOffenderFilterByPeriodV2(prisonNumber, LocalDate.now(), LocalDate.now().plusDays(1))
    return SARReadinessProfileDTO(profileEntity)
  }

  private fun givenTheKnownAcceptedProfileSARVersion2(): SARReadinessProfileDTO {
    val prisonNumber = knownPrisonNumber
    val profileRequest = buildAcceptedSupportProfile()
    addProfileV2(prisonNumber, profileRequest)
    val profileEntity = getProfileForOffenderFilterByPeriodV2(prisonNumber, LocalDate.now(), LocalDate.now().plusDays(1))
    return SARReadinessProfileDTO(profileEntity)
  }

  @Test
  fun `reply 200 (OK) and SAR profileData contains expected values`() {
    val sarDto = givenTheKnownDeclinedProfileSARVersion2()
    val profileData = sarDto.content.profileData

    assertThat(profileData.status).containsExactly(ProfileStatus.NO_RIGHT_TO_WORK)
    assertThat(profileData.statusChangeType).containsExactly(StatusChange.NEW)
  }

  @Test
  fun `reply 204 (No Content), when requesting SAR with unknown prisoner`() {
    assertGetSARResponseStatusAndBody(expectedStatusCode = HttpStatus.NO_CONTENT, prn = "A9999ZZ")
  }

  @Test
  fun `reeply 400 (Bad Request), when fromDate is after toDate`() {
    val today = defaultCurrentTimeLocal.toLocalDate()
    val tomorrow = today.plusDays(1)
    val errorMessage = "fromDate ($tomorrow) cannot be after toDate ($today)"
    val expectedBody = """
      {"status":400,"errorCode":null,"userMessage":"$errorMessage","developerMessage":"$errorMessage","moreInfo":null}
    """.trimIndent()

    assertGetSARResponseStatusAndBody(
      expectedStatusCode = HttpStatus.BAD_REQUEST,
      expectedBody = expectedBody,
      fromDate = tomorrow,
      toDate = today,
      prn = knownPrisonNumber,
    )
  }
}
