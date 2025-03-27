package uk.gov.justice.digital.hmpps.educationemployment.api.shared.domain

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

interface TimeProvider {
  val timezoneId: ZoneId

  fun now(): LocalDateTime

  fun nowAsInstant(): Instant = now().atZone(timezoneId).toInstant()

  fun today() = now().toLocalDate()
}
