package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application

data class MetricsSummaryCount(
  val countWithin12Weeks: Long,
  val countOver12Weeks: Long,
  val countSupportDeclined: Long,
  val countNoRightToWork: Long,
)
