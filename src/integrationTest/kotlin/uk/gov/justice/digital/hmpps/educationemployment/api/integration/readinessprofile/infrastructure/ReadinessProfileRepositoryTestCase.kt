package uk.gov.justice.digital.hmpps.educationemployment.api.integration.readinessprofile.infrastructure

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import org.assertj.core.api.Assertions.assertThat
import org.springframework.data.history.Revision
import org.springframework.data.history.RevisionMetadata.RevisionType
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.RevisionInfo
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.infrastructure.RepositoryTestCase
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.infrastructure.TestClock
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.Action
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionsRequired
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportDeclined
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkExperience
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkImpacts
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkInterests
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.circumstanceChangesRequiredToWorkList
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.infrastructure.MetricsCountByStringField
import java.time.Instant
import java.time.LocalDateTime

abstract class ReadinessProfileRepositoryTestCase : RepositoryTestCase() {
  private val typeRefProfile by lazy { object : TypeReference<Profile>() {} }

  protected open val testClock: TestClock = TestClock.defaultClock()
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

  protected val Profile.json: JsonNode get() = objectMapper.valueToTree(this)

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
    return makeReadinessProfile(prisonNumber, bookingId, profile)
  }

  protected fun makeProfileWithSupportAccepted(
    prisonNumber: String,
    bookingId: Long,
    prisonId: String,
    prisonName: String? = null,
    within12Weeks: Boolean = true,
    vararg actionTodo: ActionTodo,
  ) = makeProfile(prisonNumber, bookingId, ProfileStatus.SUPPORT_NEEDED, prisonId, prisonName, within12Weeks, supportAccepted = makeSupportAccepted(*actionTodo))

  protected fun makeSupportAccepted(vararg actionTodo: ActionTodo): SupportAccepted {
    val actionsRequired = actionTodo.map { Action(it, ActionStatus.NOT_STARTED, null, null) }
      .let { actionNotStarted ->
        val actionsCompleted = actionTodo.toSet().let { actions ->
          ActionTodo.entries.filter { it != ActionTodo.INTERVIEW_CLOTHING && !actions.contains(it) }
            .map { Action(it, ActionStatus.COMPLETED, null, null) }
        }
        ActionsRequired(auditor, currentLocalDateTime, actionNotStarted + actionsCompleted)
      }
    val workImpacts = WorkImpacts(auditor, currentLocalDateTime, emptyList(), false, false, false)
    val workInterests = WorkInterests(auditor, currentLocalDateTime, emptyList(), "", "")
    val workExperience = WorkExperience(auditor, currentLocalDateTime, "", emptyList(), "")
    return SupportAccepted(
      modifiedBy = auditor,
      modifiedDateTime = currentLocalDateTime,
      actionsRequired = actionsRequired,
      workImpacts = workImpacts,
      workInterests = workInterests,
      workExperience = workExperience,
    )
  }

  protected fun makeProfileWithNoRightToWork(
    prisonNumber: String,
    bookingId: Long,
    prisonId: String,
    prisonName: String? = null,
    within12Weeks: Boolean = true,
  ) = makeProfile(prisonNumber, bookingId, ProfileStatus.NO_RIGHT_TO_WORK, prisonId, prisonName, within12Weeks)

  protected fun makeProfileWithReadyToWork(
    prisonNumber: String,
    bookingId: Long,
    prisonId: String,
    prisonName: String? = null,
    within12Weeks: Boolean = true,
  ) = makeProfile(prisonNumber, bookingId, ProfileStatus.READY_TO_WORK, prisonId, prisonName, within12Weeks, supportAccepted = makeSupportAccepted())

  protected fun makeProfile(
    prisonNumber: String,
    bookingId: Long,
    status: ProfileStatus,
    prisonId: String,
    prisonName: String? = null,
    within12Weeks: Boolean = true,
    supportDeclined: SupportDeclined? = null,
    supportAccepted: SupportAccepted? = null,
  ) = Profile(
    status = status,
    prisonId = prisonId,
    prisonName = prisonName,
    within12Weeks = within12Weeks,
    statusChange = false,
    statusChangeDate = null,
    statusChangeType = StatusChange.NEW,
    supportDeclined = supportDeclined,
    supportAccepted = supportAccepted,
  ).let { profile -> makeReadinessProfile(prisonNumber, bookingId, profile) }

  protected fun assertEquals(expected: List<MetricsCountByStringField>, actual: List<MetricsCountByStringField>) {
    assertThat(actual).isNotEmpty.hasSize(expected.size)

    val expectedMetrics = expected.map { it.field to it }.toMap()
    val actualMetrics = actual.map { it.field to it }.toMap()

    expectedMetrics.forEach {
      val reason = it.key
      assertThat(actualMetrics).containsKeys(reason)
      val actualMetric = actualMetrics[reason]!!
      val expectedMetric = expectedMetrics[reason]!!
      assertThat(actualMetric.field).isEqualTo(expectedMetric.field)
      assertThat(actualMetric.countWithin12Weeks)
        .`as`("checking count of reason=$reason, if within 12 weeks")
        .isEqualTo(expectedMetric.countWithin12Weeks)
      assertThat(actualMetric.countOver12Weeks)
        .`as`("checking count of reason=$reason, if over 12 weeks")
        .isEqualTo(expectedMetric.countOver12Weeks)
    }
  }

  protected fun parseProfile(profileData: JsonNode): Profile = objectMapper.treeToValue(profileData, typeRefProfile)

  private fun makeReadinessProfile(prisonNumber: String, bookingId: Long, profile: Profile) = ReadinessProfile(
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
