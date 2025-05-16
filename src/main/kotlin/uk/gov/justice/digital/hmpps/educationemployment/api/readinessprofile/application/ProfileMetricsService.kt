package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfileRepository
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.domain.TimeProvider
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.infrastructure.MetricsCountByStringField
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Service
class ProfileMetricsService(
  private val readinessProfileRepository: ReadinessProfileRepository,
  private val timeProvider: TimeProvider,
) {
  private val atEndOfDay = LocalTime.MAX.truncatedTo(ChronoUnit.MICROS)

  fun retrieveMetricsReasonsSupportDeclinedByPrisonIdAndDates(
    prisonId: String,
    dateFrom: LocalDate,
    dateTo: LocalDate,
  ): List<GetMetricsReasonsSupportDeclinedResponse> = readinessProfileRepository.countReasonsForSupportDeclinedByPrisonIdAndDateTimeBetween(
    prisonId,
    startTime = dateFrom.startAt,
    endTime = dateTo.endAt,
  ).reasonsSupportDeclinedResponses()

  fun retrieveMetricsDocumentsSupportNeededByPrisonIdAndDates(
    prisonId: String,
    dateFrom: LocalDate,
    dateTo: LocalDate,
  ): List<GetMetricsDocumentSupportResponse> = readinessProfileRepository.countDocumentsSupportNeededByPrisonIdAndDateTimeBetween(
    prisonId,
    startTime = dateFrom.startAt,
    endTime = dateTo.endAt,
  ).documentsSupportResponses()

  fun retrieveMetricsWorkStatusProgressByPrisonIdAndDates(
    prisonId: String,
    dateFrom: LocalDate,
    dateTo: LocalDate,
  ): GetMetricsWorkStatusResponse {
    val startTime = dateFrom.startAt
    val endTime = dateTo.endAt
    val statusChangeCount = readinessProfileRepository.countWorkStatusChangeByPrisonIdAndDateTimeBetween(prisonId, startTime, endTime)
    val statusCounts = readinessProfileRepository.countWorkStatusByPrisonIdAndDateTimeBetween(prisonId, startTime, endTime)
    return GetMetricsWorkStatusResponse(statusChangeCount, statusCounts.statusCounts())
  }

  private val LocalDate.startAt: Instant get() = this.atStartOfDay().instant
  private val LocalDate.endAt: Instant get() = this.atTime(atEndOfDay).instant
  private val LocalDateTime.instant: Instant get() = this.atZone(timeProvider.timezoneId).toInstant()

  private fun List<MetricsCountByStringField>.reasonsSupportDeclinedResponses() = this.map { GetMetricsReasonsSupportDeclinedResponse(it.field, it.countWithin12Weeks, it.countOver12Weeks) }
  private fun List<MetricsCountByStringField>.documentsSupportResponses() = this.map { GetMetricsDocumentSupportResponse(it.field, it.countWithin12Weeks, it.countOver12Weeks) }
  private fun List<MetricsCountByStringField>.statusCounts() = this.map { MetricsProfileStatusCount(it.field, it.countWithin12Weeks, it.countOver12Weeks) }
}

data class GetMetricsReasonsSupportDeclinedResponse(
  val supportToWorkDeclinedReason: String,
  val numberOfPrisonersWithin12Weeks: Long,
  val numberOfPrisonersOver12Weeks: Long,
)

data class GetMetricsDocumentSupportResponse(
  val actionTodo: String,
  val numberOfPrisonersWithin12Weeks: Long,
  val numberOfPrisonersOver12Weeks: Long,
)

data class MetricsProfileStatusCount(
  val profileStatus: String,
  val numberOfPrisonersWithin12Weeks: Long,
  val numberOfPrisonersOver12Weeks: Long,
)

data class GetMetricsWorkStatusResponse(
  val numberOfPrisonersStatusChange: Long,
  val statusCounts: List<MetricsProfileStatusCount>,
)
