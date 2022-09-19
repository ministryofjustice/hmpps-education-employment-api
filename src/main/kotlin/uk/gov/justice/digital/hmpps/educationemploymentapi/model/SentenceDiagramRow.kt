package uk.gov.justice.digital.hmpps.educationemploymentapi.model

data class SentenceDiagramRow(
  val description: String,
  val sections: List<SentenceDiagramRowSection>
)
