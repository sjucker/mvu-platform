package ch.mvurdorf.platform.testing;

import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static ch.mvurdorf.platform.jooq.Public.PUBLIC;

/**
 * Base class for integration tests that need a real database.
 * <p>
 * A single Postgres container is started once per JVM and shared by all subclasses (it is removed by Testcontainers
 * when the JVM exits). The Flyway migrations are applied once, and all tables are truncated before each test.
 * Services can be constructed directly with {@link #jooqDsl} and DAOs created from {@code jooqDsl.configuration()}.
 */
public abstract class AbstractIntegrationTest {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16.13");

    protected static final DSLContext jooqDsl;

    static {
        POSTGRES.start();
        Flyway.configure()
              .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
              .load()
              .migrate();
        jooqDsl = DSL.using(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    @BeforeEach
    void resetDatabase() {
        jooqDsl.execute(jooqDsl.truncate(PUBLIC.getTables())
                               .restartIdentity()
                               .cascade());
    }
}
