package uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.infrastructure

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AuditCleaner(val entityManager: EntityManager) {
  @Transactional
  fun deleteAllRevisions() {
    entityManager.createNativeQuery("delete from work_readiness_audit").executeUpdate()
    entityManager.createNativeQuery("delete from revision_info").executeUpdate()
    entityManager.flush()
  }
}
