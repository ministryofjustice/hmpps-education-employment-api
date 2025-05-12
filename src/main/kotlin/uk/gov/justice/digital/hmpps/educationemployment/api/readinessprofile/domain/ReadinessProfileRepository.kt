package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.infrastructure.MetricsCountByStringField
import java.time.Instant

@Repository
interface ReadinessProfileRepository :
  JpaRepository<ReadinessProfile, String>,
  RevisionRepository<ReadinessProfile, String, Long> {

  @Query(
    """
    WITH recent_updates AS (
      SELECT rev_number, offender_id FROM work_readiness_audit wra
      WHERE wra.modified_date_time BETWEEN :startTime AND :endTime AND wra.rev_type IN (0,1)
    ), latest AS (
      SELECT offender_id, MAX(rev_number) AS rev_number FROM recent_updates GROUP BY offender_id
    ), latest_profiles AS (
      SELECT wra.offender_id, 
        wra.profile_data->>'prisonId' AS prison_id,
        COALESCE((wra.profile_data->>'within12Weeks')::boolean, true) AS within_12_weeks,
        wra.profile_data#>'{supportDeclined, supportToWorkDeclinedReason}' AS declined_reasons
      FROM work_readiness_audit wra INNER JOIN latest l
      ON L.offender_id = wra.offender_id AND l.rev_number = wra.rev_number
      WHERE wra.profile_data->>'status' = 'SUPPORT_DECLINED'
      AND wra.profile_data->>'prisonId' = :prisonId
    ), declined_reasons AS (
      SELECT p.offender_id, p.within_12_weeks, dr.value AS declined_reason FROM latest_profiles p, jsonb_array_elements_text(declined_reasons) AS dr
    )
    SELECT declined_reason as field,
      COUNT(CASE within_12_weeks WHEN TRUE THEN 1 ELSE 0 END) AS countWithin12Weeks,
      COUNT(CASE within_12_weeks WHEN FALSE THEN 1 ELSE 0 END) AS countOver12Weeks
    FROM declined_reasons GROUP BY 1 ORDER BY 1;
    """,
    nativeQuery = true,
  )
  fun countReasonsForSupportDeclinedByPrisonIdAndDateTimeBetween(
    prisonId: String,
    startTime: Instant,
    endTime: Instant,
  ): List<MetricsCountByStringField>
}
