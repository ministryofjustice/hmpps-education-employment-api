package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfileRepository
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.UnitTestBase
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.infrastructure.MetricsCountForTest
import java.time.LocalDate
import kotlin.test.assertContentEquals

class ProfileMetricsServiceTest : UnitTestBase() {
  @Mock
  private lateinit var readinessProfileRepository: ReadinessProfileRepository

  @InjectMocks
  private lateinit var profileMetricsService: ProfileMetricsService

  private val prisonId = "MDI"
  private val dateFrom = LocalDate.of(2021, 1, 1)
  private val dateTo = LocalDate.of(2021, 1, 31)

  @Nested
  @DisplayName("Given none of readiness profile")
  inner class GivenNoProfile {
    @BeforeEach
    internal fun setUp() {
      lenient().whenever(readinessProfileRepository.countReasonsForSupportDeclinedByPrisonIdAndDateTimeBetween(eq(prisonId), any(), any()))
        .thenReturn(emptyList())
    }

    @Test
    fun `return nothing for metric - Reasons for not wanting support`() {
      val actual = profileMetricsService.retrieveMetricsReasonsSupportDeclinedByPrisonIdAndDates(prisonId, dateFrom, dateTo)
      assertThat(actual).isEmpty()
    }
  }

  @Nested
  @DisplayName("Given some existing readiness profiles")
  inner class GivenExistingProfiles {
    @Test
    fun `return counts for metric - Reasons for not wanting support`() {
      val metricCounts = listOf(
        makeMetricsCount(SupportToWorkDeclinedReason.HEALTH, 11, 2),
        makeMetricsCount(SupportToWorkDeclinedReason.RETIRED, 5, 8),
        makeMetricsCount(SupportToWorkDeclinedReason.LIMIT_THEIR_ABILITY, 3, 4),
      )
      whenever(readinessProfileRepository.countReasonsForSupportDeclinedByPrisonIdAndDateTimeBetween(eq(prisonId), any(), any())).thenReturn(metricCounts)
      val expected = metricCounts.map { it.reasonsDeclinedResponse() }.toList()

      val actual = profileMetricsService.retrieveMetricsReasonsSupportDeclinedByPrisonIdAndDates(prisonId, dateFrom, dateTo)

      assertContentEquals(expected, actual)
    }
  }

  private fun makeMetricsCount(reasonForSupportDeclined: SupportToWorkDeclinedReason, countWithin12Weeks: Long, countOver12Weeks: Long) = makeMetricsCount(reasonForSupportDeclined.name, countWithin12Weeks, countOver12Weeks)

  private fun makeMetricsCount(field: String, countWithin12Weeks: Long = 0, countOver12Weeks: Long = 0) = MetricsCountForTest(field, countWithin12Weeks, countOver12Weeks)

  private fun MetricsCountForTest.reasonsDeclinedResponse() = GetMetricsReasonsSupportDeclinedResponse(field, countWithin12Weeks, countOver12Weeks)
}
