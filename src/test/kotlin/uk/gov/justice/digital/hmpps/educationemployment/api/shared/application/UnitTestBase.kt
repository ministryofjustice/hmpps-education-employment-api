package uk.gov.justice.digital.hmpps.educationemployment.api.shared.application

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.domain.TimeProvider
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
abstract class UnitTestBase {
  @Mock
  protected lateinit var timeProvider: TimeProvider

  protected val defaultTimeZoneOffset = ZoneOffset.UTC
  protected val defaultTimeZone: ZoneId = defaultTimeZoneOffset
  protected val defaultCurrentLocalTime = LocalDateTime.of(2025, 1, 1, 1, 1, 1)
  protected val defaultCurrentTime: Instant by lazy { defaultCurrentLocalTime.atZone(defaultTimeZone).toInstant() }

  @BeforeEach
  internal open fun setUpBase() {
    lenient().whenever(timeProvider.nowAsInstant()).thenReturn(defaultCurrentTime)
    lenient().whenever(timeProvider.now()).thenReturn(defaultCurrentLocalTime)
  }
}
