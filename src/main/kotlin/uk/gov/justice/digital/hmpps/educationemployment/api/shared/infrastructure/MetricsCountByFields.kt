package uk.gov.justice.digital.hmpps.educationemployment.api.shared.infrastructure

interface MetricsCountByField<T> {
  val field: T
  val countWithin12Weeks: Long
  val countOver12Weeks: Long
}

interface MetricsCountByStringField : MetricsCountByField<String>
