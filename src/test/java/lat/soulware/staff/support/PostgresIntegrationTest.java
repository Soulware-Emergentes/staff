package lat.soulware.staff.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

/**
 * Base class for tests that need a real Postgres with this project's two roles.
 *
 * <p>One container is started for the whole test suite and torn down by Ryuk when the JVM exits.
 * It runs {@code scripts/db/setup-roles.sql} verbatim, so the script that provisions a real
 * deployment is the same one every test runs against - if it breaks, the build breaks.
 *
 * <p>The datasource properties in {@code application.properties} have no defaults on purpose:
 * a missing {@code DB_URL} in production must fail loudly. Tests therefore supply real values
 * for a real database rather than weakening that config.
 */
public abstract class PostgresIntegrationTest {

    // Thse have to match the setup-roles.sql script
    protected static final String DATABASE_NAME = "staff";
    protected static final String MIGRATOR_USERNAME = "staff_migrator";
    protected static final String APP_USERNAME = "staff_app";

    // WARNING: DO NOT let these match your actual .env passwords
    private static final String MIGRATOR_PASSWORD = "MIGRATOR_RANDOM_PASSWORD";
    private static final String APP_PASSWORD = "APP_RANDOM_PASSWORD";

    private static final Path SETUP_ROLES_SCRIPT = Path.of("scripts", "db", "setup-roles.sql");
    private static final String SCRIPT_PATH_IN_CONTAINER = "/tmp/setup-roles.sql";

    // The identity provider reads these. Every seeded person logs in with DEV_PASSWORD, and the
    // seeded service client authenticates with SERVICE_CLIENT_SECRET.
    protected static final String ISSUER = "http://staff.test";
    protected static final String REDIRECT_URI = "http://localhost:5173/callback";
    protected static final String DEV_PASSWORD = "staff-dev-password";
    protected static final String SERVICE_CLIENT_ID = "sgt-service-dev";
    protected static final String SERVICE_CLIENT_SECRET = "sgt-service-dev-secret";

    private static final PostgreSQLContainer POSTGRES = startProvisionedContainer();
    private static final List<Path> SIGNING_KEYS = writeSigningKeys();

    /**
     * Starts the container and provisions its roles before any subclass runs. Assigned to a static
     * field so one container is shared across every integration test rather than started per class.
     */
    private static PostgreSQLContainer startProvisionedContainer() {
        MountableFile setupScript = MountableFile.forHostPath(SETUP_ROLES_SCRIPT.toAbsolutePath());

        PostgreSQLContainer container = new PostgreSQLContainer("postgres:17-alpine");
        container.withCopyFileToContainer(setupScript, SCRIPT_PATH_IN_CONTAINER);

        container.start();
        createRoles(container);

        return container;
    }

    /**
     * Runs setup-roles.sql through psql inside the container. It has to be psql rather than JDBC
     * because the script uses meta-commands ({@code \set}, {@code \connect}) that only psql
     * understands, and {@code CREATE DATABASE} cannot run inside a transaction block.
     */
    private static void createRoles(PostgreSQLContainer container) {
        String psqlCommand = """
            STAFF_MIGRATOR_PASSWORD='%s' STAFF_APP_PASSWORD='%s' \
            psql -v ON_ERROR_STOP=1 -U %s -d %s -f %s"""
            .formatted(
                MIGRATOR_PASSWORD,
                APP_PASSWORD,
                container.getUsername(),
                container.getDatabaseName(),
                SCRIPT_PATH_IN_CONTAINER
            );

        try {
            Container.ExecResult result = container.execInContainer("sh", "-c", psqlCommand);
            boolean scriptFailed = result.getExitCode() != 0;

            if (scriptFailed) {
                throw new IllegalStateException(
                    "setup-roles.sql failed with exit code %d:%n%s"
                        .formatted(result.getExitCode(), result.getStderr())
                );
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not run setup-roles.sql in the container", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while running setup-roles.sql", e);
        }
    }

    /**
     * Writes a fresh RSA key pair to temporary PEM files, the way a deployment provides the pair
     * tokens are signed with.
     *
     * @return the private and the public key's PEM files, in that order
     */
    private static List<Path> writeSigningKeys() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();

            Path privateKey = Files.createTempFile("signing-private", ".pem");
            Path publicKey = Files.createTempFile("signing-public", ".pem");
            Files.writeString(privateKey, pem("PRIVATE KEY", pair.getPrivate().getEncoded()));
            Files.writeString(publicKey, pem("PUBLIC KEY", pair.getPublic().getEncoded()));
            return List.of(privateKey, publicKey);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No RSA key pair generator", e);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write the signing key pair", e);
        }
    }

    private static String pem(String type, byte[] der) {
        String body = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(der);
        return "-----BEGIN %s-----%n%s%n-----END %s-----%n".formatted(type, body, type);
    }

    /** JDBC URL of the provisioned {@code staff} database, whichever role connects to it. */
    protected static String jdbcUrl() {
        Integer mappedPort = POSTGRES.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT);

        return "jdbc:postgresql://%s:%d/%s".formatted(POSTGRES.getHost(), mappedPort, DATABASE_NAME);
    }

    @DynamicPropertySource
    /**
     * Points Spring at the container after it has been assigned its port. The two roles are handed
     * out separately: Flyway migrates as the migrator, the application connects as the app role,
     * which is what makes the privilege separation testable at all.
     */
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.url", PostgresIntegrationTest::jdbcUrl);
        registry.add("spring.flyway.user", () -> MIGRATOR_USERNAME);
        registry.add("spring.flyway.password", () -> MIGRATOR_PASSWORD);

        registry.add("spring.datasource.url", PostgresIntegrationTest::jdbcUrl);
        registry.add("spring.datasource.username", () -> APP_USERNAME);
        registry.add("spring.datasource.password", () -> APP_PASSWORD);
        registry.add("API_DOCS_ENABLED", () -> "true");

        registry.add("SERVER_PORT", () -> "0");
        registry.add("staff.directory.audience", () -> "staff-directory-api-dev");
        registry.add("staff.oidc.issuer", () -> ISSUER);
        registry.add("STAFF_ENVIRONMENT", () -> "dev");
        registry.add("staff.oidc.web.origins", () -> "http://localhost:5173,http://localhost:5174");
        registry.add("staff.oidc.signing.private-key-path", () -> SIGNING_KEYS.get(0).toString());
        registry.add("staff.oidc.signing.public-key-path", () -> SIGNING_KEYS.get(1).toString());
    }
}
