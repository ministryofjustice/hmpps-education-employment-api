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
      SELECT rev_number, offender_id, profile_data FROM work_readiness_audit wra
      WHERE wra.modified_date_time BETWEEN :startTime AND :endTime AND wra.rev_type IN (0,1)
    ), profile_at_start AS (
      SELECT wra1.rev_number, wra1.offender_id, wra1.profile_data FROM work_readiness_audit wra1
      INNER JOIN (
        SELECT offender_id, max(rev_number) as rev_number 
        FROM work_readiness_audit wra2
        WHERE wra2.offender_id IN (SELECT DISTINCT offender_id from recent_updates) AND wra2.modified_date_time <= :startTime AND wra2.rev_type IN (0,1)
        GROUP BY 1
      ) at_start 
      ON wra1.offender_id = at_start.offender_id AND wra1.rev_number = at_start.rev_number
      AND at_start.rev_number NOT IN (SELECT rev_number from recent_updates)
    ), profile_snapshots AS (
      SELECT * FROM recent_updates UNION ALL SELECT * FROM profile_at_start
    ), latest AS (
      SELECT offender_id, MAX(rev_number) AS rev_number, BOOL_OR(COALESCE((profile_data->>'within12Weeks')::boolean, true)) as within_12_weeks
      FROM profile_snapshots GROUP BY offender_id
    ), latest_profiles AS (
      SELECT p.offender_id, 
        p.profile_data->>'prisonId' AS prison_id,
        l.within_12_weeks,
        p.profile_data#>'{supportDeclined, supportToWorkDeclinedReason}' AS declined_reasons
      FROM profile_snapshots p INNER JOIN latest l
      ON L.offender_id = p.offender_id AND l.rev_number = p.rev_number
      WHERE p.profile_data->>'status' = 'SUPPORT_DECLINED'
      AND p.profile_data->>'prisonId' = :prisonId
    ), declined_reasons AS (
      SELECT p.offender_id, p.within_12_weeks, dr.value AS declined_reason FROM latest_profiles p, jsonb_array_elements_text(declined_reasons) AS dr
    )
    SELECT declined_reason as field,
      COUNT(CASE within_12_weeks WHEN TRUE THEN 1 ELSE NULL END) AS countWithin12Weeks,
      COUNT(CASE within_12_weeks WHEN FALSE THEN 1 ELSE NULL END) AS countOver12Weeks
    FROM declined_reasons 
    WHERE declined_reason IS NOT NULL 
    GROUP BY 1 ORDER BY 1;
    """,
    nativeQuery = true,
  )
  fun countReasonsForSupportDeclinedByPrisonIdAndDateTimeBetween(
    prisonId: String,
    startTime: Instant,
    endTime: Instant,
  ): List<MetricsCountByStringField>

  @Query(
    """
   WITH recent_updates AS (
      SELECT rev_number, offender_id, profile_data FROM work_readiness_audit wra
      WHERE wra.modified_date_time BETWEEN :startTime AND :endTime AND wra.rev_type IN (0,1)
    ), profile_at_start AS (
      SELECT wra1.rev_number, wra1.offender_id, wra1.profile_data FROM work_readiness_audit wra1
      INNER JOIN (
        SELECT offender_id, max(rev_number) as rev_number 
        FROM work_readiness_audit wra2
        WHERE wra2.offender_id IN (SELECT DISTINCT offender_id from recent_updates) AND wra2.modified_date_time <= :startTime AND wra2.rev_type IN (0,1)
        GROUP BY 1
      ) at_start 
      ON wra1.offender_id = at_start.offender_id AND wra1.rev_number = at_start.rev_number
      AND at_start.rev_number NOT IN (SELECT rev_number from recent_updates)
    ), profile_snapshots AS (
      SELECT * FROM recent_updates UNION ALL SELECT * FROM profile_at_start
    ), latest AS (
      SELECT offender_id, MAX(rev_number) AS rev_number, BOOL_OR(COALESCE((profile_data->>'within12Weeks')::boolean, true)) as within_12_weeks
      FROM profile_snapshots GROUP BY offender_id
    ), latest_profiles AS (
      SELECT p.offender_id, 
        p.profile_data->>'prisonId' AS prison_id,
        l.within_12_weeks,
        p.profile_data#>'{supportAccepted, actionsRequired, actions}' AS documents_support
      FROM profile_snapshots p INNER JOIN latest l
      ON L.offender_id = p.offender_id AND l.rev_number = p.rev_number
      WHERE p.profile_data->>'status' IN ('SUPPORT_NEEDED', 'READY_TO_WORK')
      AND p.profile_data->>'prisonId' = :prisonId
    ), documents_support AS (
      SELECT p.offender_id, p.within_12_weeks, ds.value->>'todoItem' AS document_support, ds.value->>'status' AS status FROM latest_profiles p, jsonb_array_elements(documents_support) AS ds
      WHERE ds.value->>'status' IN ('NOT_STARTED', 'IN_PROGRESS')
    )
    SELECT document_support as field,
      COUNT(CASE within_12_weeks WHEN TRUE THEN 1 ELSE NULL END) AS countWithin12Weeks,
      COUNT(CASE within_12_weeks WHEN FALSE THEN 1 ELSE NULL END) AS countOver12Weeks
    FROM documents_support 
    WHERE document_support IS NOT NULL 
    GROUP BY 1 ORDER BY 1;      
    """,
    nativeQuery = true,
  )
  fun countDocumentsSupportNeededByPrisonIdAndDateTimeBetween(
    prisonId: String,
    startTime: Instant,
    endTime: Instant,
  ): List<MetricsCountByStringField>
}
