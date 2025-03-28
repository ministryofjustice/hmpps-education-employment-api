package uk.gov.justice.digital.hmpps.educationemployment.api.integration.readinessprofile.infrastructure

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.history.Revision
import org.springframework.data.history.RevisionMetadata.RevisionType
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.RevisionInfo
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.Note
import uk.gov.justice.digital.hmpps.educationemployment.api.entity.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.infrastructure.RepositoryTestCase
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.profileOfKnownPrisoner

class ReadinessProfileRepositoryShould : RepositoryTestCase() {
  private val firstAuditor = ProfileObjects.createdBy
  private val subsequentAuditor = ProfileObjects.lastModifiedBy

  @BeforeEach
  override fun setUp() {
    super.setUp()
    setCurrentAuditor(firstAuditor)
  }

  @Test
  fun `return empty list, when nothing has been created yet`() {
    readinessProfileRepository.findAll().let {
      assertThat(it).isEmpty()
    }
  }

  @Test
  fun `return nothing, for any prison number (offender ID)`() {
    val prisonNumber = ProfileObjects.unknownPrisonNumber
    val actual = readinessProfileRepository.findById(prisonNumber)
    assertThat(actual).isEmpty
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
            profile = Note(auditor, currentTimeLocal, ActionTodo.ID, "updating info: ${index + 1}")
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

  private val ReadinessProfile.asExpected
    get() = this.copy(
      new = false,
      createdDateTime = currentTimeLocal,
      createdBy = auditor,
      modifiedDateTime = currentTimeLocal,
      modifiedBy = auditor,
    )

  private fun assertRevisionMetadata(
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
}
