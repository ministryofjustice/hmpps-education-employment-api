package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.dashboard

import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.whenever
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.WR_VIEW_ROLE
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.v2.ReadinessProfileV2TestCase
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.infrastructure.TestClock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

const val DASHBOARD_ENDPOINT = "/dashboard"

abstract class DashboardTestCase : ReadinessProfileV2TestCase() {
  protected val testClock: TestClock = TestClock.defaultClock()
  protected val currentTime: Instant get() = testClock.instant
  protected val currentDate: LocalDate get() = LocalDate.ofInstant(currentTime, ZoneOffset.UTC)

  private val readOnlyHeaders get() = setAuthorisationOfRoles(WR_VIEW_ROLE)
  private val readOnlyRequestNoBody get() = HttpEntity<HttpHeaders>(readOnlyHeaders)

  @BeforeEach
  internal fun setUpDashboardTestCase() {
    whenever(dateTimeProvider.now).thenAnswer { Optional.of(currentTime) }
  }

  protected fun assertRequestWithoutBody(
    url: String,
    method: HttpMethod,
    expectedStatus: HttpStatus,
    expectedResponse: String? = null,
  ) = assertRequest(url, method, readOnlyRequestNoBody, expectedStatus, expectedResponse)
}
