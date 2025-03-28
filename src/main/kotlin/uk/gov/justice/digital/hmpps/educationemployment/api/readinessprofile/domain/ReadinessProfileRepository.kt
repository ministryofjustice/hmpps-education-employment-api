package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.stereotype.Repository

@Repository
interface ReadinessProfileRepository :
  JpaRepository<ReadinessProfile, String>,
  RevisionRepository<ReadinessProfile, String, Long>
