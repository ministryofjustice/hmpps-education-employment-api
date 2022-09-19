package uk.gov.justice.digital.hmpps.educationemploymentapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.threeten.extra.LocalDateRange
import uk.gov.justice.digital.hmpps.educationemploymentapi.enumerations.AdjustmentType.ADDITIONAL_DAYS_SERVED
import uk.gov.justice.digital.hmpps.educationemploymentapi.enumerations.AdjustmentType.LICENSE_UNUSED_ADA
import uk.gov.justice.digital.hmpps.educationemploymentapi.enumerations.AdjustmentType.RELEASE_UNUSED_ADA
import uk.gov.justice.digital.hmpps.educationemploymentapi.enumerations.CalculationRule
import uk.gov.justice.digital.hmpps.educationemploymentapi.enumerations.ReleaseDateType
import uk.gov.justice.digital.hmpps.educationemploymentapi.model.Adjustment
import uk.gov.justice.digital.hmpps.educationemploymentapi.model.Booking
import uk.gov.justice.digital.hmpps.educationemploymentapi.model.CalculableSentence
import uk.gov.justice.digital.hmpps.educationemploymentapi.model.SentenceCalculation
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS
import java.util.Collections
import kotlin.math.min

@Service
class BookingTimelineService(
  val sentenceAdjustedCalculationService: SentenceAdjustedCalculationService,
  val extractionService: SentencesExtractionService
) {

  fun walkTimelineOfBooking(booking: Booking): Booking {
    val workingBooking = createSentenceGroupsAndShareAdjustments(booking)
    val expiryDate = extractionService.mostRecent(workingBooking.getAllExtractableSentences(), SentenceCalculation::expiryDate)
    capDatesByExpiry(expiryDate, workingBooking.sentenceGroups, workingBooking)
    return booking
  }

  private fun createSentenceGroupsAndShareAdjustments(booking: Booking): Booking {
    log.info("Building timeline of sentences")
    val sortedSentences = booking.getAllExtractableSentences().sortedBy { it.sentencedAt }
    val timelineTracker = TimelineTracker(
      firstSentence = sortedSentences[0],
      timelineRange = sortedSentences[0].getRangeOfSentenceBeforeAwardedDays(),
      previousSentence = sortedSentences[0]
    )

    sortedSentences.forEach {
      it.sentenceCalculation.adjustmentsBefore = Collections.max(listOf(timelineTracker.timelineRange.end, it.sentencedAt))
      it.sentenceCalculation.adjustmentsAfter = timelineTracker.previousReleaseDateReached
      val itRange = it.getRangeOfSentenceBeforeAwardedDays()
      if (isThisSentenceTheFirstOfSentencesParallelToRecall(timelineTracker, it)) {
        endCurrentSentenceGroup(timelineTracker, booking)
        startNewSentenceGroup(timelineTracker, it, itRange)
      } else if (timelineTracker.timelineRange.isConnected(itRange)) {
        // This sentence overlaps with the previous. Its therefore in the same sentence group.
        if (itRange.end.isAfter(timelineTracker.timelineRange.end)) {
          timelineTracker.timelineRange = LocalDateRange.of(timelineTracker.timelineRange.start, itRange.end)
        }
        timelineTracker.currentSentenceGroup.add(it)
      } else {
        // There sentence doesn't overlap with the previous sentence group.
        // 1. Check if there have been any ADAs served
        var daysBetween = DAYS.between(timelineTracker.timelineRange.end, it.sentencedAt)
        val adaAvailable = it.sentenceCalculation.calculatedTotalAwardedDays
        val daysAdaServed = min(daysBetween - 1, adaAvailable.toLong())
        booking.adjustments.addAdjustment(
          ADDITIONAL_DAYS_SERVED,
          Adjustment(
            numberOfDays = daysAdaServed.toInt(),
            appliesToSentencesFrom = it.sentencedAt
          )
        )

        daysBetween = DAYS.between(timelineTracker.timelineRange.end.plusDays(daysAdaServed), it.sentencedAt)
        if (daysBetween <= 1) {
          // With ADAs served the sentence now overlaps with the sentence group and therefore belongs to the group.
          timelineTracker.timelineRange = LocalDateRange.of(timelineTracker.timelineRange.start, itRange.end)
          timelineTracker.currentSentenceGroup.add(it)
        } else {
          // Even with all ADAs applied the sentence does not overlap with the sentence group, it therefore belongs to a new group.
          endCurrentSentenceGroup(timelineTracker, booking)
          startNewSentenceGroup(timelineTracker, it, itRange)
        }
      }
      timelineTracker.previousSentence = it
    }
    endCurrentSentenceGroup(timelineTracker, booking)
    booking.sentenceGroups = timelineTracker.sentenceGroups
    return booking
  }

  /*
    When we have the first non-recall sentence parallel to recall sentences, we need to start a new sentence group so that adjustments are not shared.
   */
  private fun isThisSentenceTheFirstOfSentencesParallelToRecall(timelineTracker: TimelineTracker, it: CalculableSentence): Boolean {
    return timelineTracker.previousSentence.isRecall() && !it.isRecall()
  }

  private fun startNewSentenceGroup(timelineTracker: TimelineTracker, it: CalculableSentence, itRange: LocalDateRange) {
    timelineTracker.currentSentenceGroup = mutableListOf()
    timelineTracker.currentSentenceGroup.add(it)
    timelineTracker.previousReleaseDateReached = it.sentencedAt.minusDays(1)
    it.sentenceCalculation.adjustmentsBefore = it.sentencedAt
    it.sentenceCalculation.adjustmentsAfter = timelineTracker.previousReleaseDateReached
    if (itRange.end.isAfter(timelineTracker.timelineRange.end)) {
      timelineTracker.timelineRange = LocalDateRange.of(timelineTracker.timelineRange.start, itRange.end)
    }
  }

  private fun endCurrentSentenceGroup(timelineTracker: TimelineTracker, booking: Booking) {
    shareAdjustmentsThroughSentenceGroup(timelineTracker, booking)
    // Clear the sentence group and start again.
    timelineTracker.sentenceGroups.add(timelineTracker.currentSentenceGroup)
  }

  private fun capDatesByExpiry(
    expiry: LocalDate,
    sentenceGroups: List<List<CalculableSentence>>,
    booking: Booking
  ) {
    val adjustments = sentenceGroups[0][0].sentenceCalculation.adjustments
    sentenceGroups.forEach { group ->
      val unusedDays = group.filter { it.sentenceCalculation.releaseDate.isAfter(expiry) }.maxOfOrNull { DAYS.between(expiry, it.sentenceCalculation.releaseDate) }
      if (unusedDays != null && unusedDays > 0) {
        adjustments.addAdjustment(RELEASE_UNUSED_ADA, Adjustment(group.minOf { it.sentencedAt }, unusedDays.toInt()))
        group.forEach {
          readjustDates(it, booking)
        }
      }
      val unusedLicenseDays = group.filter {
        val ledBreakdown = it.sentenceCalculation.breakdownByReleaseDateType[ReleaseDateType.LED]
        ledBreakdown != null && ledBreakdown.rules.contains(CalculationRule.LED_CONSEC_ORA_AND_NON_ORA) && it.sentenceCalculation.licenceExpiryDate!!.isAfter(expiry)
      }.maxOfOrNull {
        DAYS.between(expiry, it.sentenceCalculation.licenceExpiryDate!!)
      }

      if (unusedLicenseDays != null && unusedLicenseDays > 0) {
        adjustments.addAdjustment(LICENSE_UNUSED_ADA, Adjustment(group.minOf { it.sentencedAt }, unusedLicenseDays.toInt()))
        group.forEach {
          readjustDates(it, booking)
        }
      }
    }
  }

  private fun shareAdjustmentsThroughSentenceGroup(timelineTracker: TimelineTracker, booking: Booking) {
    timelineTracker.currentSentenceGroup.forEach {
      it.sentenceCalculation.adjustmentsBefore = timelineTracker.timelineRange.end
      readjustDates(it, booking)
    }
  }

  private fun readjustDates(it: CalculableSentence, booking: Booking) {
    sentenceAdjustedCalculationService.calculateDatesFromAdjustments(it, booking)
    log.info(it.buildString())
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
