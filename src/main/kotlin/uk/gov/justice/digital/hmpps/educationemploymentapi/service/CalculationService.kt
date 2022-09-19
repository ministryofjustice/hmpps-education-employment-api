package uk.gov.justice.digital.hmpps.educationemploymentapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemploymentapi.model.Booking
import uk.gov.justice.digital.hmpps.educationemploymentapi.model.CalculationResult
import uk.gov.justice.digital.hmpps.educationemploymentapi.validation.ValidationService

@Service
class CalculationService(
  private val bookingCalculationService: BookingCalculationService,
  private val bookingExtractionService: BookingExtractionService,
  private val bookingTimelineService: BookingTimelineService,
  private val validationService: ValidationService
) {

  fun calculateReleaseDates(booking: Booking): Pair<Booking, CalculationResult> {
    val workingBooking = calculate(booking)
    // apply any rules to calculate the dates
    return workingBooking to bookingExtractionService.extract(workingBooking)
  }

  private fun calculate(booking: Booking): Booking {
    var workingBooking: Booking = booking

    // identify the types of the sentences
    workingBooking =
      bookingCalculationService
        .identify(workingBooking)

    // calculate the dates within the sentences (Generate initial sentence calculations)
    workingBooking =
      bookingCalculationService
        .calculate(workingBooking)

    workingBooking =
      bookingCalculationService
        .createConsecutiveSentences(workingBooking)

    workingBooking =
      bookingCalculationService
        .createSingleTermSentences(workingBooking)

    workingBooking = bookingTimelineService
      .walkTimelineOfBooking(workingBooking)

    validationService.validateAfterCalculation(workingBooking)

    return workingBooking
  }
}
