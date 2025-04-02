package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain

data class CurrentSupportState(
  var supportDeclined: SupportDeclined?,
  var supportAccepted: SupportAccepted?,
)
