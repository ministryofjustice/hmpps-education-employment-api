package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.v2.ReadinessProfileV2TestCase
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.ActionsRequiredDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.SupportAcceptedDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.SupportDeclinedDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.WorkExperienceDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.WorkImpactsDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.WorkInterestsDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.AbilityToWorkImpactedBy
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.Action
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.CircumstanceChangesRequiredToWork
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.IDocs
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.QualificationsAndTraining
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkTypesOfInterest
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.profileOfKnownPrisoner
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarApiDataTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarApiTestBase
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarReportTest
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class SubjectAccessRequestApiTest {

  /**
   * Check API data returned from SAR endpoint
   */
  @Nested
  @DisplayName("API data test")
  inner class ApiDataTest :
    TestCase(),
    SarApiDataTest

  /**
   * Check report content rendered
   */
  @Nested
  @DisplayName("Report test")
  inner class ReportTest :
    TestCase(),
    SarReportTest

  abstract class TestCase : SubjectAccessRequestApiTestCase() {
    @Autowired
    protected lateinit var webTestClient: WebTestClient

    private lateinit var sarReadinessProfile: ReadinessProfile
    private lateinit var sarProfile: Profile
    private val sarPrisonNumber = profileOfKnownPrisoner.offenderId

    @BeforeAll
    internal fun init() {
      currentUser = "USER1_GEN"
      sarReadinessProfile = profileOfKnownPrisoner.run {
        // set supportDeclined to null
        sarProfile = parseProfile(profileData).apply { supportDeclined = null }
        copy(profileData = sarProfile.json())
      }
    }

    override fun getSarHelper(): SarIntegrationTestHelper = sarIntegrationTestHelper
    override fun getWebTestClientInstance(): WebTestClient = webTestClient

    override fun setupTestData() {
      // Work readiness flow: NO_RIGHT_TO_WORK -> SUPPORT_DECLINED -> SUPPORT_NEEDED -> READY_TO_WORK
      val timeTicking: () -> Unit = { sarCurrentTime = sarCurrentTime.plusSeconds(60L) }
      var updatedDto: ReadinessProfileDTO

      // 1) Create WR profile: `NO_RIGHT_TO_WORK`
      givenProfilesAreCreated(sarReadinessProfile)
        .also { updatedDto = it.first() }
      timeTicking()

      // Start updating...
      val updateReadinessProfile: () -> Unit = {
        assertUpdateReadinessProfileIsOk(sarPrisonNumber, updatedDto.request())
          .also { updatedDto = it.body!! }
      }
      // 2) Update WR profile: status change to `SUPPORT_DECLINED`
      updatedDto.profileData.status = ProfileStatus.SUPPORT_DECLINED
      updatedDto.profileData.supportDeclined = makeSupportDeclined()
      updateReadinessProfile()
      timeTicking()

      // 3) Update WR profile: status change to `SUPPORT_NEEDED`
      currentUser = "USER2_GEN"
      updatedDto.profileData.apply {
        status = ProfileStatus.SUPPORT_NEEDED
        statusChangeType = StatusChange.DECLINED_TO_ACCEPTED
      }
      updatedDto.profileData.supportAccepted = makeSupportAccepted()
      updateReadinessProfile()
      timeTicking()

      val actions = updatedDto.profileData.supportAccepted!!.actionsRequired.actions

      // 4) Add WR notes (to actions To-Do)
      actions.forEach { assertAddNoteIsOk(sarPrisonNumber, it.todoItem, "adding some notes") }
      timeTicking()

      // 5) Start action items
      // 6) Complete action items
      listOf(
        ActionStatus.IN_PROGRESS,
        ActionStatus.COMPLETED,
      ).forEach { status ->
        val updatedActions = actions.run { actions.map { it.copy(status = status) } }
        updatedDto.profileData.apply { supportAccepted = supportAccepted!!.run { copy(actionsRequired = actionsRequired.copy(actions = updatedActions)) } }
        updateReadinessProfile()
        timeTicking()
      }

      // 7) Update WR profile: status change to `READY_TO_WORK`
      updatedDto.profileData.status = ProfileStatus.READY_TO_WORK
      updateReadinessProfile()
      timeTicking()
    }

    override fun getPrn(): String? = sarPrisonNumber

    private fun makeSupportAccepted(): SupportAcceptedDTO = SupportAcceptedDTO(
      modifiedBy = currentUser,
      modifiedDateTime = sarCurrentTime,
      actionsRequired = ActionsRequiredDTO(
        modifiedBy = currentUser,
        modifiedDateTime = sarCurrentTime,
        actions = listOf(
          makeAction(ActionTodo.BANK_ACCOUNT),
          makeAction(ActionTodo.HOUSING),
          makeAction(
            ActionTodo.ID,
            id = listOf(
              IDocs.DRIVING_LICENCE,
              IDocs.PASSPORT,
              IDocs.BIRTH_CERTIFICATE,
              IDocs.OTHER,
            ),
            other = "Some other ID documents",
          ),
        ),
      ),
      workImpacts = WorkImpactsDTO(
        modifiedBy = currentUser,
        modifiedDateTime = sarCurrentTime,
        abilityToWorkImpactedBy = listOf(
          AbilityToWorkImpactedBy.MENTAL_HEALTH_ISSUES,
          AbilityToWorkImpactedBy.PHYSICAL_HEALTH_ISSUES,
        ),
        caringResponsibilitiesFullTime = true,
        ableToManageMentalHealth = true,
        ableToManageDependencies = false,
      ),
      workInterests = WorkInterestsDTO(
        modifiedBy = currentUser,
        modifiedDateTime = sarCurrentTime,
        workTypesOfInterest = listOf(
          WorkTypesOfInterest.CONSTRUCTION,
          WorkTypesOfInterest.MANUFACTURING,
          WorkTypesOfInterest.OTHER,
        ),
        workTypesOfInterestOther = "Some other work types of interest",
        jobOfParticularInterest = "A dummy job",
      ),
      workExperience = WorkExperienceDTO(
        modifiedBy = currentUser,
        modifiedDateTime = sarCurrentTime,
        previousWorkOrVolunteering = "Admin in a NGO",
        qualificationsAndTraining = listOf(
          QualificationsAndTraining.DRIVING_LICENSE,
          QualificationsAndTraining.HGV_LICENSE,
          QualificationsAndTraining.MACHINERY,
          QualificationsAndTraining.OTHER,
        ),
        qualificationsAndTrainingOther = "Some other training",
      ),
    )

    private fun makeSupportDeclined(): SupportDeclinedDTO = SupportDeclinedDTO(
      modifiedBy = currentUser,
      modifiedDateTime = sarCurrentTime,
      supportToWorkDeclinedReason = listOf(
        SupportToWorkDeclinedReason.HEALTH,
        SupportToWorkDeclinedReason.OTHER,
      ),
      supportToWorkDeclinedReasonOther = "Some other reasons",
      circumstanceChangesRequiredToWork = listOf(
        CircumstanceChangesRequiredToWork.MENTAL_HEALTH_SUPPORT,
        CircumstanceChangesRequiredToWork.OTHER,
      ),
      circumstanceChangesRequiredToWorkOther = "Some other circumstance changes",
    )

    private fun makeAction(
      todoItem: ActionTodo,
      status: ActionStatus = ActionStatus.NOT_STARTED,
      other: String? = null,
      id: List<IDocs>? = null,
    ) = Action(todoItem, status, other, id)

    private fun ReadinessProfileDTO.request() = run { ReadinessProfileRequestDTO(bookingId, profileData) }
  }
}

@Transactional(propagation = Propagation.NOT_SUPPORTED)
abstract class SubjectAccessRequestApiTestCase :
  ReadinessProfileV2TestCase(),
  SarApiTestBase {

  protected var sarCurrentTime: Instant = defaultCurrentTime

  protected val sarTimezoneId: ZoneId = ZoneId.of("Europe/London")
  protected val sarCurrentTimeLocal: LocalDateTime get() = sarCurrentTime.atZone(sarTimezoneId).toLocalDateTime()

  override fun getCrn(): String? = null
  override fun getFromDate(): LocalDate? = sarCurrentTimeLocal.minusDays(1L).toLocalDate()
  override fun getToDate(): LocalDate? = sarCurrentTimeLocal.plusDays(1L).toLocalDate()

  @BeforeEach
  internal fun setup() {
    whenever(dateTimeProvider.now).thenAnswer { Optional.of(sarCurrentTime) }
    whenever(timeProvider.timeZoneId).thenReturn(sarTimezoneId)
    whenever(timeProvider.now()).thenAnswer { sarCurrentTimeLocal }
  }
}
