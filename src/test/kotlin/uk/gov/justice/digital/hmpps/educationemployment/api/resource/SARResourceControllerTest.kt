@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.reset
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ProfileV2Service
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.V2Profiles.readinessProfileForSAR

const val SAR_ENDPOINT = "/subject-access-request"

@WebMvcTest(controllers = [SARResourceController::class])
@ContextConfiguration(classes = [SARResourceController::class])
class SARResourceControllerTest : ControllerTestBase() {
  @MockitoBean
  private lateinit var profileService: ProfileV2Service

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
    }
  }

  @Test
  fun `Get No Content of unknown offender for SAR`() {
    val prn = ProfileObjects.unknownPrisonNumber
    whenever(profileService.getProfileForOffenderFilterByPeriod(eq(prn), isNull(), isNull())).thenThrow(NotFoundException(prn))
    /*
    So the NFE is a checked exception but as the Kotlin code does not explicitly declare the throws - Mockito is unable to
    throw the Exception. So the workaround is to declare it as a Runtime (unchecked exception instead)
     */
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
}
