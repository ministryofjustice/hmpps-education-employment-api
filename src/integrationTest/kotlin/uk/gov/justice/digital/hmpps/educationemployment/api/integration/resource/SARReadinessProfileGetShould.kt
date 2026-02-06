@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.knownCaseReferenceNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.makeProfileRequestWithSupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.makeProfileRequestWithSupportDeclined
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileJsonOfAnotherPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileJsonOfKnownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileJsonWithSupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileOfAnotherPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileRequestOfKnownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ProfileV2Service
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.anotherPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.knownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.unknownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile

class SARReadinessProfileGetShould : SARReadinessProfileTestCase() {

  @Autowired
  private lateinit var profileService: ProfileV2Service

  @Nested
  @DisplayName("Given an unknown prisoner without readiness profile")
  inner class GivenAnUnknownPrisoner {
    @Test
    fun `reply 204 (No Content), when requesting a SAR with unknown prisoner, and PRN is provided`() {
      assertGetSARResponseStatusAndBody(expectedStatusCode = HttpStatus.NO_CONTENT, prn = unknownPrisonNumber)
    }

    @Test
    fun `reply 209 (Subject Identifier is not recognised by this service), when requesting a SAR with CRN only`() {
      assertGetSARResponseStatusAndBody(expectedStatusCodeValue = 209, crn = knownCaseReferenceNumber)
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
    private lateinit var prisonNumber: String
    private val expectedProfile = profileJsonOfKnownPrisonNumber

    @BeforeEach
    internal fun setUp() {
      prisonNumber = givenTheKnownProfile().offenderId
    }

    @Test
    fun `reply 200 (Ok), when requesting SAR with a profile of known prisoner, and PRN is provided`() {
      assertGetSARResponseIsOk(prn = prisonNumber, expectedProfileAsJson = expectedProfile)
    }

    @Test
    fun `reply 200 (OK), when requesting a SAR with required role and more irrelevant roles`() {
      assertGetSARResponseIsOk(prn = prisonNumber, expectedProfileAsJson = expectedProfile, roles = listOf(SAR_ROLE, WR_VIEW_ROLE, WR_EDIT_ROLE))
    }
  }

  @Nested
  @DisplayName("Given another readiness profile with support declined")
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  inner class GivenAnotherProfileWithSupportDeclined {
    private lateinit var expectedProfileDTO: ReadinessProfileDTO
    private lateinit var expectedPrisonNumber: String
    private lateinit var expectedProfileJson: JsonNode
    private lateinit var expectedProfileData: Profile

    @BeforeEach
    fun beforeEach() {
      expectedProfileDTO = givenAnotherProfileWithSupportDeclined()
      expectedPrisonNumber = expectedProfileDTO.offenderId
      expectedProfileJson = profileJsonOfAnotherPrisonNumber
      expectedProfileData = profileOfAnotherPrisonNumber
    }

    @Test
    fun `reply 200 (OK) and no unexpected data exposed via SAR response (supportDeclined)`() {
      val prisonNumber = expectedPrisonNumber
      val expectedProfile = expectedProfileJson

      readinessProfileRepository.findById(prisonNumber).orElse(null)?.let {
        readinessProfileRepository.delete(it)
      }

      val sarResult = assertGetSARResponseIsOk(expectedProfileAsJson = expectedProfile, prn = prisonNumber)
      val jsonContent = objectMapper.readTree(sarResult.body!!.asJson()).get("content")

      listOf("bookingId", "createdBy", "modifiedBy", "noteData").forEach {
        val node = jsonContent.findParent(it)
        assertThat(node).withFailMessage { "$it was not excluded! Found at:\n $node" }.isNull()
      }
    }

    @Test
    fun `reply 200(OK), when requesting a SAR with specified period`() {
      val today = defaultCurrentTimeLocal.toLocalDate()
      val tomorrow = today.plusDays(1)

      val prisonNumber = expectedPrisonNumber
      val expectedProfile = expectedProfileJson

      val sarResult = assertGetSARResponseIsOk(
        expectedProfileAsJson = expectedProfile,
        prn = prisonNumber,
        fromDate = today,
        toDate = tomorrow,
      )
      assertThat(sarResult.body).isNotNull
    }
  }

  @Nested
  @DisplayName("Given a readiness profile with support accepted")
  inner class GivenAProfileWithSupportAccepted {

    @Test
    fun `reply 200 (OK) and no unexpected data exposed via SAR response (supportAccepted)`() {
      val prisonNumber = givenAProfileWithSupportAccepted().offenderId
      val expectedProfile = expectedProfileWithSupportAccepted

      val sarResult = assertGetSARResponseIsOk(expectedProfileAsJson = expectedProfile, prn = prisonNumber)
      val jsonContent = objectMapper.readTree(sarResult.body!!.asJson()).get("content")

      listOf("bookingId", "createdBy", "modifiedBy", "noteData").forEach {
        val node = jsonContent.findParent(it)
        assertThat(node).withFailMessage { "$it was not excluded! Found at:\n $node" }.isNull()
      }
    }
  }

  private fun givenTheKnownProfile(): ReadinessProfileDTO {
    val prisonNumber = knownPrisonNumber
    val profileRequest = profileRequestOfKnownPrisonNumber
    val profile = addProfile(prisonNumber, profileRequest)
    return ReadinessProfileDTO(profile)
  }

  private fun givenAnotherProfileWithSupportDeclined(): ReadinessProfileDTO {
    val prisonNumber = anotherPrisonNumber
    val request = makeProfileRequestWithSupportDeclined()
    val result = addProfile(prisonNumber, request)
    repeat(6) { times ->
      request.profileData.supportDeclined!!.let {
        request.profileData.supportDeclined =
          it.copy(supportToWorkDeclinedReasonOther = "modified the n-th (${times + 1}) times")
      }
    }
    return ReadinessProfileDTO(result)
  }

  private fun givenAProfileWithSupportAccepted(): ReadinessProfileDTO {
    val prisonNumber = anotherPrisonNumber
    val request = makeProfileRequestWithSupportAccepted()
    val result = addProfile(prisonNumber, request)
    repeat(6) { times ->
      request.profileData.supportAccepted!!.let {
        request.profileData.supportAccepted =
          it.copy(workExperience = it.workExperience.copy(previousWorkOrVolunteering = "modified the n-th (${times + 1}) times"))
      }
    }
    return ReadinessProfileDTO(result)
  }
  private val expectedProfileWithSupportAccepted get() = profileJsonWithSupportAccepted.deepCopy<JsonNode>().apply {
    val workExperienceNode = get("supportAccepted").get("workExperience") as ObjectNode
    workExperienceNode.set("previousWorkOrVolunteering", TextNode("")) as JsonNode
  }

  private fun addProfile(prisonNumber: String, profileRequest: ReadinessProfileRequestDTO): ReadinessProfile = profileService.createProfileForOffender(
    userId = "test user",
    offenderId = prisonNumber,
    bookingId = profileRequest.bookingId,
    profile = profileRequest.profileData,
  )
}
