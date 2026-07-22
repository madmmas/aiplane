package dev.madmmas.aimanager.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Shared Postgres Testcontainers fixture for {@code *IT} classes.
 *
 * <p>Subclasses get a real Flyway-migrated database via {@code @ServiceConnection}.
 */
@Testcontainers
public abstract class AbstractPostgresIntegrationTest {

  static {
    // docker-java defaults to API 1.24; Docker Engine 25+/Desktop rejects that with HTTP 400.
    System.setProperty("api.version", "1.44");
  }

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
}
