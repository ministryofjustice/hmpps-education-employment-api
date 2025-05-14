package uk.gov.justice.digital.hmpps.educationemployment.api.integration.readinessprofile.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.springframework.data.history.Revision
import org.springframework.data.history.RevisionMetadata.RevisionType
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.RevisionInfo
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.infrastructure.RepositoryTestCase
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.infrastructure.TestClock
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportDeclined
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.circumstanceChangesRequiredToWorkList
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import java.time.Instant
import java.time.LocalDateTime

abstract class ReadinessProfileRepositoryTestCase : RepositoryTestCase() {
  protected val testClock: TestClock = TestClock.defaultClock()
  override val currentTime: Instant get() = testClock.instant
  protected val currentLocalDateTime: LocalDateTime get() = testClock.localDateTime

  protected val ReadinessProfile.asExpected
    get() = this.copy(
      new = false,
      createdDateTime = currentTimeLocal,
      createdBy = auditor,
      modifiedDateTime = currentTimeLocal,
      modifiedBy = auditor,
    )

  protected fun assertRevisionMetadata(
    expectedRevisionType: RevisionType,
    expectedCreator: String?,
    vararg revisions: Revision<Long, ReadinessProfile>,
  ) {
    revisions.forEach { revision ->
      with(revision.metadata) {
        assertThat(revisionType).isEqualTo(expectedRevisionType)
        expectedCreator?.let { assertThat(getDelegate<RevisionInfo>().createdBy).isEqualTo(expectedCreator) }
      }
    }
  }

  protected fun makeProfileWithSupportDeclined(
    prisonNumber: String,
    bookingId: Long,
    prisonId: String,
    prisonName: String? = null,
    within12Weeks: Boolean = true,
    vararg reasons: SupportToWorkDeclinedReason,
  ): ReadinessProfile {
    val supportDeclined = SupportDeclined(
      modifiedBy = auditor,
      modifiedDateTime = currentLocalDateTime,
      supportToWorkDeclinedReason = reasons.toList(),
      supportToWorkDeclinedReasonOther = "",
      circumstanceChangesRequiredToWork = circumstanceChangesRequiredToWorkList,
      circumstanceChangesRequiredToWorkOther = "",
    )
    val profile = Profile(
      status = ProfileStatus.SUPPORT_DECLINED,
      prisonId = prisonId,
      prisonName = prisonName,
      within12Weeks = within12Weeks,
      statusChange = false,
      statusChangeDate = null,
      statusChangeType = StatusChange.NEW,
      supportDeclined = supportDeclined,
      supportAccepted = null,
    )

    return ReadinessProfile(
      offenderId = prisonNumber,
      bookingId = bookingId,
      createdBy = defaultAuditor,
      createdDateTime = currentLocalDateTime,
      modifiedBy = defaultAuditor,
      modifiedDateTime = currentLocalDateTime,
      schemaVersion = "2.0",
      profileData = objectMapper.valueToTree(profile),
      notesData = emptyJsonArray,
      new = true,
    )
  }
}
