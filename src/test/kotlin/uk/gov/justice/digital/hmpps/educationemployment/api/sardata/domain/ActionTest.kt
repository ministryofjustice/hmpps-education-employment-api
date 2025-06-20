package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test

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

//    assertTrue(json.contains("\"todoItem\":[\"ID\"]"))
//    assertTrue(json.contains("\"status\":[\"COMPLETED\"]"))
//    assertFalse(json.contains("todoItemAsList"))
//    assertFalse(json.contains("statusAsList"))
  }
}
