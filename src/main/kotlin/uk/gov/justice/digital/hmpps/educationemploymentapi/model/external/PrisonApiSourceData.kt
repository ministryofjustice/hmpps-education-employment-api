package uk.gov.justice.digital.hmpps.educationemploymentapi.model.external

data class PrisonApiSourceData(
  val sentenceAndOffences: List<SentenceAndOffences>,
  val prisonerDetails: PrisonerDetails,
  val bookingAndSentenceAdjustments: BookingAndSentenceAdjustments,
  val returnToCustodyDate: ReturnToCustodyDate?
)
