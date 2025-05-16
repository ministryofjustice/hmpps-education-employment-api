package uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.infrastructure

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicLong

interface TestClock {
  val clock: Clock
  val instant: Instant get() = clock.instant()

  val timezoneId: ZoneId
  val localDateTime: LocalDateTime get() = instant.atZone(timezoneId).toLocalDateTime()

  companion object {
    internal val defaultCurrentTime: Instant = Instant.parse("2025-01-01T00:00:00Z")

    fun defaultClock() = BasicTestClock.defaultClock()
    fun timeslotClock() = TimeslotClock.defaultClock()

    fun fixedClock(fixedInstant: Instant) = BasicTestClock.fixedClock(fixedInstant)
    fun timeslotClock(startTime: Instant) = TimeslotClock.timeslotToClock(startTime)
    fun timeslotClock(startTime: Instant, timeslotLength: Duration) = TimeslotClock(startTime, timeslotLength)
    fun timeslotClock(startTime: Instant, timeslotLength: Duration, timezoneId: ZoneId) = TimeslotClock(startTime, timeslotLength, timezoneId)
  }

  open class BasicTestClock(
    private val testClock: Clock,
    override val timezoneId: ZoneId,
  ) : TestClock {
    override val clock: Clock get() = testClock

    internal companion object {
      val defaultCurrentTime = TestClock.defaultCurrentTime

      fun fixedClock(fixedInstant: Instant) = FixedTestClock(fixedInstant)
      fun defaultClock() = FixedTestClock(defaultCurrentTime)
    }
  }

  class FixedTestClock(fixedInstant: Instant) : BasicTestClock(Clock.fixed(fixedInstant, ZoneOffset.UTC), ZoneOffset.UTC)

  class TimeslotClock(
    private val startTime: Instant,
    private val timeslotLength: Duration,
    override val timezoneId: ZoneId = ZoneOffset.UTC,
  ) : TestClock {
    private val baseClock: Clock get() = Clock.fixed(startTime, timezoneId)

    val timeslot: AtomicLong = AtomicLong(0L)

    override val clock: Clock get() = timeslotToClock(timeslot.toLong())

    internal companion object {
      val defaultDuration = Duration.ofDays(1)

      fun defaultClock() = incrementDailyCLock()
      fun incrementDailyCLock() = TimeslotClock(defaultCurrentTime, defaultDuration)
      fun timeslotToClock(startTime: Instant, timeslotLength: Duration = defaultDuration, timezoneId: ZoneId = ZoneOffset.UTC) = TimeslotClock(startTime, timeslotLength, timezoneId)
    }

    fun timeslotToInstant(timeslot: Long) = startTime + timeslotLength.multipliedBy(timeslot)
    fun timeslotToLocalDateTime(timeslot: Long) = timeslotToInstant(timeslot).atZone(timezoneId).toLocalDateTime()

    private fun timeslotToClock(timeslot: Long): Clock = Clock.offset(baseClock, timeslotLength.multipliedBy(timeslot))
  }
}
