package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.dashboard

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus.SUPPORT_DECLINED
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus.SUPPORT_NEEDED
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.GetMetricsWorkStatusResponse
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.MetricsProfileStatusCount
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects

class MetricsWorkStatusGetShould : MetricsWorkStatusTestCase() {
  private val emptyMetricResponse: String get() = GetMetricsWorkStatusResponse(0, emptyList()).metricsResponse

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
      assertGetMetricsIsOk(prisonId, today, tomorrow, emptyMetricResponse)
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
    private val yetAnotherPrison = ProfileObjects.prison1

    @BeforeEach
    fun setUp() {
      givenMoreProfilesFromMultiplePrisons()
      // change one profile status (NO_RIGHT_TO_WORK -> SUPPORT_DECLINED)
      ProfileObjects.profileOfKnownPrisoner.let {
        val statusChangeRequest = objectMapper.treeToValue(it.profileData, Profile::class.java).statusChangeRequestToDeclined()
        assertChangeStatusIsOk(it.offenderId, statusChangeRequest)
      }
    }

    @Test
    fun `return correct counts at metrics, given prison with profiles of status change`() {
      val expectedResponse = metric(1, listOf(SUPPORT_DECLINED, SUPPORT_NEEDED).map { statusCount(it, 1, 0) })

      assertGetMetricsIsOk(prisonId, yesterday, tomorrow, expectedResponse)
    }

    @Test
    fun `return correct counts at metrics, given prison without profiles of status change`() {
      val expectedResponse = metric(0, statusCount(SUPPORT_DECLINED, 0, 1))

      assertGetMetricsIsOk(yetAnotherPrison, yesterday, tomorrow, expectedResponse)
    }

    @Test
    fun `return empty count at metrics, that profiles of other prison(s) will be excluded`() {
      assertGetMetricsIsOk(anotherPrison, yesterday, tomorrow, emptyMetricResponse)
    }

    @Test
    fun `return empty count at metrics, that profiles were created or updated BEFORE reporting period`() {
      assertGetMetricsIsOk(prisonId, yesterday, yesterday, emptyMetricResponse)
    }

    @Test
    fun `return empty count at metrics, that applications were created or updated AFTER reporting period`() {
      assertGetMetricsIsOk(prisonId, tomorrow, tomorrow, emptyMetricResponse)
    }
  }

  private fun metric(statusChangeCount: Long, vararg statusCounts: MetricsProfileStatusCount) = metric(statusChangeCount, statusCounts.toList())
  private fun metric(statusChangeCount: Long, statusCounts: List<MetricsProfileStatusCount>) = GetMetricsWorkStatusResponse(
    numberOfPrisonersStatusChange = statusChangeCount,
    statusCounts = statusCounts,
  ).metricsResponse

  private fun statusCount(
    profileStatus: ProfileStatus,
    numberOfPrisonersWithin12Weeks: Long,
    numberOfPrisonersOver12Weeks: Long,
  ) = MetricsProfileStatusCount(profileStatus.name, numberOfPrisonersWithin12Weeks, numberOfPrisonersOver12Weeks)
}
