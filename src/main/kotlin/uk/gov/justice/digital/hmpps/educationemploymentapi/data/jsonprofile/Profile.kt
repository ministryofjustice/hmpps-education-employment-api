package uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile

import java.time.LocalDateTime

data class Profile(
  val status:ProfileStatus,
  val supportDeclined: SupportDeclined?,
  val supportAccepted: SupportAccepted?
)
