package uk.gov.justice.digital.hmpps.educationemploymentapi.resource

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import uk.gov.justice.digital.hmpps.educationemploymentapi.config.ControllerAdvice
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.ActionTodo
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Note
import uk.gov.justice.digital.hmpps.educationemploymentapi.service.ProfileService
import javax.validation.Validator

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
  private lateinit var validator: Validator

  @Autowired
  private lateinit var mapper: ObjectMapper

  private val noteListJson = "[\n" +
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

  @BeforeEach
  fun reset() {
    reset(profileService)

    mvc = MockMvcBuilders
      .standaloneSetup(ProfileResourceController(profileService, validator))
      .setControllerAdvice(ControllerAdvice())
      .build()
  }

  @Test
  fun `Test POST of a PRELIMINARY receive`() {
    val prisonerId = "A1234AB"
    val bankAccount = ActionTodo.BANK_ACCOUNT.toString()
    var notesList: MutableList<Note> = mapper.readValue(noteListJson, object : TypeReference<MutableList<Note>>() {})
    whenever(profileService.getProfileNotesForOffender(prisonerId, ActionTodo.BANK_ACCOUNT)).thenReturn(notesList)

    val result = mvc.perform(get("/readiness-profiles/A1234AB/notes/$bankAccount").accept(APPLICATION_JSON))
      .andExpect(status().isOk)
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn()

//    assertThat(result.response.contentAsString).isEqualTo(mapper.writeValueAsString(notesList))
    verify(profileService, times(1)).getProfileNotesForOffender(prisonerId, ActionTodo.BANK_ACCOUNT)
  }
}
