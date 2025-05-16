package uk.gov.justice.digital.hmpps.educationemployment.api.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version

  @Bean
  fun customOpenAPI(): OpenAPI? = OpenAPI()
    .tags(
      listOf(
        Tag().name("Popular"),
        Tag().name("v2").description("Profile APIs version 2"),
        Tag().name("v1").description("(Deprecated) Profile APIs version 1"),
        Tag().name("Notes").description("Profile Notes APIs"),
        Tag().name("SAR").description("Subject Access Request API"),
        Tag().name("Dashboard").description("Reporting Dashboard API"),
      ),
    )
    .info(
      Info()
        .title("HMPPS Education Employment API")
        .version(version)
        .description("API for employment education")
        .contact(
          Contact()
            .name("HMPPS Digital Studio")
            .email("feedback@digital.justice.gov.uk"),
        ),
    )
}
