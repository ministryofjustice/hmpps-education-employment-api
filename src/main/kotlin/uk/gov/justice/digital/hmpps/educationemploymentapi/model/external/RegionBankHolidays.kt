package uk.gov.justice.digital.hmpps.educationemploymentapi.model.external

data class RegionBankHolidays(
  val division: String,
  val events: List<BankHoliday>
)
