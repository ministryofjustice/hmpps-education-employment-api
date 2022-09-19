package uk.gov.justice.digital.hmpps.educationemploymentapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationemploymentapi.entity.CalculationRequest
import java.util.Optional

@Repository
interface CalculationRequestRepository : JpaRepository<CalculationRequest, Long> {
  fun findFirstByPrisonerIdAndBookingIdAndCalculationStatusOrderByCalculatedAtDesc(
    prisonerId: String,
    bookingId: Long,
    calculationStatus: String
  ): Optional<CalculationRequest>

  fun findByIdAndCalculationStatus(calculationRequestId: Long, calculationStatus: String): Optional<CalculationRequest>
}
