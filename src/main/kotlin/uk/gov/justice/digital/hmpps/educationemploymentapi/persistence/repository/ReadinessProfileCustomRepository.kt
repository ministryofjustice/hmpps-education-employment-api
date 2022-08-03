package uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.repository

import uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.model.ReadinessProfile


import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.flow
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.CriteriaDefinition
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.model.ReadinessProfileFilter

interface ReadinessProfileCustomRepository {
  fun findForGivenOffenders(filter: ReadinessProfileFilter): Flow<ReadinessProfile>
}

@Repository
class ReadinessProfileCustomRepositoryImpl(private val template: R2dbcEntityTemplate) : ReadinessProfileCustomRepository {
  override fun findForGivenOffenders(filter: ReadinessProfileFilter): Flow<ReadinessProfile> {
    return template.select(ReadinessProfile::class.java)
      .from("work_readiness")
      .matching(
        buildQuery(filter)
          .sort(Sort.by("modifiedDateTime").descending())
      )
      .flow()
  }

  private fun buildQuery(filter: ReadinessProfileFilter): Query =
    query(
      CriteriaDefinition.from(
        mutableListOf<CriteriaDefinition>().apply {
          this and filter.offenderIds?.let {
            where("offender_id").`in`(it)
          }
        }
      )
    )
}

private infix fun MutableList<CriteriaDefinition>.and(criteria: CriteriaDefinition?) {
  criteria?.run { add(this) }
}

