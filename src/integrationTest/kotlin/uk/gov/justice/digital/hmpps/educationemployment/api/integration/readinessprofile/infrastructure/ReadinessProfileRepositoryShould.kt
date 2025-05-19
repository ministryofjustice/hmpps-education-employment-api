package uk.gov.justice.digital.hmpps.educationemployment.api.integration.readinessprofile.infrastructure

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.history.RevisionMetadata.RevisionType
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.RevisionInfo
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.infrastructure.MetricsCountForTest
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.infrastructure.TestClock
import uk.gov.justice.digital.hmpps.educationemployment.api.notesdata.domain.Note
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo.BANK_ACCOUNT
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo.CV_AND_COVERING_LETTER
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo.DISCLOSURE_LETTER
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo.EMAIL
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo.HOUSING
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo.ID
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo.PHONE
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus.NO_RIGHT_TO_WORK
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus.READY_TO_WORK
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus.SUPPORT_DECLINED
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus.SUPPORT_NEEDED
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason.ALREADY_HAS_WORK
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason.FULL_TIME_CARER
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason.HEALTH
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason.HOUSING_NOT_IN_PLACE
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason.LACKS_CONFIDENCE_OR_MOTIVATION
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason.LIMIT_THEIR_ABILITY
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason.NO_REASON
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason.OTHER
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason.RETIRED
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason.RETURNING_TO_JOB
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason.SELF_EMPLOYED
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.profileOfAnotherPrisoner
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.profileOfDeclinedSupportPrisoner
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.profileOfKnownPrisoner
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.unknownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.infrastructure.MetricsCountByStringField
import java.time.Instant

class ReadinessProfileRepositoryShould : ReadinessProfileRepositoryTestCase() {
  private val startOfTime = Instant.parse("2025-01-01T00:00:00Z")
  private val timeslotClock = TestClock.timeslotClock(startOfTime)
  private val timeslotDuration = TestClock.TimeslotClock.defaultDuration
  override val testClock = timeslotClock

  private val firstAuditor = ProfileObjects.createdBy
  private val subsequentAuditor = ProfileObjects.lastModifiedBy

  private val prisonId = ProfileObjects.prison1
  private val prisonName = ProfileObjects.prison1Name
  private val startTime = startOfTime
  private val endTime = startOfTime

  @BeforeEach
  override fun setUp() {
    super.setUp()
    setCurrentAuditor(firstAuditor)
    timeslotClock.timeslot.set(0)
  }

  @Test
  fun `return empty list, when nothing has been created yet`() {
    readinessProfileRepository.findAll().let {
      assertThat(it).isEmpty()
    }
  }

  @Test
  fun `return nothing, for any prison number (offender ID)`() {
    val prisonNumber = unknownPrisonNumber
    val actual = readinessProfileRepository.findById(prisonNumber)
    assertThat(actual).isEmpty
  }

  @Test
  fun `return empty list, for metric of reasons for support declined`() {
    val counts = readinessProfileRepository.countReasonsForSupportDeclinedByPrisonIdAndDateTimeBetween(prisonId, startTime, endTime)
    assertThat(counts).isEmpty()
  }

  @Nested
  @DisplayName("Given a known prisoner")
  inner class GivenPrisoner {
    private val newProfile = profileOfKnownPrisoner
    private val prisonNumber = newProfile.offenderId

    @Nested
    @DisplayName("and a new readiness profile")
    inner class AndNewReadinessProfile {
      @Test
      fun `return nothing, for given prison number`() {
        val actual = readinessProfileRepository.findById(prisonNumber)
        assertThat(actual).isEmpty
      }

      @Test
      fun `create a new profile with given prison number`() {
        val saved = readinessProfileRepository.save(newProfile)

        assertThat(saved).isEqualTo(newProfile.asExpected)
      }
    }

    @Nested
    @DisplayName("and an existing readiness profile")
    inner class AndExistingReadinessProfile {
      private lateinit var existingProfile: ReadinessProfile

      @BeforeEach
      internal fun setUp() {
        existingProfile = readinessProfileRepository.save(newProfile)
      }

      @Test
      fun `update existing profile`() {
        val saved = readinessProfileRepository.save(existingProfile)

        assertThat(saved).isEqualTo(existingProfile.asExpected)
      }

      @Nested
      @DisplayName("And revision(s) of the readiness profile has/have been maintained")
      @Transactional(propagation = Propagation.NOT_SUPPORTED)
      inner class AndRevisionOfProfileMaintained {
        private lateinit var profile: ReadinessProfile

        @BeforeEach
        internal fun setUp() {
          profile = existingProfile
        }

        @Test
        fun `retrieve the latest revision of the readiness profile`() {
          val revisions = readinessProfileRepository.findRevisions(profile.offenderId)

          assertThat(revisions).isNotEmpty
          revisions.latestRevision.let { latest ->
            with(latest.metadata) {
              assertThat(revisionType).isEqualTo(RevisionType.INSERT)
              assertThat(getDelegate<RevisionInfo>().createdBy).isEqualTo(auditor)
            }
            assertEquals(latest.entity, profile)
            with(latest.entity) {
              assertThat(createdBy).isNotNull
              assertThat(modifiedBy).isNotNull
              assertThat(createdDateTime).isNotNull
              assertThat(modifiedDateTime).isNotNull
            }
          }
        }

        @Test
        fun `retrieve all revisions of the readiness profile, when it has been updated multiple times`() {
          setCurrentAuditor(subsequentAuditor)

          val updateCount = 3
          repeat(updateCount) { index ->
            profile = Note(auditor, currentTimeLocal, ID, "updating info: ${index + 1}")
              .let { (profile.notesData as ArrayNode).deepCopy().add(objectMapper.valueToTree(it) as JsonNode) }
              .let { readinessProfileRepository.saveAndFlush(profile.copy(notesData = it)) }
          }
          val expectedRevisionCount = updateCount + 1

          val revisions = readinessProfileRepository.findRevisions(prisonNumber)
          assertThat(revisions).isNotEmpty
          assertThat(revisions.content.count()).isEqualTo(expectedRevisionCount)
          assertRevisionMetadata(RevisionType.UPDATE, auditor, revisions.latestRevision)
          assertRevisionMetadata(RevisionType.INSERT, firstAuditor, revisions.content[0])
          assertRevisionMetadata(
            expectedRevisionType = RevisionType.UPDATE,
            expectedCreator = subsequentAuditor,
            revisions = revisions.content.subList(1, revisions.content.size).toTypedArray(),
          )
        }
      }
    }
  }

  @Nested
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  @DisplayName("Given some readiness profile with revision(s)")
  inner class GivenExistingReadinessProfilesWithRevisions {
    private lateinit var profiles: List<ReadinessProfile>

    @BeforeEach
    internal fun setUp() {
      profiles = listOf(
        profileOfKnownPrisoner,
        profileOfAnotherPrisoner,
        profileOfDeclinedSupportPrisoner,
      ).map { readinessProfileRepository.save(it) }.toList()
    }

    @Test
    fun `return metric of reasons for support declined`() {
      val expectedReasons = listOf(FULL_TIME_CARER, SELF_EMPLOYED, RETURNING_TO_JOB, ALREADY_HAS_WORK).map { it.name }.toSet()

      val counts = readinessProfileRepository.countReasonsForSupportDeclinedByPrisonIdAndDateTimeBetween(prisonId, startTime, endTime)

      // There is only one profile with support declined
      assertThat(counts).isNotEmpty.hasSize(expectedReasons.size)
      val actualReasons = counts.map { it.field }.toSet()
      assertThat(actualReasons).isEqualTo(expectedReasons)
      // All reasons were from the single profile over 12 weeks from release
      counts.map { it.countWithin12Weeks }.toSet().let { assertThat(it).containsOnly(0L) }
      counts.map { it.countOver12Weeks }.toSet().let { assertThat(it).containsOnly(1L) }
    }
  }

  @Nested
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  @DisplayName("Given many profiles with support declined")
  inner class GIvenManyProfilesWithSupportDeclined {
    private lateinit var profiles: MutableList<ReadinessProfile>
    private lateinit var profileMap: MutableMap<String, ReadinessProfile>

    @BeforeEach
    internal fun setUp() {
      auditCleaner.deleteAllRevisions()
      givenProfilesInFourTimeslots()
    }

    @Test
    fun `return metric of reasons for support declined`() {
      // time t = [1,4]
      val startTime = startOfTime + timeslotDuration
      val endTime = currentTime

      val expectedMetrics = listOf(
        makeMetricCount(ALREADY_HAS_WORK, 4, 1),
        makeMetricCount(LIMIT_THEIR_ABILITY, 12, 0),
        makeMetricCount(FULL_TIME_CARER, 1, 0),
        makeMetricCount(HOUSING_NOT_IN_PLACE, 11, 1),
        makeMetricCount(LACKS_CONFIDENCE_OR_MOTIVATION, 3, 0),
        makeMetricCount(HEALTH, 6, 4),
        makeMetricCount(NO_REASON, 2, 0),
        makeMetricCount(RETIRED, 7, 0),
        makeMetricCount(RETURNING_TO_JOB, 2, 0),
        makeMetricCount(SELF_EMPLOYED, 13, 1),
        makeMetricCount(OTHER, 8, 3),
      )

      val actualMetrics = readinessProfileRepository.countReasonsForSupportDeclinedByPrisonIdAndDateTimeBetween(prisonId, startTime, endTime)

      assertThat(actualMetrics).isNotEmpty.hasSize(SupportToWorkDeclinedReason.entries.size)
      assertEquals(expectedMetrics, actualMetrics)
    }

    @Test
    fun `return metrics with profile, that was over 12 weeks before the period, and within 12 weeks during the period`() {
      // time t = [2,2]
      val startTime = startOfTime + timeslotDuration.multipliedBy(2)
      val endTime = startTime
      // expected counts from profiles:   within={2} ; over={5, 9, 15},
      // and then resolved to these reasons: (two with zero counts: FULL_TIME_CARER, NO_REASON)
      val expectedMetrics = listOf(
        makeMetricCount(ALREADY_HAS_WORK, 1, 0),
        makeMetricCount(LIMIT_THEIR_ABILITY, 1, 2),
        makeMetricCount(FULL_TIME_CARER, 0, 0),
        makeMetricCount(HOUSING_NOT_IN_PLACE, 1, 3),
        makeMetricCount(LACKS_CONFIDENCE_OR_MOTIVATION, 1, 0),
        makeMetricCount(HEALTH, 1, 2),
        makeMetricCount(NO_REASON, 0, 0),
        makeMetricCount(RETIRED, 1, 1),
        makeMetricCount(RETURNING_TO_JOB, 1, 0),
        makeMetricCount(SELF_EMPLOYED, 1, 2),
        makeMetricCount(OTHER, 1, 2),
      ).filter { it.countWithin12Weeks > 0 || it.countOver12Weeks > 0 }

      val counts = readinessProfileRepository.countReasonsForSupportDeclinedByPrisonIdAndDateTimeBetween(prisonId, startTime, endTime)

      assertEquals(expectedMetrics, counts)
    }

    @Test
    fun `return metrics with profile, that was within 12 weeks and becoming over during the period (check beginning snapshot)`() {
      // time t = [2,3]
      val startTime = startOfTime + timeslotDuration.multipliedBy(2)
      val endTime = startOfTime + timeslotDuration.multipliedBy(3)
      // expected counts from profiles:   within={2, 3, 6, 7, 9} ; over={5, 10, 15, 16},
      // and then resolved to these reasons: (two with zero counts: FULL_TIME_CARER, NO_REASON)
      val expectedMetrics = listOf(
        makeMetricCount(ALREADY_HAS_WORK, 2, 0),
        makeMetricCount(LIMIT_THEIR_ABILITY, 5, 2),
        makeMetricCount(FULL_TIME_CARER, 0, 0),
        makeMetricCount(HOUSING_NOT_IN_PLACE, 5, 3),
        makeMetricCount(LACKS_CONFIDENCE_OR_MOTIVATION, 2, 0),
        makeMetricCount(HEALTH, 3, 3),
        makeMetricCount(NO_REASON, 0, 0),
        makeMetricCount(RETIRED, 4, 1),
        makeMetricCount(RETURNING_TO_JOB, 1, 0),
        makeMetricCount(SELF_EMPLOYED, 5, 2),
        makeMetricCount(OTHER, 4, 3),
      ).filter { it.countWithin12Weeks > 0 || it.countOver12Weeks > 0 }

      val counts = readinessProfileRepository.countReasonsForSupportDeclinedByPrisonIdAndDateTimeBetween(prisonId, startTime, endTime)

      assertEquals(expectedMetrics, counts)
    }

    private fun givenProfilesInFourTimeslots() {
      givenSomeProfiles()
      profileMap = mutableMapOf()

      // offender ID mapping to index (1-based)
      val offenderIdToIdxMap = profiles.mapIndexed { i, it -> it.offenderId to i + 1 }.toMap()
      val updatesWithin12WeeksAtTimeT = sortedMapOf(
        0 to arrayOf(),
        1 to arrayOf(1, 5, 6, 11, 12, 13),
        2 to arrayOf(2),
        3 to arrayOf(3, 7, 9),
        4 to arrayOf(4, 8, 10),
      )
      val updatesOver12WeeksAtTimeT = sortedMapOf(
        0 to arrayOf(),
        1 to arrayOf(9, 10, 14),
        2 to arrayOf(5, 9, 15),
        3 to arrayOf(6, 10, 15, 16),
        4 to arrayOf(7, 17),
      )

      updatesWithin12WeeksAtTimeT.keys.forEach { t ->
        val updatedProfiles = mutableListOf<ReadinessProfile>()
        updatesWithin12WeeksAtTimeT[t]?.map { idx -> profileAt(idx).within12Weeks() }?.let { updatedProfiles += it }
        updatesOver12WeeksAtTimeT[t]?.map { idx -> profileAt(idx).over12Weeks() }?.let { updatedProfiles += it }
        if (updatedProfiles.isNotEmpty()) {
          timeslotClock.timeslot.set(t.toLong())
          updatedProfiles.forEach { it.addNote(ID, "making changes") }
          readinessProfileRepository.saveAllAndFlush(updatedProfiles).onEach {
            profileMap[it.offenderId] = it
            offenderIdToIdxMap[it.offenderId]?.let { idx -> refreshProfileAt(idx, it) }
          }
        }
      }
    }

    private fun givenSomeProfiles() {
      // LIMIT_THEIR_ABILITY on screen:  "Feels type of offence will limit their ability to find work", and on dashboard "Type of offence"
      val reasons = listOf(
        arrayOf(ALREADY_HAS_WORK, LIMIT_THEIR_ABILITY, FULL_TIME_CARER, HOUSING_NOT_IN_PLACE, LACKS_CONFIDENCE_OR_MOTIVATION, HEALTH, RETIRED, RETURNING_TO_JOB, SELF_EMPLOYED, OTHER),
        arrayOf(ALREADY_HAS_WORK, LIMIT_THEIR_ABILITY, HOUSING_NOT_IN_PLACE, LACKS_CONFIDENCE_OR_MOTIVATION, HEALTH, RETIRED, RETURNING_TO_JOB, SELF_EMPLOYED, OTHER),
        arrayOf(ALREADY_HAS_WORK, LIMIT_THEIR_ABILITY, HOUSING_NOT_IN_PLACE, LACKS_CONFIDENCE_OR_MOTIVATION, HEALTH, RETIRED, SELF_EMPLOYED, OTHER),
        arrayOf(ALREADY_HAS_WORK, LIMIT_THEIR_ABILITY, HOUSING_NOT_IN_PLACE, HEALTH, RETIRED, SELF_EMPLOYED, OTHER),
        arrayOf(LIMIT_THEIR_ABILITY, HOUSING_NOT_IN_PLACE, HEALTH, RETIRED, SELF_EMPLOYED, OTHER),
        arrayOf(LIMIT_THEIR_ABILITY, HOUSING_NOT_IN_PLACE, HEALTH, RETIRED, SELF_EMPLOYED, OTHER),
        arrayOf(LIMIT_THEIR_ABILITY, HOUSING_NOT_IN_PLACE, RETIRED, SELF_EMPLOYED, OTHER),
        arrayOf(LIMIT_THEIR_ABILITY, HOUSING_NOT_IN_PLACE, SELF_EMPLOYED, OTHER),
        arrayOf(LIMIT_THEIR_ABILITY, HOUSING_NOT_IN_PLACE, SELF_EMPLOYED),
        arrayOf(LIMIT_THEIR_ABILITY, HOUSING_NOT_IN_PLACE, SELF_EMPLOYED),
        arrayOf(LIMIT_THEIR_ABILITY, HOUSING_NOT_IN_PLACE, SELF_EMPLOYED),
        arrayOf(LIMIT_THEIR_ABILITY, NO_REASON, SELF_EMPLOYED),
        arrayOf(NO_REASON, SELF_EMPLOYED),
        arrayOf(ALREADY_HAS_WORK, HEALTH, SELF_EMPLOYED, OTHER),
        arrayOf(HOUSING_NOT_IN_PLACE, HEALTH, OTHER),
        arrayOf(HEALTH, OTHER),
        arrayOf(HEALTH),
      )
      // At last/final time t:  Profiles #1 to #13 are within 12 weeks; profiles #14 to #17 are over 12 weeks
      profiles = (1..17).map { i ->
        makeProfileDeclined(
          prisonNumber = "D%04dDD".format(i),
          bookingId = i.toLong(),
          within12Weeks = (i <= 13),
          *reasons[i - 1],
        )
      }.toMutableList()
    }

    private fun makeProfileDeclined(
      prisonNumber: String,
      bookingId: Long,
      within12Weeks: Boolean,
      vararg reasons: SupportToWorkDeclinedReason,
    ) = makeProfileWithSupportDeclined(
      prisonNumber = prisonNumber,
      bookingId = bookingId,
      prisonId = prisonId,
      prisonName = prisonName,
      within12Weeks = within12Weeks,
      *reasons,
    )

    private fun makeMetricCount(
      reason: SupportToWorkDeclinedReason,
      countWithin12Weeks: Long,
      countOver12Weeks: Long,
    ): MetricsCountByStringField = MetricsCountForTest(reason.name, countWithin12Weeks, countOver12Weeks)

    private fun profileAt(idx: Int) = profiles[idx - 1]
    private fun refreshProfileAt(idx: Int, updatedProfile: ReadinessProfile) {
      profiles[idx - 1] = updatedProfile
    }
    private fun ReadinessProfile.within12Weeks() = copyAs(true)
    private fun ReadinessProfile.over12Weeks() = copyAs(false)
    private fun ReadinessProfile.copyAs(within12Weeks: Boolean) = (profileData.deepCopy() as ObjectNode)
      .put("within12Weeks", within12Weeks).let {
        copy(
          profileData = it,
          modifiedDateTime = currentLocalDateTime,
          createdDateTime = createdDateTime,
        )
      }
    private fun ReadinessProfile.addNote(attribute: ActionTodo, noteText: String) = this.apply {
      (notesData as ArrayNode).add(
        Note(
          attribute = attribute,
          text = noteText,
          createdBy = auditor,
          createdDateTime = currentLocalDateTime,
        ).let { objectMapper.valueToTree(it) as JsonNode },
      )
    }
  }

  @Nested
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  @DisplayName("Given many profiles with support accepted")
  inner class GIvenManyProfilesWithSupportAccepted {
    private lateinit var profiles: MutableList<ReadinessProfile>

    @BeforeEach
    internal fun setUp() {
      auditCleaner.deleteAllRevisions()
      givenProfilesInOneTimeslot()
    }

    @Test
    fun `return metric of documentation and support needed`() {
      // time t = [0,0]
      val startTime = startOfTime
      val endTime = currentTime

      val expectedMetrics = listOf(
        makeMetricCount(BANK_ACCOUNT, 2, 1),
        makeMetricCount(CV_AND_COVERING_LETTER, 22, 6),
        makeMetricCount(DISCLOSURE_LETTER, 12, 6),
        makeMetricCount(EMAIL, 16, 6),
        makeMetricCount(HOUSING, 21, 6),
        makeMetricCount(ID, 18, 4),
        makeMetricCount(PHONE, 4, 5),
      )

      val actualMetrics = readinessProfileRepository.countDocumentsSupportNeededByPrisonIdAndDateTimeBetween(prisonId, startTime, endTime)

      // cannot verify with enum size as INTERVIEW_CLOTHING is not included
      assertThat(actualMetrics).isNotEmpty.hasSize(expectedMetrics.size)
      assertEquals(expectedMetrics, actualMetrics)
    }

    private fun givenProfilesInOneTimeslot() {
      givenProfilesWithSupportAccepted()
      // save all profiles at time t=0
      readinessProfileRepository.saveAllAndFlush(profiles)
    }

    private fun givenProfilesWithSupportAccepted() {
      val documentsSupport = mutableListOf<Array<ActionTodo>>()
      // profiles within12w
      documentsSupport +=
        (1..2).map { arrayOf(BANK_ACCOUNT, CV_AND_COVERING_LETTER, DISCLOSURE_LETTER, EMAIL, HOUSING, ID, PHONE) } +
        (3..4).map { arrayOf(CV_AND_COVERING_LETTER, DISCLOSURE_LETTER, EMAIL, HOUSING, ID, PHONE) } +
        (5..12).map { arrayOf(CV_AND_COVERING_LETTER, DISCLOSURE_LETTER, EMAIL, HOUSING, ID) } +
        (13..16).map { arrayOf(CV_AND_COVERING_LETTER, EMAIL, HOUSING, ID) } +
        (17..18).map { arrayOf(CV_AND_COVERING_LETTER, HOUSING, ID) } +
        (19..21).map { arrayOf(CV_AND_COVERING_LETTER, HOUSING) }
      documentsSupport.add(arrayOf(CV_AND_COVERING_LETTER))
      // profiles over12w
      documentsSupport.add(arrayOf(BANK_ACCOUNT, CV_AND_COVERING_LETTER, DISCLOSURE_LETTER, EMAIL, HOUSING, ID, PHONE))
      documentsSupport += (24..26).map { arrayOf(CV_AND_COVERING_LETTER, DISCLOSURE_LETTER, EMAIL, HOUSING, ID, PHONE) }
      documentsSupport.add(arrayOf(CV_AND_COVERING_LETTER, DISCLOSURE_LETTER, EMAIL, HOUSING, PHONE))
      documentsSupport.add(arrayOf(CV_AND_COVERING_LETTER, DISCLOSURE_LETTER, EMAIL, HOUSING))

      // Profiles #1 to #22 are within 12 weeks; profiles #23 to #28 are over 12 weeks
      profiles = (1..28).map { i ->
        makeProfileAccepted(
          prisonNumber = "S%04dAA".format(i),
          bookingId = i.toLong(),
          within12Weeks = (i <= 22),
          *documentsSupport[i - 1],
        )
      }.toMutableList()
    }

    private fun makeProfileAccepted(
      prisonNumber: String,
      bookingId: Long,
      within12Weeks: Boolean,
      vararg documentsSupport: ActionTodo,
    ) = makeProfileWithSupportAccepted(
      prisonNumber = prisonNumber,
      bookingId = bookingId,
      prisonId = prisonId,
      prisonName = prisonName,
      within12Weeks = within12Weeks,
      *documentsSupport,
    )

    private fun makeMetricCount(
      actionTodo: ActionTodo,
      countWithin12Weeks: Long,
      countOver12Weeks: Long,
    ): MetricsCountByStringField = MetricsCountForTest(actionTodo.name, countWithin12Weeks, countOver12Weeks)
  }

  @Nested
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  @DisplayName("Given many profiles with status changes")
  inner class GIvenManyProfilesWithStatusChanges {
    private lateinit var profiles: MutableList<ReadinessProfile>
    private lateinit var profileMap: MutableMap<String, ReadinessProfile>
    private lateinit var offenderIdToIdxMap: Map<String, Int>

    @BeforeEach
    internal fun setUp() {
      auditCleaner.deleteAllRevisions()
      profiles = mutableListOf()
      profileMap = mutableMapOf()
      givenProfilesInTwoTimeslots()
    }

    @Test
    fun `return metric of work status counts`() {
      // time t = [1,2]
      val startTime = startOfTime + timeslotDuration
      val endTime = startOfTime + timeslotDuration.multipliedBy(2)

      val expectedMetrics = listOf(
        makeMetricCount(SUPPORT_NEEDED, 22, 6),
        makeMetricCount(READY_TO_WORK, 12, 0),
        makeMetricCount(SUPPORT_DECLINED, 16, 2),
        makeMetricCount(NO_RIGHT_TO_WORK, 21, 0),
      )

      val actualMetrics = readinessProfileRepository.countWorkStatusByPrisonIdAndDateTimeBetween(prisonId, startTime, endTime)

      assertThat(actualMetrics).isNotEmpty.hasSize(ProfileStatus.entries.size)
      assertEquals(expectedMetrics, actualMetrics)
    }

    @Test
    fun `return metric of work status change count`() {
      // time t = [1,2]
      val startTime = startOfTime + timeslotDuration
      val endTime = startOfTime + timeslotDuration.multipliedBy(2)
      val expectedMetric = 28L

      val actualMetric = readinessProfileRepository.countWorkStatusChangeByPrisonIdAndDateTimeBetween(prisonId, startTime, endTime)

      assertEquals(expectedMetric, actualMetric)
    }

    private fun givenProfilesInTwoTimeslots() {
      // 1. save all testing profiles
      givenSomeProfiles()
      timeslotClock.timeslot.set(1L)
      readinessProfileRepository.saveAllAndFlush(profiles).onEach { refreshProfile(it) }

      // 2. change status of profiles #1 to #28, from support declined to accepted
      timeslotClock.timeslot.set(2L)
      (1..28).map { idx ->
        profileAt(idx).apply {
          profileData = parseProfile(profileData).transitSupportDeclinedToAccepted().json
          modifiedDateTime = currentLocalDateTime
        }
      }.let { readinessProfileRepository.saveAllAndFlush(it).onEach { refreshProfile(it) } }
    }

    private fun givenSomeProfiles() {
      profiles += (1L..28).map { i ->
        makeProfileWithSupportDeclined("S%04dSS".format(i), i, prisonId, prisonName, (i <= 22), NO_REASON)
      } + (29L..40).map { i ->
        makeProfileWithReadyToWork("R%04dRR".format(i), i, prisonId, prisonName, true)
      } + (41L..58).map { i ->
        makeProfileWithSupportDeclined("D%04dDD".format(i), i, prisonId, prisonName, (i <= 56), NO_REASON)
      } + (59L..79).map { i ->
        makeProfileWithNoRightToWork("N%04dNN".format(i), i, prisonId, prisonName, true)
      }
      // offender ID mapping to index (1-based)
      offenderIdToIdxMap = profiles.mapIndexed { i, it -> it.offenderId to i + 1 }.toMap()
    }

    private fun profileAt(idx: Int) = profiles[idx - 1]
    private fun refreshProfileAt(idx: Int, updatedProfile: ReadinessProfile) {
      profiles[idx - 1] = updatedProfile
    }

    private fun refreshProfile(updatedProfile: ReadinessProfile): Unit = updatedProfile.let {
      profileMap[it.offenderId] = it
      offenderIdToIdxMap[it.offenderId]?.let { idx -> refreshProfileAt(idx, it) }
    }

    private fun makeMetricCount(
      status: ProfileStatus,
      countWithin12Weeks: Long,
      countOver12Weeks: Long,
    ): MetricsCountByStringField = MetricsCountForTest(status.name, countWithin12Weeks, countOver12Weeks)

    private fun Profile.transitSupportDeclinedToAccepted() = this.apply {
      status = SUPPORT_NEEDED
      statusChange = true
      statusChangeType = StatusChange.DECLINED_TO_ACCEPTED
      statusChangeDate = currentLocalDateTime
      supportAccepted = makeSupportAccepted(*ActionTodo.entries.toTypedArray())
      supportDeclined = null
    }
  }
}
