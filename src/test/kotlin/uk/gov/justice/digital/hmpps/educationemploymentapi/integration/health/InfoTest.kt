package uk.gov.justice.digital.hmpps.educationemploymentapi.integration.health

import uk.gov.justice.digital.hmpps.educationemploymentapi.integration.IntegrationTestBase

class InfoTest : IntegrationTestBase() {

 /* @Test
  fun `Info page is accessible`() {
    webTestClient
      .get()
      .uri("/info")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("build.name")
      .isEqualTo("hmpps-education-employment-api")
  }

  @Test
  fun `Info page reports version`() {
    webTestClient
      .get()
      .uri("/info")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .consumeWith(System.out::println)
      .jsonPath("build.version").value<String> {
        assertThat(it).startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
      }
  }*/
}
