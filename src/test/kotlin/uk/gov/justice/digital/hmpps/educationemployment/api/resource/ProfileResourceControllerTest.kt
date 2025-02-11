package uk.gov.justice.digital.hmpps.educationemployment.api.resource

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.reset
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import uk.gov.justice.digital.hmpps.educationemployment.api.TestData
import uk.gov.justice.digital.hmpps.educationemployment.api.config.ControllerAdvice
import uk.gov.justice.digital.hmpps.educationemployment.api.config.DpsPrincipal
import uk.gov.justice.digital.hmpps.educationemployment.api.data.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.Note
import uk.gov.justice.digital.hmpps.educationemployment.api.helpers.JwtAuthHelper
import uk.gov.justice.digital.hmpps.educationemployment.api.service.ProfileService

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@WebMvcTest(controllers = [ProfileResourceController::class])
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = [ProfileResourceController::class])
@WebAppConfiguration
class ProfileResourceControllerTest {

  @MockBean
  private lateinit var profileService: ProfileService

  @Autowired
  private lateinit var mvc: MockMvc

  @Autowired
  private lateinit var mapper: ObjectMapper

  var jwtAuthHelper: JwtAuthHelper = JwtAuthHelper()

  @BeforeEach
  fun reset() {
    reset(profileService)
    SecurityMockMvcConfigurers.springSecurity()

    mvc = MockMvcBuilders
      .standaloneSetup(ProfileResourceController(profileService))
      .setControllerAdvice(ControllerAdvice())
      .build()
  }

  @Test
  fun `Test GET of a PRELIMINARY retreive profile notes`() {
    val prisonerId = "A1234AB"
    val disclosureLetter = ActionTodo.DISCLOSURE_LETTER.toString()
    var notesList: MutableList<Note> = mapper.readValue(TestData.noteListJson, object : TypeReference<MutableList<Note>>() {})
    whenever(profileService.getProfileNotesForOffender(prisonerId, ActionTodo.DISCLOSURE_LETTER)).thenReturn(notesList)

    val result = mvc.perform(get("/readiness-profiles/A1234AB/notes/$disclosureLetter").accept(APPLICATION_JSON))
      .andExpect(status().isOk)
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn()
    var retreivedNotesList: MutableList<Note> = mapper.readValue(result.response.contentAsString, object : TypeReference<MutableList<Note>>() {})
    assert(retreivedNotesList.size == 2)
    verify(profileService, times(1)).getProfileNotesForOffender(prisonerId, ActionTodo.DISCLOSURE_LETTER)
  }

  @Test
  fun `Test Post  of a add profile notes`() {
    val disclosureLetter = ActionTodo.DISCLOSURE_LETTER.toString()
    var notesList: MutableList<Note> = mapper.readValue(TestData.noteListJson, object : TypeReference<MutableList<Note>>() {})
    whenever(profileService.addProfileNoteForOffender(any(), any(), any(), any())).thenReturn(notesList)

    val result = mvc.perform(post("/readiness-profiles/A1234AB/notes/$disclosureLetter").accept(APPLICATION_JSON).content(TestData.noteFreeTextJson).contentType(APPLICATION_JSON).principal(DpsPrincipal("test", "test")).headers((setAuthorisation(roles = listOf("WORK_READINESS_EDIT")))))
      .andExpect(status().isOk)
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn()
    var retreivedNotesList: MutableList<Note> = mapper.readValue(result.response.contentAsString, object : TypeReference<MutableList<Note>>() {})
    assert(retreivedNotesList.size == 2)
    verify(profileService, times(1)).addProfileNoteForOffender(any(), any(), any(), any())
  }

  @Test
  fun `Test Post  of a add profile `() {
    whenever(profileService.createProfileForOffender(any(), any(), any(), any())).thenReturn(TestData.readinessProfile)
    var dpsPrincipal = DpsPrincipal("username", "displayName")
    val result = mvc.perform(post("/readiness-profiles/A1234AB").accept(APPLICATION_JSON).content(TestData.createProfileJsonRequest).contentType(APPLICATION_JSON).principal(DpsPrincipal("test", "test")).headers((setAuthorisation(roles = listOf("WORK_READINESS_EDIT")))))
      .andExpect(status().isOk)
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn()
    val actualReadinessProfileDTO = mapper.readValue(
      result.response.contentAsString,
      object : TypeReference<ReadinessProfileDTO>() {},
    )
    Assertions.assertThat(actualReadinessProfileDTO).extracting(TestData.createdByString, TestData.offenderIdString, TestData.bookingIdString)
      .contains(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId)
    verify(profileService, times(1)).createProfileForOffender(any(), any(), any(), any())
  }

  @Test
  fun `Test Post  of an update profile `() {
    whenever(profileService.createProfileForOffender(any(), any(), any(), any())).thenReturn(TestData.readinessProfile)
    val result = mvc.perform(post("/readiness-profiles/A1234AB").accept(APPLICATION_JSON).content(TestData.createProfileJsonRequest).contentType(APPLICATION_JSON).principal(DpsPrincipal("test", "test")).headers((setAuthorisation(roles = listOf("WORK_READINESS_EDIT")))))
      .andExpect(status().isOk)
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn()
    val actualReadinessProfileDTO = mapper.readValue(
      result.response.contentAsString,
      object : TypeReference<ReadinessProfileDTO>() {},
    )
    Assertions.assertThat(actualReadinessProfileDTO).extracting(TestData.createdByString, TestData.offenderIdString, TestData.bookingIdString)
      .contains(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId)
    verify(profileService, times(1)).createProfileForOffender(any(), any(), any(), any())
  }

  @Test
  fun `Test Get  profile of an  Offender `() {
    whenever(profileService.getProfileForOffender(any())).thenReturn(TestData.readinessProfile)
    val result = mvc.perform(get("/readiness-profiles/A1234AB").accept(APPLICATION_JSON).content(TestData.createProfileJsonRequest).contentType(APPLICATION_JSON).param("oauth2User", "ssss").headers((setAuthorisation(roles = listOf("WORK_READINESS_EDIT")))))
      .andExpect(status().isOk)
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn()
    val actualReadinessProfileDTO = mapper.readValue(
      result.response.contentAsString,
      object : TypeReference<ReadinessProfileDTO>() {},
    )
    Assertions.assertThat(actualReadinessProfileDTO).extracting(TestData.createdByString, TestData.offenderIdString, TestData.bookingIdString)
      .contains(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId)
    verify(profileService, times(1)).getProfileForOffender(any())
  }

  @Test
  fun `Test Get  of an  profile list for offenders `() {
    whenever(profileService.getProfilesForOffenders(any())).thenReturn(TestData.profileList)
    val result = mvc.perform(post("/readiness-profiles/search").accept(APPLICATION_JSON).content(TestData.offenderIdListjson).contentType(APPLICATION_JSON).headers((setAuthorisation(roles = listOf("WORK_READINESS_EDIT")))))
      .andExpect(status().isOk)
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn()
    val readinessProfileDTOList = mapper.readValue(
      result.response.contentAsString,
      object : TypeReference<List<ReadinessProfileDTO>>() {},
    )
    Assertions.assertThat(readinessProfileDTOList[0]).extracting(TestData.createdByString, TestData.offenderIdString, TestData.bookingIdString)
      .contains(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId)
    verify(profileService, times(1)).getProfilesForOffenders(any())
  }

  internal fun setAuthorisation(
    user: String = "test-client",
    roles: List<String> = listOf(),
  ): (HttpHeaders) = jwtAuthHelper.setAuthorisationForUnitTests(user, roles)
}
