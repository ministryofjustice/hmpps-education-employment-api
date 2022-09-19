package uk.gov.justice.digital.hmpps.educationemploymentapi.model.external

import uk.gov.justice.digital.hmpps.educationemploymentapi.util.isBeforeOrEqualTo
import java.time.LocalDate

data class PrisonerDetails(
  val bookingId: Long,
  val offenderNo: String,
  val firstName: String = "",
  val lastName: String = "",
  val dateOfBirth: LocalDate,
  val alerts: List<Alert> = emptyList(),
  val agencyId: String = ""
) {

  fun activeAlerts(): List<Alert> {
    return alerts.filter {
      it.dateCreated.isBeforeOrEqualTo(LocalDate.now()) &&
        (it.dateExpires == null || it.dateExpires.isAfter(LocalDate.now()))
    }
  }
}
