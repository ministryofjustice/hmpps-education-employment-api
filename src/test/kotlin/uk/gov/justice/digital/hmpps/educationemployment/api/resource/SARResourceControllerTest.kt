@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.resource

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration

const val SAR_ENDPOINT = "/subject-access-request"

@WebMvcTest(controllers = [SARResourceController::class])
@ContextConfiguration(classes = [SARResourceController::class])
class SARResourceControllerTest : ControllerTestBase() {
  /*
  @MockitoBean
  private lateinit var profileService: ProfileV1Service

  companion object {
    private const val ROLE_SAR = "ROLE_SAR_DATA_ACCESS"
  }

  @BeforeEach
  fun reset() {
    reset(profileService)
    initMvcMock(SARResourceController(profileService))
  }

  @Test
  fun `Test Get profile of an Offender for SAR`() {
    val prn = ProfileObjects.anotherPrisonNumber
    whenever(profileService.getProfileForOffenderFilterByPeriod(eq(prn), isNull(), isNull())).thenReturn(readinessProfileForSAR)

    val result = assertRetrieveSARProfileIsOk(prn)

    val jsonSARProfile = objectMapper.readTree(result.response.contentAsString)
    val jsonContent = jsonSARProfile.findPath("content")
    assertThat(jsonContent.isMissingNode).isFalse()
    assertThat(jsonContent.get("offenderId").textValue()).isEqualTo(prn)
    jsonContent.findPath("profileData").let { jsonProfile ->
      assertThat(jsonProfile.isMissingNode).isFalse()
      assertThat(jsonProfile.get("supportDeclined")).isNotEmpty
      assertThat(jsonProfile.get("supportDeclined_history")).isNotEmpty
      assertThat(jsonProfile.get("supportAccepted_history")).isNotEmpty
    }
  }

  @Test
  fun `Get No Content of unknown offender for SAR`() {
    val prn = ProfileObjects.unknownPrisonNumber
    whenever(profileService.getProfileForOffenderFilterByPeriod(eq(prn), isNull(), isNull())).thenThrow(NotFoundException(prn))

    assertRetrieveSARProfileIsNoContent(prn)
  }

  private fun assertRetrieveSARProfileIsOk(prn: String) = assertRetrieveSARProfile(prn).also {
    verify(profileService, times(1)).getProfileForOffenderFilterByPeriod(eq(prn), isNull(), isNull())
  }

  private fun assertRetrieveSARProfileIsNoContent(prn: String) = assertApiReplyEmptyBody(
    get(SAR_ENDPOINT).param("prn", prn),
    principal = dpsPrincipal,
    role = ROLE_SAR,
    resultMatcher = status().isNoContent,
  )

  private fun assertRetrieveSARProfile(prn: String, resultMatcher: ResultMatcher = status().isOk) = assertApiReplyJson(
    get(SAR_ENDPOINT).param("prn", prn),
    principal = dpsPrincipal,
    role = ROLE_SAR,
    resultMatcher = resultMatcher,
  )
*/
}
