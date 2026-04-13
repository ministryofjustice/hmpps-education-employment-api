package uk.gov.justice.digital.hmpps.educationemployment.api.config

import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.tags.Tag
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.educationemployment.api.sar.application.SARContentDTO
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version!!

  @Bean
  fun customOpenAPI(): OpenAPI? = OpenAPI()
    .tags(
      listOf(
        Tag().name("Popular"),
        Tag().name("v2").description("Profile APIs version 2"),
        Tag().name("v1").description("(Deprecated) Profile APIs version 1"),
        Tag().name("Notes").description("Profile Notes APIs"),
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
    .components(
      Components()
        .addSecuritySchemes("view-work-readiness-role", SecurityScheme().addBearerJwtRequirement("WORK_READINESS_VIEW"))
        .addSecuritySchemes("edit-work-readiness-role", SecurityScheme().addBearerJwtRequirement("WORK_READINESS_EDIT"))
        .addSecuritySchemes("view-jobs-board-role", SecurityScheme().addBearerJwtRequirementSystemRole("ROLE_EDUCATION_WORK_PLAN_VIEW"))
        .addSecuritySchemes("edit-jobs-board-role", SecurityScheme().addBearerJwtRequirementSystemRole("ROLE_EDUCATION_WORK_PLAN_EDIT"))
        .addSecuritySchemes("sar-role", SecurityScheme().addBearerJwtRequirementSystemRole("ROLE_SAR_DATA_ACCESS")),
    )
    .addSecurityItem(
      SecurityRequirement()
        .addList("view-work-readiness-role", listOf("read"))
        .addList("edit-work-readiness-role", listOf("read", "write"))
        .addList("view-jobs-board-role", listOf("read"))
        .addList("edit-jobs-board-role", listOf("read", "write"))
        .addList("sar-role", listOf("read")),
    )

  @Bean
  fun openAPICustomiser(): OpenApiCustomizer = OpenApiCustomizer {
    typedContentForSar(it)
  }

  private fun typedContentForSar(openApi: OpenAPI) {
    // register the SAR Content DTO
    val resolvedSchema = ModelConverters.getInstance().readAllAsResolvedSchema(SARContentDTO::class.java).also {
      openApi.components.addSchemas(it.schema.name, it.schema)
      it.referencedSchemas.forEach { openApi.components.addSchemas(it.key, it.value) }
    }
    // Touch up the SAR schema
    openApi.components.schemas[HmppsSubjectAccessRequestContent::class.simpleName]?.let { sarSchema ->
      sarSchema.properties["content"] = ArraySchema().items(resolvedSchema.schema)
      sarSchema.properties["attachments"]?.let {
        it.description = "(Not in use) ${it.description}"
        it.example = null
      }
    }
    // Add security requirements to SAR endpoints
    listOf(
      "/subject-access-request",
      "/subject-access-request/template",
    ).forEach { openApi.paths[it]!!.get!!.security = listOf(SecurityRequirement().addList("sar-role", listOf("read"))) }
  }

  private fun SecurityScheme.addBearerJwtRequirement(role: String) = addBearerJwtRequirementWithRoleDescription("`$role` user role")
  private fun SecurityScheme.addBearerJwtRequirementSystemRole(role: String) = addBearerJwtRequirementWithRoleDescription("`$role` system role")

  private fun SecurityScheme.addBearerJwtRequirementWithRoleDescription(roleDescription: String): SecurityScheme = type(SecurityScheme.Type.HTTP)
    .scheme("bearer")
    .bearerFormat("JWT")
    .`in`(SecurityScheme.In.HEADER)
    .name("Authorization")
    .description("A HMPPS Auth access token with the $roleDescription.")
}
