package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.dashboard

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason.ALREADY_HAS_WORK
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason.FULL_TIME_CARER
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason.RETURNING_TO_JOB
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason.SELF_EMPLOYED
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.GetMetricsReasonsSupportDeclinedResponse
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.prison1

class MetricsReasonsSupportDeclinedGetShould : MetricsReasonsSupportDeclinedTestCase() {
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
      assertGetMetricsIsOk(prisonId, today, tomorrow, "[]")
    }
  }

  @Nested
  @DisplayName("Given some profiles, with the given prisonId")
  inner class GivenSomeProfiles {
    private val yesterday = currentDate.minusDays(1)
    private val today = currentDate
    private val tomorrow = today.plusDays(1)
    private val prisonId = prison1
    private val anotherPrison = ProfileObjects.prisonId

    @BeforeEach
    fun setUp() {
      givenMoreProfilesFromMultiplePrisons()
    }

    @Test
    fun `return correct counts at metrics, given prison with one profile`() {
      val expectedResponse = listOf(
        ALREADY_HAS_WORK,
        FULL_TIME_CARER,
        RETURNING_TO_JOB,
        SELF_EMPLOYED,
      ).map { metric(it, 0, 1) }.metricsResponses

      assertGetMetricsIsOk(prisonId, yesterday, tomorrow, expectedResponse)
    }

    @Test
    fun `return empty count at metrics, that profiles of other prison(s) will be excluded`() {
      assertGetMetricsIsOk(anotherPrison, yesterday, tomorrow, "[]")
    }

    @Test
    fun `return empty count at metrics, that profiles were created or updated BEFORE reporting period`() {
      assertGetMetricsIsOk(prisonId, yesterday, yesterday, "[]")
    }

    @Test
    fun `return empty count at metrics, that applications were created or updated AFTER reporting period`() {
      assertGetMetricsIsOk(prisonId, tomorrow, tomorrow, "[]")
    }
  }

  private fun metric(
    supportToWorkDeclinedReason: SupportToWorkDeclinedReason,
    numberOfPrisonersWithin12Weeks: Long,
    numberOfPrisonersOver12Weeks: Long,
  ) = GetMetricsReasonsSupportDeclinedResponse(supportToWorkDeclinedReason.name, numberOfPrisonersWithin12Weeks, numberOfPrisonersOver12Weeks)
}
