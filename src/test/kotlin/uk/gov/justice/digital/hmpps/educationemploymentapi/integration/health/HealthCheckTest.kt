package uk.gov.justice.digital.hmpps.educationemploymentapi.integration.health

import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.getForEntity
import uk.gov.justice.digital.hmpps.educationemploymentapi.integration.IntegrationTestBase

class HealthCheckTest : IntegrationTestBase() {

  @Test
  fun `Health page reports ok`() {
    val result = restTemplate.getForEntity("/health", String.javaClass)
    assert(result != null)
    assert(result.hasBody())
    assert(result.statusCode.is2xxSuccessful)
  }

  /* @Test  @Test
   fun `Health info reports version`() {
     webTestClient.get().uri("/health")
       .exchange()
       .expectStatus().isOk
       .expectBody()
       .jsonPath("components.healthInfo.details.version").value(
         Consumer<String> {
           assertThat(it).startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
         }
       )
   }

   @Test
   fun `Health ping page is accessible`() {
     webTestClient.get()
       .uri("/health/ping")
       .exchange()
       .expectStatus()
       .isOk
       .expectBody()
       .jsonPath("status").isEqualTo("UP")
   }

   @Test
   fun `readiness reports ok`() {
     webTestClient.get()
       .uri("/health/readiness")
       .exchange()
       .expectStatus()
       .isOk
       .expectBody()
       .jsonPath("status").isEqualTo("UP")
   }

   @Test
   fun `liveness reports ok`() {
     webTestClient.get()
       .uri("/health/liveness")
       .exchange()
       .expectStatus()
       .isOk
       .expectBody()
       .jsonPath("status").isEqualTo("UP")
   }*/
}
