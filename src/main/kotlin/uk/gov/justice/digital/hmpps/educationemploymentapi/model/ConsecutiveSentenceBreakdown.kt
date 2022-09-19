package uk.gov.justice.digital.hmpps.educationemploymentapi.model

import uk.gov.justice.digital.hmpps.educationemploymentapi.enumerations.ReleaseDateType
import java.time.LocalDate

data class ConsecutiveSentenceBreakdown(
  override val sentencedAt: LocalDate,
  override val sentenceLength: String,
  override val sentenceLengthDays: Int,
  override val dates: Map<ReleaseDateType, DateBreakdown>,
  val sentenceParts: List<ConsecutiveSentencePart>
) : SentenceBreakdown
