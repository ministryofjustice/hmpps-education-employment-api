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
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.knownCaseReferenceNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.makeProfileRequestWithSupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.makeProfileRequestWithSupportDeclined
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileJsonOfAnotherPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileJsonOfKnownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileJsonWithSupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.SARTestData.profileRequestOfKnownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.notesdata.domain.Note
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.anotherPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.knownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.unknownPrisonNumber
import java.time.format.DateTimeFormatter

class SARReadinessProfileGetShould : SARReadinessProfileTestCase() {
  init {
    currentUser = "BJDEV"
  }

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
      val result = assertGetSARResponseIsOk(prn = prisonNumber, expectedProfileAsJson = expectedProfile)

      val sarContent = result.sarContent()
      assertThat(sarContent).isNotEmpty

      val sarProfile = result.sarContent().let { objectMapper.valueToTree<JsonNode>(it.first()) }
      val expectedTimestamp = defaultCurrentTimeLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
      val assertTextField: (String, String) -> Unit = { field, expected -> assertThat(sarProfile.get(field).textValue()).isEqualTo(expected) }
      assertTextField("offenderId", prisonNumber)
      assertTextField("createdBy", currentUser)
      assertTextField("createdDateTime", expectedTimestamp)
      assertTextField("modifiedBy", currentUser)
      assertTextField("modifiedDateTime", expectedTimestamp)

      assertThat(sarProfile.get("notesData")).isEmpty()
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

    @BeforeEach
    fun beforeEach() {
      expectedProfileDTO = givenAnotherProfileWithSupportDeclined()
      expectedPrisonNumber = expectedProfileDTO.offenderId
      expectedProfileJson = profileJsonOfAnotherPrisonNumber
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

      listOf("bookingId").forEach {
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
    private lateinit var prisonNumber: String
    private val expectedProfileWithSupportAccepted get() = profileJsonWithSupportAccepted.deepCopy<JsonNode>().apply {
      val workExperienceNode = get("supportAccepted").get("workExperience") as ObjectNode
      workExperienceNode.set("previousWorkOrVolunteering", TextNode("")) as JsonNode
    }
    private val expectedNotes: List<Note> get() = mapOf(
      ActionTodo.BANK_ACCOUNT to "Bank account will be opened in ABC Bank.",
      ActionTodo.HOUSING to "Housing request has been submitted.",
      ActionTodo.ID to "ID document has been verified.",
    ).map { (attribute, text) -> makeNote(attribute, text) }

    @BeforeEach
    internal fun setUp() {
      prisonNumber = givenAProfileWithSupportAccepted().offenderId
      expectedNotes.forEach { addProfileNote(prisonNumber, it.attribute, it.text) }
    }

    @Test
    fun `reply 200 (OK) and no unexpected data exposed via SAR response (supportAccepted)`() {
      val expectedProfile = expectedProfileWithSupportAccepted
      val expectedNotesAsJson = objectMapper.valueToTree<JsonNode>(expectedNotes)

      val sarResult = assertGetSARResponseIsOk(
        expectedProfileAsJson = expectedProfile,
        expectedNotesAsJson = expectedNotesAsJson,
        prn = prisonNumber,
      )
      val jsonContent = objectMapper.readTree(sarResult.body!!.asJson()).get("content")

      listOf("bookingId").forEach {
        val node = jsonContent.findParent(it)
        assertThat(node).withFailMessage { "$it was not excluded! Found at:\n $node" }.isNull()
      }
    }
  }

  private fun givenTheKnownProfile(): ReadinessProfileDTO {
    val prisonNumber = knownPrisonNumber
    val profileRequest = profileRequestOfKnownPrisonNumber
    return addProfile(prisonNumber, profileRequest)
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
    return result
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
    return result
  }

  private fun addProfile(prisonNumber: String, profileRequest: ReadinessProfileRequestDTO) = assertAddReadinessProfileIsOk(prisonNumber, profileRequest).body!!

  private fun addProfileNote(prisonNumber: String, attribute: ActionTodo, noteText: String) = assertAddNoteIsOk(
    prisonNumber = prisonNumber,
    attribute = attribute,
    noteText = noteText,
  )

  private fun makeNote(attribute: ActionTodo, text: String) = Note(
    createdBy = currentUser,
    createdDateTime = defaultCurrentTimeLocal,
    attribute = attribute,
    text = text,
  )
}
