package uk.gov.justice.digital.hmpps.educationemploymentapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemploymentapi.model.external.BookingAndSentenceAdjustments
import uk.gov.justice.digital.hmpps.educationemploymentapi.model.external.PrisonApiSourceData
import uk.gov.justice.digital.hmpps.educationemploymentapi.model.external.PrisonerDetails
import uk.gov.justice.digital.hmpps.educationemploymentapi.model.external.ReturnToCustodyDate
import uk.gov.justice.digital.hmpps.educationemploymentapi.model.external.SentenceAndOffences
import uk.gov.justice.digital.hmpps.educationemploymentapi.model.external.SentenceCalculationType
import uk.gov.justice.digital.hmpps.educationemploymentapi.model.external.UpdateOffenderDates

@Service
class PrisonService(
  private val prisonApiClient: PrisonApiClient,
) {

  fun getPrisonApiSourceDataIncludingInactive(prisonerId: String): PrisonApiSourceData {
    val prisonerDetails = getOffenderDetail(prisonerId)
    return if (prisonerDetails.agencyId == "OUT") {
      getInactivePrisonApiSourceData(prisonerDetails)
    } else {
      getActivePrisonApiSourceData(prisonerDetails)
    }
  }
  fun getPrisonApiSourceData(prisonerId: String): PrisonApiSourceData {
    val prisonerDetails = getOffenderDetail(prisonerId)
    return getActivePrisonApiSourceData(prisonerDetails)
  }

  private fun getActivePrisonApiSourceData(prisonerDetails: PrisonerDetails): PrisonApiSourceData {
    val sentenceAndOffences = getSentencesAndOffences(prisonerDetails.bookingId)
    val bookingAndSentenceAdjustments = getBookingAndSentenceAdjustments(prisonerDetails.bookingId)
    val bookingHasFixedTermRecall = sentenceAndOffences.any { SentenceCalculationType.from(it.sentenceCalculationType)?.recallType?.isFixedTermRecall == true }
    var returnToCustodyDate: ReturnToCustodyDate? = null
    if (bookingHasFixedTermRecall) {
      returnToCustodyDate = prisonApiClient.getReturnToCustodyDate(prisonerDetails.bookingId)
    }
    return PrisonApiSourceData(sentenceAndOffences, prisonerDetails, bookingAndSentenceAdjustments, returnToCustodyDate)
  }

  private fun getInactivePrisonApiSourceData(prisonerDetails: PrisonerDetails): PrisonApiSourceData {
    val sentenceAndOffences = getSentencesAndOffences(prisonerDetails.bookingId, false)
    val bookingAndSentenceAdjustments = getBookingAndSentenceAdjustments(prisonerDetails.bookingId, false)
    val bookingHasFixedTermRecall = sentenceAndOffences.any { SentenceCalculationType.from(it.sentenceCalculationType)?.recallType?.isFixedTermRecall == true }
    var returnToCustodyDate: ReturnToCustodyDate? = null
    if (bookingHasFixedTermRecall) {
      returnToCustodyDate = prisonApiClient.getReturnToCustodyDate(prisonerDetails.bookingId)
    }
    return PrisonApiSourceData(sentenceAndOffences, prisonerDetails, bookingAndSentenceAdjustments, returnToCustodyDate)
  }

  fun getOffenderDetail(prisonerId: String): PrisonerDetails {
    return prisonApiClient.getOffenderDetail(prisonerId)
  }

  fun getBookingAndSentenceAdjustments(bookingId: Long, filterActive: Boolean = true): BookingAndSentenceAdjustments {
    val adjustments = prisonApiClient.getSentenceAndBookingAdjustments(bookingId)
    return BookingAndSentenceAdjustments(
      sentenceAdjustments = adjustments.sentenceAdjustments.filter { !filterActive || it.active },
      bookingAdjustments = adjustments.bookingAdjustments.filter { !filterActive || it.active }
    )
  }

  fun getSentencesAndOffences(bookingId: Long, filterActive: Boolean = true): List<SentenceAndOffences> {
    return prisonApiClient.getSentencesAndOffences(bookingId)
      .filter { !filterActive || it.sentenceStatus == "A" }
  }

  fun postReleaseDates(bookingId: Long, updateOffenderDates: UpdateOffenderDates) {
    return prisonApiClient.postReleaseDates(bookingId, updateOffenderDates)
  }
}
