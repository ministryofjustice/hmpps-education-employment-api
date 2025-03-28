package uk.gov.justice.digital.hmpps.educationemployment.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationemployment.api.entity.ReadinessProfile

@Repository
interface ReadinessProfileRepository :
  JpaRepository<ReadinessProfile, String>,
  RevisionRepository<ReadinessProfile, String, Long>
