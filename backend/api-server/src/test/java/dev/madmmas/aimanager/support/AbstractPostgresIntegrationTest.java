package dev.madmmas.aimanager.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Shared Postgres Testcontainers fixture for {@code *IT} classes.
 *
 * <p>The container is started once in a static initializer (not {@code @Container}) so it stays up
 * across every {@code @SpringBootTest} class in the failsafe run. {@code @Container} on a shared
 * parent field stops the DB between classes and leaves later contexts with a dead JDBC URL.
 */
public abstract class AbstractPostgresIntegrationTest {

  static {
    // docker-java defaults to API 1.24; Docker Engine 25+/Desktop rejects that with HTTP 400.
    System.setProperty("api.version", "1.44");
  }

  @SuppressWarnings("resource")
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void registerDatasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }
}
