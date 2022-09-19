package uk.gov.justice.digital.hmpps.educationemploymentapi.model

import uk.gov.justice.digital.hmpps.educationemploymentapi.enumerations.ReleaseDateType
import java.time.LocalDate

interface SentenceBreakdown : SentenceLengthBreakdown {
  val sentencedAt: LocalDate
  val dates: Map<ReleaseDateType, DateBreakdown>
}
