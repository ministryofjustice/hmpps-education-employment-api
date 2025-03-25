package uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.envers.RevisionEntity
import org.hibernate.envers.RevisionNumber
import org.hibernate.envers.RevisionTimestamp
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.infrastructure.AuditRevisionEntityListener

@Entity
@Table(name = "revision_info")
@RevisionEntity(AuditRevisionEntityListener::class)
data class RevisionInfo(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @RevisionNumber
  @Column(name = "rev_number")
  val revNumber: Int,

  @RevisionTimestamp
  @Column(name = "rev_time")
  val revTime: Long,

  @Column(name = "created_by")
  var createdBy: String? = null,
)
