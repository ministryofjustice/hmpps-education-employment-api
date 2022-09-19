package uk.gov.justice.digital.hmpps.educationemploymentapi.validation

enum class ValidationCode {
  UNSUPPORTED_SENTENCE_TYPE,
  OFFENCE_DATE_AFTER_SENTENCE_START_DATE,
  OFFENCE_DATE_AFTER_SENTENCE_RANGE_DATE,
  OFFENCE_MISSING_DATE,
  REMAND_FROM_TO_DATES_REQUIRED,
  SENTENCE_HAS_MULTIPLE_TERMS,
  REMAND_OVERLAPS_WITH_REMAND,
  REMAND_OVERLAPS_WITH_SENTENCE,
  CUSTODIAL_PERIOD_EXTINGUISHED,
  ADJUSTMENT_AFTER_RELEASE,
  MULTIPLE_SENTENCES_CONSECUTIVE_TO,
  PRISONER_SUBJECT_TO_PTD,
  SEC_91_SENTENCE_TYPE_INCORRECT,
  ADJUSTMENT_FUTURE_DATED,
  SENTENCE_HAS_NO_IMPRISONMENT_TERM,
  SENTENCE_HAS_NO_LICENCE_TERM,
  ZERO_IMPRISONMENT_TERM,
  EDS_LICENCE_TERM_LESS_THAN_ONE_YEAR,
  EDS_LICENCE_TERM_MORE_THAN_EIGHT_YEARS,
  EDS18_EDS21_EDSU18_SENTENCE_TYPE_INCORRECT,
  LASPO_AR_SENTENCE_TYPE_INCORRECT,
  MORE_THAN_ONE_IMPRISONMENT_TERM,
  MORE_THAN_ONE_LICENCE_TERM,
  SOPC_LICENCE_TERM_NOT_12_MONTHS,
  SEC236A_SENTENCE_TYPE_INCORRECT,
  SOPC18_SOPC21_SENTENCE_TYPE_INCORRECT
}
