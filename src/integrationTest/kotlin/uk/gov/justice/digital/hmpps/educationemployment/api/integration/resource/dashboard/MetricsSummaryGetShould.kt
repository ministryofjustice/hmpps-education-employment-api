package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.dashboard

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.GetMetricsSummaryResponse
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects

class MetricsSummaryGetShould : MetricsSummaryTestCase() {
  private val zeroMetricResponse: String get() = GetMetricsSummaryResponse(0, 0, 0, 0).metricsResponse

  @Nested
  @DisplayName("Given invalid request parameter(s)")
  inner class GivenInvalidRequestParameter {
    @Test
    fun `return error, when missing all parameters`() {
      assertGetMetricsReturnsBadRequestError()
    }

    @Test
    fun `return error, when missing parameter prisonId`() {
      assertGetMetricsReturnsBadRequestError(
        parameters = "dateFrom=$currentDate&dateTo=$currentDate",
        expectedResponse = expectedErrorMessageMissingParameter("prisonId"),
      )
    }

    @Test
    fun `return error, when missing parameter dateFrom`() {
      assertGetMetricsReturnsBadRequestError(
        parameters = "prisonId=MDI&dateTo=$currentDate",
        expectedResponse = expectedErrorMessageMissingDateParameter("dateFrom"),
      )
    }

    @Test
    fun `return error, when missing parameter dateTo`() {
      assertGetMetricsReturnsBadRequestError(
        parameters = "prisonId=MDI&dateFrom=$currentDate",
        expectedResponse = expectedErrorMessageMissingDateParameter("dateTo"),
      )
    }

    @Test
    fun `return error, when invalid format of date parameter has been specified`() {
      val invalidDate = "2099_12_31"
      assertGetMetricsReturnsBadRequestError(
        parameters = "prisonId=MDI&dateFrom=$invalidDate&dateTo=$invalidDate",
        expectedResponse = expectedErrorMessageParameterTypeMismatch("dateFrom", invalidDate),
      )
    }

    @Test
    fun `return error, when invalid value of date parameter has been specified`() {
      val invalidDate = "abc"
      assertGetMetricsReturnsBadRequestError(
        parameters = "prisonId=MDI&dateFrom=$invalidDate&dateTo=$invalidDate",
        expectedResponse = expectedErrorMessageParameterTypeMismatch("dateFrom", invalidDate),
      )
    }

    @Test
    fun `return error, when invalid reporting period has been specified`() {
      val dateFrom = "2024-02-01"
      val dateTo = "2024-01-31"
      assertGetMetricsReturnsBadRequestError(
        parameters = "prisonId=MDI&dateFrom=$dateFrom&dateTo=$dateTo",
        expectedResponse = expectedErrorMessageInvalidDatePeriod(dateFrom, dateTo),
      )
    }
  }

  @Nested
  @DisplayName("Given no profile, with the given prisonId")
  inner class GivenNoProfile {
    private val prisonId = ProfileObjects.prison1

    @Test
    fun `return empty count at metrics`() {
      val today = currentDate
      val tomorrow = today.plusDays(1)
      assertGetMetricsIsOk(prisonId, today, tomorrow, zeroMetricResponse)
    }
  }

  @Nested
  @DisplayName("Given some profiles, with the given prisonId")
  inner class GivenSomeProfiles {
    private val yesterday = currentDate.minusDays(1)
    private val today = currentDate
    private val tomorrow = today.plusDays(1)
    private val prisonId = ProfileObjects.prisonId
    private val anotherPrison = ProfileObjects.prison3

    @BeforeEach
    fun setUp() {
      givenMoreProfilesFromMultiplePrisons()
    }

    @Test
    fun `return correct counts at metrics, given prison with profiles`() {
      val expectedResponse = metric(2, 0, 0, 1)

      assertGetMetricsIsOk(prisonId, yesterday, tomorrow, expectedResponse)
    }

    @Test
    fun `return zero count at metrics, that profiles of other prison(s) will be excluded`() {
      assertGetMetricsIsOk(anotherPrison, yesterday, tomorrow, zeroMetricResponse)
    }

    @Test
    fun `return zero count at metrics, that profiles were created or updated BEFORE reporting period`() {
      assertGetMetricsIsOk(prisonId, yesterday, yesterday, zeroMetricResponse)
    }

    @Test
    fun `return zero count at metrics, that applications were created or updated AFTER reporting period`() {
      assertGetMetricsIsOk(prisonId, tomorrow, tomorrow, zeroMetricResponse)
    }
  }

  private fun metric(
    numberOfPrisonersWithin12Weeks: Long,
    numberOfPrisonersOver12Weeks: Long,
    numberOfSupportDeclined: Long,
    numberOfNoRightToWork: Long,
  ) = GetMetricsSummaryResponse(
    numberOfPrisonersWithin12Weeks,
    numberOfPrisonersOver12Weeks,
    numberOfSupportDeclined,
    numberOfNoRightToWork,
  ).metricsResponse
}
