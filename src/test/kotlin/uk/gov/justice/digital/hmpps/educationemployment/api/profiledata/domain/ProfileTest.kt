package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.v2.Profile
import java.time.Instant

class ProfileTest {

  private val objectMapper = CapturedSpringConfigValues.objectMapper

  @Test
  fun `should serialize status and statusChangeType as arrays`() {
    val profile = Profile(
      status = ProfileStatus.NO_RIGHT_TO_WORK,
      statusChange = true,
      statusChangeDate = Instant.parse("2024-06-14T10:00:00Z"),
      statusChangeType = StatusChange.NEW,
      prisonId = "LEI",
      prisonName = "Leeds",
      within12Weeks = true,
      supportDeclined = null,
      supportAccepted = null,
    )

    val json = objectMapper.writeValueAsString(profile)

    assertTrue(json.contains("\"status\":\"NO_RIGHT_TO_WORK\""))
    assertTrue(json.contains("\"statusChangeType\":\"NEW\""))
    assertTrue(json.contains("\"statusChange\":true"))
    assertFalse(json.contains("statusAsList"))
    assertFalse(json.contains("statusChangeAsList"))
  }
}
