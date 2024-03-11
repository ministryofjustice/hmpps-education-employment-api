package uk.gov.justice.digital.hmpps.educationemploymentapi.integration

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.educationemploymentapi.HmppsEducationEmploymentApi
import uk.gov.justice.digital.hmpps.educationemploymentapi.helpers.JwtAuthHelper

// import uk.gov.justice.digital.hmpps.educationemploymentapi.integration.wiremock.OAuthExtension
/*
** The abstract parent class for integration tests.
**
**  It supplies : -
**     - The SpringBootTest annotation.
**     - The active profile "test"
**     - An extension class providing a Wiremock hmpps-auth server.
**     - A JwtAuthHelper function.
**     - A WebTestClient.
**     - An ObjectMapper called mapper.
**     - A logger.
**     - SQL reset and load scripts to reset reference data - tests can then load what they need.
*/

@SpringBootTest(
  webEnvironment = RANDOM_PORT,
  classes = arrayOf(
    HmppsEducationEmploymentApi::class,
  ),
)
@ActiveProfiles("test")
class IntegrationTestBase internal constructor() {

  @Autowired
  lateinit var restTemplate: TestRestTemplate

  @Autowired
  lateinit var jwtAuthHelper: JwtAuthHelper
  internal fun setAuthorisation(
    user: String = "test-client",
    roles: List<String> = listOf(),
  ): (HttpHeaders) {
    return jwtAuthHelper.setAuthorisationForUnitTests(user, roles)
  }
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
