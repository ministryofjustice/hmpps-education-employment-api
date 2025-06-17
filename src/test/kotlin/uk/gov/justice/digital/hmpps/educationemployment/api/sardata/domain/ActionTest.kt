package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.IDocs

class ActionTest {

  private val objectMapper: ObjectMapper = jacksonObjectMapper()

  @Test
  fun `should serialize todoItem and status as arrays via mixin`() {
    val action = Action(
      todoItem = ActionTodo.ID,
      status = ActionStatus.COMPLETED,
      other = "notes",
      id = listOf(IDocs.PASSPORT),
    )

    val json = objectMapper.writeValueAsString(action)
    println("Serialized JSON: $json")

    assertTrue(json.contains("\"todoItem\":[\"ID\"]"))
    assertTrue(json.contains("\"status\":[\"COMPLETED\"]"))
    assertFalse(json.contains("todoItemAsList"))
    assertFalse(json.contains("statusAsList"))
  }
}
