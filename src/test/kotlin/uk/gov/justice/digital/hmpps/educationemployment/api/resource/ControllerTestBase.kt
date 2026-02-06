package uk.gov.justice.digital.hmpps.educationemployment.api.resource

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.AuditObjects.testClient
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.AuditObjects.testPrincipal
import uk.gov.justice.digital.hmpps.educationemployment.api.config.ControllerAdvice
import uk.gov.justice.digital.hmpps.educationemployment.api.config.DpsPrincipal
import uk.gov.justice.digital.hmpps.educationemployment.api.helpers.JwtAuthHelper
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.UnitTestBase
import java.security.Principal

@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WebAppConfiguration
@EnableWebSecurity
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class ControllerTestBase : UnitTestBase() {
  @Autowired
  protected lateinit var mvc: MockMvc

  private lateinit var jwtAuthHelper: JwtAuthHelper

  protected lateinit var dpsPrincipal: DpsPrincipal

  companion object {
    protected const val ROLE_READWRITE = "WORK_READINESS_EDIT"
    protected const val ROLE_READONLY = "WORK_READINESS_VIEW"
  }

  @BeforeAll
  internal fun beforeAll() {
    jwtAuthHelper = JwtAuthHelper()
    dpsPrincipal = DpsPrincipal(testPrincipal, testPrincipal)
  }

  protected fun setAuthorisation(
    user: String = testClient,
    roles: List<String> = listOf(),
  ): (HttpHeaders) = jwtAuthHelper.setAuthorisationForUnitTests(user, roles)

  protected fun initMvcMock(controller: Any) {
    SecurityMockMvcConfigurers.springSecurity()

    mvc = MockMvcBuilders
      .standaloneSetup(controller)
      .setControllerAdvice(ControllerAdvice())
      .build()
  }

  protected fun assertReadWriteApiReplyJson(
    requestBuilder: MockHttpServletRequestBuilder,
    requestJson: String? = null,
    resultMatcher: ResultMatcher = status().isOk,
  ) = assertApiReplyJson(requestBuilder, requestJson, dpsPrincipal, ROLE_READWRITE, resultMatcher)

  protected fun assertReadOnlyApiReplyJson(
    requestBuilder: MockHttpServletRequestBuilder,
    requestJson: String? = null,
    resultMatcher: ResultMatcher = status().isOk,
  ) = assertApiReplyJson(requestBuilder, requestJson, dpsPrincipal, ROLE_READONLY, resultMatcher)

  protected fun assertApiReplyJson(
    requestBuilder: MockHttpServletRequestBuilder,
    requestJson: String? = null,
    principal: Principal? = null,
    role: String? = null,
    resultMatcher: ResultMatcher = status().isOk,
  ): MvcResult {
    requestJson?.let { requestBuilder.content(requestJson).contentType(APPLICATION_JSON) }
    principal?.let { requestBuilder.principal(principal) }
    role?.let { requestBuilder.headers(setAuthorisation(roles = listOf(role))) }
    return buildApiRequest(requestBuilder, resultMatcher).andExpect(content().contentType(APPLICATION_JSON)).andReturn()
  }

  protected fun assertApiReplyEmptyBody(
    requestBuilder: MockHttpServletRequestBuilder,
    requestJson: String? = null,
    principal: Principal? = null,
    role: String? = null,
    resultMatcher: ResultMatcher = status().isOk,
  ): MvcResult {
    requestJson?.let { requestBuilder.content(requestJson).contentType(APPLICATION_JSON) }
    principal?.let { requestBuilder.principal(principal) }
    role?.let { requestBuilder.headers(setAuthorisation(roles = listOf(role))) }
    return buildApiRequest(requestBuilder, resultMatcher).andReturn()
  }

  protected fun buildApiRequest(
    requestBuilder: MockHttpServletRequestBuilder,
    resultMatcher: ResultMatcher = status().isOk,
  ) = mvc.perform(requestBuilder.accept(APPLICATION_JSON)).andExpect(resultMatcher)
}
