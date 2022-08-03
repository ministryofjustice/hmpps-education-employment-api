package uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.model.ReadinessProfile

@Repository
interface ReadinessProfileRespository : ReadinessProfileCustomRepository, CoroutineCrudRepository<ReadinessProfile, String>
