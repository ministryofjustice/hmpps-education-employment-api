package uk.gov.justice.digital.hmpps.educationemployment.api.audit.infrastructure

import org.hibernate.envers.RevisionListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.RevisionInfo
import kotlin.jvm.optionals.getOrNull

@Component
class AuditRevisionEntityListener @Autowired constructor(var auditorProvider: AuditorAware<String>) : RevisionListener {

  override fun newRevision(revision: Any?) {
    (revision as RevisionInfo).apply {
      createdBy = auditorProvider.currentAuditor.getOrNull()
    }
  }
}
