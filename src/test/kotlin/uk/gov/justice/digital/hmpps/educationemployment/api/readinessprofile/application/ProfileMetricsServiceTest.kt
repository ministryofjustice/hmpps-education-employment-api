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
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfileRepository
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.UnitTestBase
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.infrastructure.MetricsCountForTest
import java.time.LocalDate
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

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
      lenient().whenever(readinessProfileRepository.countDocumentsSupportNeededByPrisonIdAndDateTimeBetween(eq(prisonId), any(), any()))
        .thenReturn(emptyList())
    }

    @Test
    fun `return nothing for metric - Reasons for not wanting support`() {
      val actual = profileMetricsService.retrieveMetricsReasonsSupportDeclinedByPrisonIdAndDates(prisonId, dateFrom, dateTo)
      assertThat(actual).isEmpty()
    }

    @Test
    fun `return nothing for metric - Documentation and support needed`() {
      val actual = profileMetricsService.retrieveMetricsDocumentsSupportNeededByPrisonIdAndDates(prisonId, dateFrom, dateTo)
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

    @Test
    fun `return counts for metric - Documentation and support needed`() {
      val metricCounts = listOf(
        makeMetricsCount(ActionTodo.BANK_ACCOUNT, 2, 1),
        makeMetricsCount(ActionTodo.CV_AND_COVERING_LETTER, 22, 6),
        makeMetricsCount(ActionTodo.DISCLOSURE_LETTER, 12, 6),
        makeMetricsCount(ActionTodo.EMAIL, 16, 6),
        makeMetricsCount(ActionTodo.HOUSING, 21, 6),
        makeMetricsCount(ActionTodo.ID, 18, 4),
        makeMetricsCount(ActionTodo.PHONE, 4, 5),

      )
      whenever(readinessProfileRepository.countDocumentsSupportNeededByPrisonIdAndDateTimeBetween(eq(prisonId), any(), any())).thenReturn(metricCounts)
      val expected = metricCounts.map { it.documentsSupportResponse() }.toList()

      val actual = profileMetricsService.retrieveMetricsDocumentsSupportNeededByPrisonIdAndDates(prisonId, dateFrom, dateTo)

      assertContentEquals(expected, actual)
    }

    @Test
    fun `return counts for metric - Work status progress`() {
      val metricCounts = listOf(
        makeMetricsCount(ProfileStatus.NO_RIGHT_TO_WORK, 21, 0),
        makeMetricsCount(ProfileStatus.SUPPORT_DECLINED, 16, 2),
        makeMetricsCount(ProfileStatus.SUPPORT_NEEDED, 22, 6),
        makeMetricsCount(ProfileStatus.READY_TO_WORK, 12, 0),
      )
      whenever(readinessProfileRepository.countWorkStatusByPrisonIdAndDateTimeBetween(eq(prisonId), any(), any())).thenReturn(metricCounts)
      whenever(readinessProfileRepository.countWorkStatusChangeByPrisonIdAndDateTimeBetween(eq(prisonId), any(), any())).thenReturn(28L)
      val expectedStatusCounts = metricCounts.map { it.profileStatus() }.toList()
      val expectedStatusChangeCount = 28L

      val actual = profileMetricsService.retrieveMetricsWorkStatusProgressByPrisonIdAndDates(prisonId, dateFrom, dateTo)

      assertEquals(expectedStatusChangeCount, actual.numberOfPrisonersStatusChange)
      assertContentEquals(expectedStatusCounts, actual.statusCounts)
    }

    @Test
    fun `return counts for metric - Summary`() {
      val metricCounts = MetricsSummaryCount(34, 6, 16, 21)
      whenever(readinessProfileRepository.countSummaryByPrisonIdAndDateTimeBetween(eq(prisonId), any(), any())).thenReturn(metricCounts)
      val expectedCount = GetMetricsSummaryResponse(34, 6, 16, 21)

      val actualCount = profileMetricsService.retrieveMetricsSummaryByPrisonIdAndDates(prisonId, dateFrom, dateTo)

      assertEquals(expectedCount, actualCount)
    }
  }

  private fun makeMetricsCount(reasonForSupportDeclined: SupportToWorkDeclinedReason, countWithin12Weeks: Long, countOver12Weeks: Long) = makeMetricsCount(reasonForSupportDeclined.name, countWithin12Weeks, countOver12Weeks)
  private fun makeMetricsCount(documentSupport: ActionTodo, countWithin12Weeks: Long, countOver12Weeks: Long) = makeMetricsCount(documentSupport.name, countWithin12Weeks, countOver12Weeks)
  private fun makeMetricsCount(profileStatus: ProfileStatus, countWithin12Weeks: Long, countOver12Weeks: Long) = makeMetricsCount(profileStatus.name, countWithin12Weeks, countOver12Weeks)
  private fun makeMetricsCount(field: String, countWithin12Weeks: Long = 0, countOver12Weeks: Long = 0) = MetricsCountForTest(field, countWithin12Weeks, countOver12Weeks)

  private fun MetricsCountForTest.reasonsDeclinedResponse() = GetMetricsReasonsSupportDeclinedResponse(field, countWithin12Weeks, countOver12Weeks)
  private fun MetricsCountForTest.documentsSupportResponse() = GetMetricsDocumentSupportResponse(field, countWithin12Weeks, countOver12Weeks)
  private fun MetricsCountForTest.profileStatus() = MetricsProfileStatusCount(field, countWithin12Weeks, countOver12Weeks)
}
