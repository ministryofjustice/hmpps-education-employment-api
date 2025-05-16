package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.dashboard

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.GetMetricsDocumentSupportResponse
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.GetMetricsReasonsSupportDeclinedResponse
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.GetMetricsWorkStatusResponse
import java.time.LocalDate

const val METRICS_REASONS_ENDPOINT = "$DASHBOARD_ENDPOINT/reasons-support-declined"
const val METRICS_DOCUMENTS_ENDPOINT = "$DASHBOARD_ENDPOINT/documents-support-needed"
const val METRICS_STATUS_ENDPOINT = "$DASHBOARD_ENDPOINT/work-status"

@Transactional(propagation = Propagation.NOT_SUPPORTED)
abstract class DashboardMetricsTestCase(
  val metricEndpoint: String,
) : DashboardTestCase() {

  protected fun assertGetMetricsIsOk(
    prisonId: String,
    dateFrom: LocalDate,
    dateTo: LocalDate,
    expectedResponse: String? = null,
  ) = assertGetMetricsIsOk(
    parameters = "prisonId=$prisonId&dateFrom=$dateFrom&dateTo=$dateTo",
    expectedResponse = expectedResponse,
  )

  protected fun assertGetMetricsIsOk(
    parameters: String,
    expectedResponse: String? = null,
  ) = assertGetMetricsIsExpected(parameters, HttpStatus.OK, expectedResponse)

  protected fun assertGetMetricsReturnsBadRequestError(
    parameters: String? = null,
    expectedResponse: String? = null,
  ) = assertGetMetricsIsExpected(parameters, HttpStatus.BAD_REQUEST, expectedResponse)

  protected fun expectedErrorMessageMissingParameter(paramName: String, paramType: String = "String") = expectedErrorMessage(
    errorMessage = "Required request parameter '$paramName' for method parameter type $paramType is not present",
    userMessagePrefix = "Missing required parameter",
  )

  protected fun expectedErrorMessageMissingDateParameter(paramName: String) = expectedErrorMessageMissingParameter(paramName, "LocalDate")

  protected fun expectedErrorMessageParameterTypeMismatch(paramName: String, paramValue: Any) = expectedErrorMessageValidationFailure(
    errorMessage = "Type mismatch: parameter '$paramName' with value '$paramValue'",
  )

  protected fun expectedErrorMessageInvalidDatePeriod(dateFrom: String, dateTo: String) = expectedErrorMessageValidationFailure(
    errorMessage = "dateFrom ($dateFrom) cannot be after dateTo ($dateTo)",
  )

  private fun expectedErrorMessageValidationFailure(errorMessage: String) = expectedErrorMessage(errorMessage, userMessagePrefix = "Validation failure")

  private fun expectedErrorMessage(errorMessage: String, userMessagePrefix: String? = null) = """
    {"status":400,"errorCode":null,"userMessage":"${userMessagePrefix?.let { "$it: " }}$errorMessage","developerMessage":"$errorMessage","moreInfo":null}
  """.trimIndent()

  private fun assertGetMetricsIsExpected(
    parameters: String? = null,
    expectedStatus: HttpStatus = HttpStatus.OK,
    expectedResponse: String? = null,
  ) {
    assertRequestWithoutBody(
      url = "$metricEndpoint${parameters?.let { "?$it" } ?: ""}",
      method = HttpMethod.GET,
      expectedStatus = expectedStatus,
      expectedResponse = expectedResponse,
    )
  }

  protected fun List<String>.joinToJsonString() = this.joinToString(separator = ",").let { "[$it]" }
  protected fun String.trimJsonResponse() = this.trimIndent().replace("\n|\\s".toRegex(), "")
}

abstract class MetricsReasonsSupportDeclinedTestCase : DashboardMetricsTestCase(METRICS_REASONS_ENDPOINT) {
  protected final val List<GetMetricsReasonsSupportDeclinedResponse>.metricsResponses: String get() = toMetricsResponses(this)

  private fun toMetricsResponses(expectedMetrics: List<GetMetricsReasonsSupportDeclinedResponse>) = expectedMetrics.map {
    """
      {
        "supportToWorkDeclinedReason":"${it.supportToWorkDeclinedReason}",
        "numberOfPrisonersWithin12Weeks":${it.numberOfPrisonersWithin12Weeks},
        "numberOfPrisonersOver12Weeks":${it.numberOfPrisonersOver12Weeks}
      }
    """.trimJsonResponse()
  }.joinToJsonString()
}

abstract class MetricsDocumentsSupportNeededTestCase : DashboardMetricsTestCase(METRICS_DOCUMENTS_ENDPOINT) {
  protected final val List<GetMetricsDocumentSupportResponse>.metricsResponses: String get() = toMetricsResponses(this)

  private fun toMetricsResponses(expectedMetrics: List<GetMetricsDocumentSupportResponse>) = expectedMetrics.map {
    """
      {
        "actionTodo":"${it.actionTodo}",
        "numberOfPrisonersWithin12Weeks":${it.numberOfPrisonersWithin12Weeks},
        "numberOfPrisonersOver12Weeks":${it.numberOfPrisonersOver12Weeks}
      }
    """.trimJsonResponse()
  }.joinToJsonString()
}

abstract class MetricsWorkStatusTestCase : DashboardMetricsTestCase(METRICS_STATUS_ENDPOINT) {
  protected final val GetMetricsWorkStatusResponse.metricsResponse: String get() = toMetricsResponses(this)

  private fun toMetricsResponses(expectedMetrics: GetMetricsWorkStatusResponse) = expectedMetrics.statusCounts.map {
    """
      {
        "profileStatus":"${it.profileStatus}",
        "numberOfPrisonersWithin12Weeks":${it.numberOfPrisonersWithin12Weeks},
        "numberOfPrisonersOver12Weeks":${it.numberOfPrisonersOver12Weeks}
      }
    """.trimJsonResponse()
  }.joinToJsonString().let { statusCounts ->
    """
      {
        "numberOfPrisonersStatusChange": ${expectedMetrics.numberOfPrisonersStatusChange},
        "statusCounts": $statusCounts
      }
    """.trimIndent().trimJsonResponse()
  }
}
