package uk.gov.justice.digital.hmpps.educationemploymentapi.model

import uk.gov.justice.digital.hmpps.educationemploymentapi.enumerations.ReleaseDateType
import java.time.LocalDate

data class ConcurrentSentenceBreakdown(
  override val sentencedAt: LocalDate,
  override val sentenceLength: String,
  override val sentenceLengthDays: Int,
  override val dates: Map<ReleaseDateType, DateBreakdown>,
  val lineSequence: Int,
  val caseSequence: Int,
  val caseReference: String? = null,
) : SentenceBreakdown
