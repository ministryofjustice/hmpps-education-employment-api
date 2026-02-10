package uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.lenient
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.auditing.AuditingHandler
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.AuditObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.infrastructure.AuditCleaner
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfileRepository
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
  protected lateinit var readinessProfileRepository: ReadinessProfileRepository

  @Autowired
  protected lateinit var auditCleaner: AuditCleaner

  val defaultTimezoneId = AuditObjects.defaultTimezoneId
  val defaultCurrentTime: Instant = AuditObjects.defaultAuditTime
  val defaultCurrentTimeLocal: LocalDateTime get() = defaultCurrentTime.atZone(defaultTimezoneId).toLocalDateTime()

  @BeforeAll
  override fun beforeAll() {
    super.beforeAll()
    auditingHandler.setDateTimeProvider(dateTimeProvider)
  }

  @BeforeEach
  internal fun setUp() {
    readinessProfileRepository.deleteAll()
    auditCleaner.deleteAllRevisions()

    lenient().whenever(dateTimeProvider.now).thenReturn(Optional.of(defaultCurrentTime))
    lenient().whenever(timeProvider.timezoneId).thenReturn(defaultTimezoneId)
    lenient().whenever(timeProvider.now()).thenReturn(defaultCurrentTimeLocal)
  }

  protected fun <RESP, REQ> assertRequest(
    url: String,
    method: HttpMethod,
    requestEntity: HttpEntity<REQ & Any>,
    responseType: Class<RESP & Any>,
    expectedStatus: HttpStatus,
  ) = restTemplate.exchange(url, method, requestEntity, responseType).also { result ->
    assertThat(result).isNotNull
    assertThat(result.statusCode).isEqualTo(expectedStatus)
  }

  protected fun <RESP, REQ> assertRequest(
    url: String,
    method: HttpMethod,
    requestEntity: HttpEntity<REQ & Any>,
    responseType: ParameterizedTypeReference<RESP & Any>,
    expectedStatus: HttpStatus,
  ) = restTemplate.exchange(url, method, requestEntity, responseType).also { result ->
    assertThat(result).isNotNull
    assertThat(result.statusCode).isEqualTo(expectedStatus)
  }

  protected fun assertRequest(
    url: String,
    method: HttpMethod,
    requestEntity: HttpEntity<*>,
    expectedStatus: HttpStatus,
    expectedResponse: String? = null,
  ) = restTemplate.exchange(url, method, requestEntity, String::class.java).also { result ->
    assertThat(result).isNotNull
    assertThat(result.statusCode).isEqualTo(expectedStatus)
    expectedResponse?.let { assertThat(result.body).isEqualTo(expectedResponse) }
  }
}
