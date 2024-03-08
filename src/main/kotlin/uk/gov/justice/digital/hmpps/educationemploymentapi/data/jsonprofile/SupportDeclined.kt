package uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile

import java.time.LocalDateTime

data class SupportDeclined(
  var modifiedBy: String?,
  var modifiedDateTime: LocalDateTime?,

  val supportToWorkDeclinedReason: List<SupportToWorkDeclinedReason>,
  val supportToWorkDeclinedReasonOther: String,
  val circumstanceChangesRequiredToWork: List<CircumstanceChangesRequiredToWork>,
  val circumstanceChangesRequiredToWorkOther: String,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as SupportDeclined

    if (supportToWorkDeclinedReason != other.supportToWorkDeclinedReason) return false
    if (supportToWorkDeclinedReasonOther != other.supportToWorkDeclinedReasonOther) return false
    if (circumstanceChangesRequiredToWork != other.circumstanceChangesRequiredToWork) return false
    if (circumstanceChangesRequiredToWorkOther != other.circumstanceChangesRequiredToWorkOther) return false

    return true
  }

  override fun hashCode(): Int {
    var result = supportToWorkDeclinedReason.hashCode()
    result = 31 * result + supportToWorkDeclinedReasonOther.hashCode()
    result = 31 * result + circumstanceChangesRequiredToWork.hashCode()
    result = 31 * result + circumstanceChangesRequiredToWorkOther.hashCode()
    return result
  }
}
