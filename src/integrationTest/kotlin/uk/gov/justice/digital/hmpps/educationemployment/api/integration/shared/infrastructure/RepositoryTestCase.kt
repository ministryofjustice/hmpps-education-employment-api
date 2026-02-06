package uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.infrastructure

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.lenient
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.domain.AuditorAware
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.AuditObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.config.TestJpaConfig
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.testcontainers.PostgresContainer
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfileRepository
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@DataJpaTest
@Import(TestJpaConfig::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("repository-test")
abstract class RepositoryTestCase {
  @Autowired
  protected lateinit var dateTimeProvider: DateTimeProvider

  @Autowired
  protected lateinit var auditorProvider: AuditorAware<String>

  @Autowired
  protected lateinit var readinessProfileRepository: ReadinessProfileRepository

  @Autowired
  protected lateinit var auditCleaner: AuditCleaner

  protected val objectMapper: ObjectMapper = jacksonObjectMapper().apply { registerModule(JavaTimeModule()) }

  protected final val defaultTimezoneId = AuditObjects.defaultTimezoneId
  protected final val defaultCurrentTime = AuditObjects.defaultAuditTime
  protected final val defaultCurrentTimeLocal: LocalDateTime get() = defaultCurrentTime.atZone(defaultTimezoneId).toLocalDateTime()
  protected final val defaultAuditor = AuditObjects.defaultAuditor

  protected open val currentTime: Instant get() = defaultCurrentTime
  protected val currentTimeLocal: LocalDateTime get() = defaultCurrentTimeLocal

  protected var auditor = AuditObjects.defaultAuditor

  protected val emptyJsonArray: JsonNode get() = objectMapper.readTree("[]")

  companion object {
    private val postgresContainer = PostgresContainer.repositoryContainer

    @JvmStatic
    @DynamicPropertySource
    fun configureTestContainers(registry: DynamicPropertyRegistry) {
      postgresContainer?.run {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
        registry.add("spring.datasource.username", postgresContainer::getUsername)
        registry.add("spring.datasource.password", postgresContainer::getPassword)
      }
    }
  }

  @BeforeEach
  internal open fun setUp() {
    readinessProfileRepository.deleteAll()
    auditCleaner.deleteAllRevisions()

    lenient().whenever(dateTimeProvider.now).thenAnswer { Optional.of(currentTime) }
    setCurrentAuditor()
  }

  protected fun setCurrentAuditor(username: String = defaultAuditor) {
    auditor = username
    lenient().whenever(auditorProvider.currentAuditor).thenAnswer { Optional.of(auditor) }
  }
}
