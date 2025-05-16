package uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.infrastructure

import uk.gov.justice.digital.hmpps.educationemployment.api.shared.infrastructure.MetricsCountByStringField

data class MetricsCountForTest(
  override val field: String,
  override val countWithin12Weeks: Long,
  override val countOver12Weeks: Long,
) : MetricsCountByStringField
