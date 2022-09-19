package uk.gov.justice.digital.hmpps.educationemploymentapi.model

enum class RecallType {

  STANDARD_RECALL,
  FIXED_TERM_RECALL_14,
  FIXED_TERM_RECALL_28;

  val isFixedTermRecall: Boolean
    get() {
      return listOf(FIXED_TERM_RECALL_28, FIXED_TERM_RECALL_14).contains(this)
    }
}
