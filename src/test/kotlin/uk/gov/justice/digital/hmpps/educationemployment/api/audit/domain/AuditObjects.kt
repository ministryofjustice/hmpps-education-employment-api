package uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain

import java.time.Instant
import java.time.ZoneId

object AuditObjects {
  val username = "auser_gen"
  val displayName = "Some One"
  val system = "system"

  val testClient = "test-client"
  val testPrincipal = "test"

  val defaultAuditor = username

  val defaultAuditTime = Instant.parse("2025-01-01T00:00:00.00Z")
  val defaultTimezoneId = ZoneId.of("Z")
  val defaultAuditLocalTime = defaultAuditTime.atZone(defaultTimezoneId).toLocalDateTime()
}
