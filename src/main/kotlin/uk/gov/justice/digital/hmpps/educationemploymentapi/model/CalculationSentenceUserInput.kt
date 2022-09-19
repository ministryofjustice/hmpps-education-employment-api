package uk.gov.justice.digital.hmpps.educationemploymentapi.model

data class CalculationSentenceUserInput(
  val sentenceSequence: Int,
  val offenceCode: String,
  val userInputType: UserInputType,
  val userChoice: Boolean
)
