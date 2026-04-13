package uk.gov.justice.digital.hmpps.educationemployment.api.shared.domain

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

interface TimeProvider {
  val timeZoneId: ZoneId

  fun now(): LocalDateTime

  fun nowAsInstant(): Instant = now().atZone(timeZoneId).toInstant()

  fun today() = now().toLocalDate()
}
