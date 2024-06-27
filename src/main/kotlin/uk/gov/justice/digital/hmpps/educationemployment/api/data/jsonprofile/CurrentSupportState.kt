package uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile

data class CurrentSupportState(
  var supportDeclined: SupportDeclined?,
  var supportAccepted: SupportAccepted?,
)
