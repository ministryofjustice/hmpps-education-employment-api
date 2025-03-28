package uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.application

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.auditing.AuditingHandler
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.AuditObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationemployment.api.repository.ReadinessProfileRepository
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.DefaultTimeProvider
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class ApplicationTestCase : IntegrationTestBase() {
  @MockitoBean
  protected lateinit var dateTimeProvider: DateTimeProvider

  @MockitoSpyBean
  protected lateinit var timeProvider: DefaultTimeProvider

  @MockitoSpyBean
  protected lateinit var auditingHandler: AuditingHandler

  @Autowired
  lateinit var readinessProfileRepository: ReadinessProfileRepository

  val defaultTimezoneId = AuditObjects.defaultTimezoneId
  val defaultCurrentTime: Instant = AuditObjects.defaultAuditTime
  val defaultCurrentTimeLocal: LocalDateTime get() = defaultCurrentTime.atZone(defaultTimezoneId).toLocalDateTime()

  @BeforeAll
  internal fun beforeAll() {
    auditingHandler.setDateTimeProvider(dateTimeProvider)
  }

  @BeforeEach
  internal fun setup() {
    whenever(dateTimeProvider.now).thenReturn(Optional.of(defaultCurrentTime))
    whenever(timeProvider.timezoneId).thenReturn(defaultTimezoneId)
    whenever(timeProvider.now()).thenReturn(defaultCurrentTimeLocal)
  }
}
