package uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile

data class CurrentSupportState(
  var supportDeclined: SupportDeclined?,
  var supportAccepted: SupportAccepted?,
)
