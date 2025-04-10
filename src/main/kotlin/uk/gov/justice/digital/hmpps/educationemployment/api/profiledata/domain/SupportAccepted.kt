package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain

import java.time.LocalDateTime

data class SupportAccepted(
  var modifiedBy: String?,
  var modifiedDateTime: LocalDateTime?,
  val actionsRequired: ActionsRequired,
  val workImpacts: WorkImpacts,
  val workInterests: WorkInterests,
  val workExperience: WorkExperience,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as SupportAccepted

    if (actionsRequired != other.actionsRequired) return false
    if (workImpacts != other.workImpacts) return false
    if (workInterests != other.workInterests) return false
    if (workExperience != other.workExperience) return false

    return true
  }

  override fun hashCode(): Int {
    var result = actionsRequired.hashCode()
    result = 31 * result + workImpacts.hashCode()
    result = 31 * result + workInterests.hashCode()
    result = 31 * result + workExperience.hashCode()
    return result
  }
}
