package uk.gov.justice.digital.hmpps.educationemployment.api.integration
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import uk.gov.justice.digital.hmpps.educationemployment.api.HmppsEducationEmploymentApi
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.helpers.JwtAuthHelper
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.testcontainers.PostgresContainer

@SpringBootTest(
  webEnvironment = RANDOM_PORT,
  classes = arrayOf(
    HmppsEducationEmploymentApi::class,
  ),
)
@AutoConfigureTestRestTemplate
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("integration-test")
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
  lateinit var jwtAuthHelper: JwtAuthHelper

  internal fun setAuthorisationOfRoles(vararg roles: String, user: String = authUser): HttpHeaders = setAuthorisation(user, listOf(*roles))

  internal fun setAuthorisation(
    user: String = authUser,
    roles: List<String> = listOf(),
  ): (HttpHeaders) = jwtAuthHelper.setAuthorisationForUnitTests(user, roles)

  protected val authUser = "test-client"

  @BeforeAll
  internal fun beforeAll() {
    flyway.clean()
    flyway.migrate()
  }
}
