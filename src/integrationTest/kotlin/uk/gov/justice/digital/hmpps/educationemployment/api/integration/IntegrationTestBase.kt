package uk.gov.justice.digital.hmpps.educationemployment.api.integration
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import uk.gov.justice.digital.hmpps.educationemployment.api.HmppsEducationEmploymentApi
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.testcontainers.PostgresContainer
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelperConfig
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

const val DEFAULT_USER = "test-client"

@SpringBootTest(
  webEnvironment = RANDOM_PORT,
  classes = arrayOf(
    HmppsEducationEmploymentApi::class,
  ),
)
@AutoConfigureTestRestTemplate
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("integration-test")
@Import(SarIntegrationTestHelperConfig::class)
abstract class IntegrationTestBase internal constructor() {
  @Autowired
  private lateinit var flyway: Flyway

  companion object {

    private val postgresContainer = PostgresContainer.flywayContainer

    @JvmStatic
    @DynamicPropertySource
    fun configureTestContainers(registry: DynamicPropertyRegistry) {
      postgresContainer?.run {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
        registry.add("spring.datasource.username", postgresContainer::getUsername)
        registry.add("spring.datasource.password", postgresContainer::getPassword)
        registry.add("spring.datasource.placeholders.database_update_password", postgresContainer::getPassword)
        registry.add("spring.datasource.placeholders.database_read_only_password", postgresContainer::getPassword)
        registry.add("spring.flyway.url", postgresContainer::getJdbcUrl)
        registry.add("spring.flyway.user", postgresContainer::getUsername)
        registry.add("spring.flyway.password", postgresContainer::getPassword)
      }
    }
  }

  @Autowired
  lateinit var restTemplate: TestRestTemplate

  @Autowired
  protected lateinit var jwtAuthorisationHelper: JwtAuthorisationHelper

  @Autowired
  protected lateinit var sarIntegrationTestHelper: SarIntegrationTestHelper

  protected fun httpHeaders(vararg roles: String, user: String = currentUser) = httpHeaders(listOf(*roles), user)
  protected fun httpHeaders(roles: List<String>, user: String = currentUser) = HttpHeaders().also { setAuthorisation(user, roles).invoke(it) }

  internal fun setAuthorisation(
    username: String? = DEFAULT_USER,
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthorisationHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected var currentUser = DEFAULT_USER

  @BeforeAll
  internal fun beforeAll() {
    flyway.clean()
    flyway.migrate()
  }
}
