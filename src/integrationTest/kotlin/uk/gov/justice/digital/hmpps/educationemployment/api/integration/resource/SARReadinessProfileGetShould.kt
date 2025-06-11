@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.makeProfileRequestOfAnotherPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.makeProfileRequestWithSupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileRequestOfKnownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.CircumstanceChangesRequiredToWork
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportDeclined
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.anotherPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.knownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.unknownPrisonNumber
import java.time.LocalDateTime

class SARReadinessProfileGetShould : SARReadinessProfileTestCase() {
  @Nested
  @DisplayName("Given an unknown prisoner without readiness profile")
  inner class GivenAnUnknownPrisoner {
    @Test
    fun `reply 204 (No Content), when requesting a SAR with unknown prisoner, and PRN is provided`() {
      assertGetSARResponseStatusAndBody(expectedStatusCode = HttpStatus.NO_CONTENT, prn = unknownPrisonNumber)
    }

//    @Test
//    fun `reply 209 (Subject Identifier is not recognised by this service), when requesting a SAR with CRN only`() {
//      assertGetSARResponseStatusAndBody(expectedStatusCodeValue = 209, crn = knownnCaseReferenceNumber)
//    }
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

//  @Nested
//  @DisplayName("Given the known readiness profile")
//  inner class GivenTheKnownProfile {
//    @Test
//    fun `reply 200 (Ok), when requesting SAR with a profile of known prisoner, and PRN is provided`() {
//      val prisonNumber = givenTheKnownProfile().offenderId
//
//      val sarResult = assertGetSARResponseIsOk(prn = prisonNumber)
//      assertThat(sarResult.body).isNotNull
//    }

//    @Test
//    fun `reply 200 (OK), when requesting a SAR with required role and more irrelevant roles`() {
//      val prisonNumber = givenTheKnownProfile().offenderId
//
//      assertGetSARResponseIsOk(prn = prisonNumber, roles = listOf(SAR_ROLE, WR_VIEW_ROLE, WR_EDIT_ROLE))
//    }
//  }

//  @Nested
//  @DisplayName("Given another readiness profile with support declined history")
//  inner class GivenAnotherProfileWithSupportDeclinedHistory {
//    private lateinit var expectedProfileDTO: ReadinessProfileDTO
//    private lateinit var expectedPrisonNumber: String
//    private lateinit var expectedProfileJson: JsonNode
//    private lateinit var expectedProfileData: Profile
//
//    @BeforeEach
//    fun beforeEach() {
//      expectedProfileDTO = givenAnotherProfileWithSupportDeclined()
//      expectedPrisonNumber = expectedProfileDTO.offenderId
//      expectedProfileJson = profileJsonOfAnotherPrisonNumber
//      expectedProfileData = profileOfAnotherPrisonNumber
//    }

    /*
    @Test
    fun `reply 200 (OK) and no unexpected data exposed via SAR response (supportDeclined, supportDeclined_history)`() {
      val prisonNumber = expectedPrisonNumber
      val expectedProfile = expectedProfileJson

      val sarResult = assertGetSARResponseIsOk(expectedProfileAsJson = expectedProfile, prn = prisonNumber)
      val jsonContent = objectMapper.readTree(sarResult.body!!.asJson()).get("content")

      listOf("bookingId", "createdBy", "modifiedBy", "noteData").forEach {
        val node = jsonContent.findParent(it)
        assertThat(node).withFailMessage { "$it was not excluded! Found at:\n $node" }.isNull()
      }
    }
     */

//    @Nested
//    @DisplayName("And period filter (fromDate, toDate) has been set")
//    inner class AndPeriodFilterHasBeenSet {
//      private val today = defaultCurrentTimeLocal.toLocalDate()
//      private val tomorrow = today.plusDays(1)
//      private val yesterday = today.minusDays(1)

//      @Test
//      fun `reply 200(OK), when requesting a SAR with specified period`() {
//        val prisonNumber = expectedPrisonNumber
//        val expectedProfile = expectedProfileJson
//
//        val sarResult = assertGetSARResponseIsOk(
//          expectedProfileAsJson = expectedProfile,
//          prn = prisonNumber,
//          fromDate = today,
//          toDate = tomorrow,
//        )
//        assertThat(sarResult.body).isNotNull
//      }

//      @Test
//      fun `reply 400(Bad Request), when requesting a SAR with invalid date range`() {
//        val expectedErrorBody = "fromDate ($tomorrow) cannot be after toDate ($today)".let { errorMessage ->
//          """
//            {"status":400,"errorCode":null,"userMessage":"$errorMessage","developerMessage":"$errorMessage","moreInfo":null}
//          """.trimIndent()
//        }
//
//        assertGetSARResponseStatusAndBody(
//          expectedStatusCode = HttpStatus.BAD_REQUEST,
//          expectedBody = expectedErrorBody,
//          fromDate = tomorrow,
//          toDate = today,
//          prn = knownPrisonNumber,
//        )
//      }
//    }
//  }

//  @Nested
//  @DisplayName("Given a readiness profile with support accepted")
//  inner class GivenAProfileWithSupportAccepted {

//    @Test
//    fun `reply 200 (OK) and no unexpected data exposed via SAR response (supportAccepted)`() {
//      val prisonNumber = givenAProfileWithSupportAccepted().offenderId
//      val expectedProfile = profileJsonWithSupportAccepted
//
//      println("expectedProfile: " + expectedProfile)
//
//      val sarResult = assertGetSARResponseIsOk(expectedProfileAsJson = expectedProfile, prn = prisonNumber)
//      val jsonContent = objectMapper.readTree(sarResult.body!!.asJson()).get("content")
//
//      listOf("bookingId", "createdBy", "modifiedBy", "noteData").forEach {
//        val node = jsonContent.findParent(it)
//        assertThat(node).withFailMessage { "$it was not excluded! Found at:\n $node" }.isNull()
//      }
//    }
//  }

  private fun givenTheKnownProfile(): ReadinessProfileDTO {
    val prisonNumber = knownPrisonNumber
    val profile = addProfileV2(prisonNumber, profileRequestOfKnownPrisonNumber)
    return ReadinessProfileDTO(profile)
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

  private fun givenAnotherProfileWithSupportDeclined(): ReadinessProfileDTO {
    val prisonNumber = anotherPrisonNumber
    val request = makeProfileRequestOfAnotherPrisonNumber()
    var result = addProfileV2(prisonNumber, request)
    repeat(6) { times ->
      request.profileData.supportDeclined!!.let {
        request.profileData.supportDeclined =
          it.copy(supportToWorkDeclinedReasonOther = "modified the n-th (${times + 1}) times")
      }
      result = addProfileV2(prisonNumber, request)
    }
    return ReadinessProfileDTO(result)
  }

  private fun givenAProfileWithSupportAccepted(): ReadinessProfileDTO {
    val prisonNumber = "X001XXX"
    val request = makeProfileRequestWithSupportAccepted()
    var result = addProfileV2(prisonNumber, request)
    repeat(6) { times ->
      request.profileData.supportAccepted!!.let {
        request.profileData.supportAccepted =
          it.copy(workExperience = it.workExperience.copy(previousWorkOrVolunteering = "modified the n-th (${times + 1}) times"))
      }
    }
    return ReadinessProfileDTO(result)
  }
}
