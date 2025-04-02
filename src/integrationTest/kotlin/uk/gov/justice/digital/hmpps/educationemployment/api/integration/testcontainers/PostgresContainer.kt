package uk.gov.justice.digital.hmpps.educationemployment.api.integration.testcontainers

import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.IOException
import java.net.ServerSocket

object PostgresContainer {
  val flywayContainer: PostgreSQLContainer<Nothing>? by lazy { startPostgresqlContainer() }
  val repositoryContainer: PostgreSQLContainer<Nothing>? by lazy { startPostgresqlContainer() }

  private fun startPostgresqlContainer(): PostgreSQLContainer<Nothing>? {
    if (isPostgresRunning()) {
      log.warn("Using existing PostgreSQL database")
      return null
    }

    log.info("Creating a TestContainers PostgreSQL database")

    return PostgreSQLContainer<Nothing>("postgres:16").apply {
      withEnv("HOSTNAME_EXTERNAL", "localhost")
      withDatabaseName("education-employment")
      withUsername("education-employment")
      withPassword("education-employment")
      setWaitStrategy(Wait.forListeningPort())
      withReuse(true)
      start()
    }
  }

  private fun isPostgresRunning(): Boolean = try {
    val serverSocket = ServerSocket(5432)
    serverSocket.localPort == 0
  } catch (error: IOException) {
    log.warn("A PostgreSQL database is running")
    true
  }

  private val log = LoggerFactory.getLogger(this::class.java)
}
