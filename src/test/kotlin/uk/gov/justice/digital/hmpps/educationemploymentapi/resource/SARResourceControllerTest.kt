package uk.gov.justice.digital.hmpps.educationemploymentapi.resource

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
import org.springframework.boot.context.properties.bind.Bindable.listOf
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import uk.gov.justice.digital.hmpps.educationemploymentapi.config.ControllerAdvice
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.SARReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemploymentapi.helpers.JwtAuthHelper
import uk.gov.justice.digital.hmpps.educationemploymentapi.service.ProfileService
import uk.gov.justice.digital.hmpps.educationemploymentapi.service.TestData

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@WebMvcTest(controllers = [SARResourceController::class])
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = [SARResourceController::class])
@WebAppConfiguration
class SARResourceControllerTest {

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
      .standaloneSetup(SARResourceController(profileService))
      .setControllerAdvice(ControllerAdvice())
      .build()
  }

  @Test
  fun `Test Get  profile of an  Offender for SAR `() {
    whenever(profileService.getProfileForOffender(any())).thenReturn(TestData.readinessProfileForSAR)
    val result = mvc.perform(get("/subject-access-request/A1234AB").accept(APPLICATION_JSON).content(TestData.createProfileJsonRequest).contentType(APPLICATION_JSON).param("oauth2User", "ssss").headers((setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))))
      .andExpect(status().isOk)
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn()
    val actualSARReadinessProfileDTO = mapper.readValue(
      result.response.contentAsString,
      object : TypeReference<SARReadinessProfileDTO>() {},
    )
    Assertions.assertThat(actualSARReadinessProfileDTO).extracting(TestData.createdByString, TestData.offenderIdString, TestData.bookingIdString, TestData.noteDataString)
      .contains(TestData.createdBy, TestData.newOffenderId, TestData.newBookingId, TestData.note)
    verify(profileService, times(1)).getProfileForOffender(any())
  }

  internal fun setAuthorisation(
    user: String = "test-client",
    roles: List<String> = listOf(),
  ): (HttpHeaders) {
    return jwtAuthHelper.setAuthorisationForUnitTests(user, roles)
  }
}
