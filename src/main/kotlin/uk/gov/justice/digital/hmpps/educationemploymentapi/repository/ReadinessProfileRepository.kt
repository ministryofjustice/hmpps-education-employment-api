package uk.gov.justice.digital.hmpps.educationemploymentapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationemploymentapi.entity.ReadinessProfile

@Repository
interface ReadinessProfileRepository : JpaRepository<ReadinessProfile, String>
