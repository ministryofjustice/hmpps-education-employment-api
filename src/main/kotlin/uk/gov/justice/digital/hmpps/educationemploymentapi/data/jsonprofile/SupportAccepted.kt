package uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile

import java.time.LocalDateTime

data class SupportAccepted(
  val actionsRequired: ActionsRequired,
  val workImpacts: WorkImpacts,
  val workInterests: WorkInterests,
  val workExperience: WorkExperience
)
