# Staff

The organization's directory and the identity provider in front of it, standing in for Active Directory and Entra ID until those are in place. Services talk to it the way they would talk to the real pair, so moving to the real deployment changes configuration and the directory's adapters, and nothing that calls it.

Built as a modular monolith with Spring Boot, structured around domain-driven design. Spring Modulith enforces the module boundaries, Flyway owns the schema, and Postgres is the only supported database.

Java 25, Spring Boot 4.1, Spring Modulith 2.1.

## Modules

| Module | Stands in for | Holds |
|---|---|---|
| `directory` | Active Directory | People, their job title and department, groups and membership, the password check |
| `identityprovider` | Entra ID or Keycloak | API resources and their roles and scopes, client registrations, group-to-role assignments, token issuance |

A person's job and department say who they are and grant nothing. What a person may do in an application is a role that application defines, held through a group. The identity provider reaches the directory only through `DirectoryApi`: check a password, describe people, list a person's groups. Those three questions are what an LDAP adapter answers once the real directory is in place, through `CredentialProjection`, `PersonProjection` and `GroupMembershipProjection`.

## Endpoints

| Concern | Endpoint | Used by |
|---|---|---|
| Discovery | `GET /.well-known/openid-configuration` | Frontends, sgt, poi |
| Signing keys | `GET /oauth2/jwks` | Services validating tokens |
| Login | `GET /oauth2/authorize` (authorization code with PKCE), `/login` | Frontends |
| Tokens | `POST /oauth2/token` (authorization code, client credentials) | Frontends, services |
| Current user | `GET /userinfo` | Frontends |
| Logout | `GET /connect/logout` | Frontends |
| Directory | `GET /api/v3/people`, `POST /api/v3/people/lookup` | Services, with a token for the directory API |

## Tokens

Every deployment of an application is registered on its own, so `sgt-api-dev` and `sgt-api-prod` are two API resources with their own role assignments. A token names one API in `aud`, and that API accepts no other.

A frontend logs a person in with a scope naming the API and, optionally, the role to act as: `scope=openid profile sgt-api-dev act-as:general-management-assistant`. The login is refused with `access_denied` when the person's groups hold no role on that API, or not the role asked for. Asking for no role works when the person holds exactly one; otherwise the token carries no active role and the frontend asks again with the one picked. Asking again while the login session lasts needs no password, which is how a person switches roles.

| Token | Issued to | Carries |
|---|---|---|
| Person's access token | A frontend, for one API | `sub` (objectGUID), `aud`, `azp`, `token_use: user`, `roles` (the active one), `eligible_roles` |
| Service access token | A confidential client, for one API | `sub` (client id), `aud`, `azp`, `token_use: service`, `scope` (what it was granted there) |
| ID token | A frontend | `sub`, `preferred_username`, `name`, `job_title`, `department` |

Userinfo answers with the ID token's claims. Public clients get no refresh token, so a frontend logs in again when its access token expires. Login sessions and issued authorizations live in memory, so a restart logs everyone out; tokens issued before it still validate, since the signing key is read from files.

A directory lookup resolves at most 100 GUIDs per request, whatever the people's jobs. GUIDs nobody holds come back under `notFound`.

## Seed data

The schema is the same everywhere. What an environment starts with comes from `db/seed/<STAFF_ENVIRONMENT>`; production has no seed folder, and its registrations are entered as data. The dev seed holds:

- 30 field agents, 10 general management assistants and one developer, `devuser`. All of them log in with `staff-dev-password`.
- Groups filled from the job title, the way a dynamic group fills from a directory attribute, plus `SGT-Developers`, which holds every sgt and poi role in dev, and `POI-Planners` and `POI-Analysts`, which start empty.
- `sgt-api-dev` with the roles `field-agent`, `general-management-assistant` and `admin`, and the frontend `sgt-web-dev` registered to log people into it.
- `poi-api-dev`, `staff-directory-api-dev` and `beneficiarios-api-dev` with their scopes, and `sgt-service-dev`, sgt's backend, granted them. Its secret is `sgt-service-dev-secret`.
- `poi-api-dev`'s roles `planner`, `analyst` and `admin`, and the frontend `poi-web-dev` registered to log people into it.

## Moving to the real deployment

The directory's three projection adapters are replaced by LDAP ones: a bind for the password check, and searches for people and group membership. Nothing above them changes.

The identity provider is replaced by configuration. Services point their issuer at the real one, their audiences at the identifiers their APIs are registered under there, and their claim names at whatever it calls them. The registrations, roles and assignments in the seed files are recreated in the real provider.

## Requirements

Docker has to be available for `./mvnw verify`. That half of the suite starts a throwaway Postgres container, so nothing needs to be installed or running beforehand, and it never touches a local database.

Running the application needs a local Postgres instance you provision yourself.

## Setting up a local database

The application uses two Postgres roles rather than one shared credential:

| Role | Privileges | Used by |
|---|---|---|
| `staff_migrator` | Owns the schema, full DDL | Flyway, at startup |
| `staff_app` | `SELECT`, `INSERT`, `UPDATE`, `DELETE` | The application, at runtime |

The runtime credential cannot create, alter, or drop tables. A leaked credential or an injection bug can tamper with data; it cannot destroy the schema.

[`scripts/db/setup-roles.sql`](scripts/db/setup-roles.sql) creates both roles and the database. It reads the passwords from the environment so they stay out of your shell history. Run it with:

```sh
export STAFF_MIGRATOR_PASSWORD='...'
export STAFF_APP_PASSWORD='...'
sudo -iu postgres psql -f scripts/db/setup-roles.sql
```

Then copy [`.env.example`](.env.example) to `.env` and fill in the same two passwords. `.env` is gitignored. Create the signing key pair with the `openssl` commands in `.env.example`; `keys/` is gitignored too.

```sh
cp .env.example .env
```

The database properties in `application.properties` have no fallback defaults. A missing `DB_URL` or `FLYWAY_URL` fails at startup rather than silently connecting somewhere unintended.

## Running

In VS Code, run the **Staff (Spring Boot)** configuration. It automatically reads `.env` through the `envFile` setting in [`.vscode/launch.json`](.vscode/launch.json).

From a shell, export the variables yourself first, since nothing loads `.env` implicitly:

```sh
set -a; source .env; set +a
./mvnw spring-boot:run
```

Both paths end up with the same variables resolved.

Flyway applies any pending migrations from `src/main/resources/db/migration` at startup, connecting as `staff_migrator`. Hibernate is set to `validate`, so the schema belongs to the migrations and entities are checked against it.

## Tests

In VS Code, use the Testing view or the gutter icons next to each test. From a shell:

```sh
./mvnw verify   # everything
./mvnw test     # only the tests that need no database
```

Classes named `*IT` need Docker and run under failsafe, in `verify`. Classes named `*Test` run under surefire, in `test`, and never touch a container.

The suite starts one Postgres container, runs `setup-roles.sql` inside it to create the roles, and shares that container across every test class. Flyway migrates the fresh database the first time a test boots the application context. The first run pulls the Postgres image.

Because the tests execute the real `setup-roles.sql`, breaking the privilege model there turns the build red.

[`DatabaseRoleSeparationIT`](src/test/java/lat/soulware/staff/platform/DatabaseRoleSeparationIT.java) connects with the application's own datasource and asserts that `staff_app` can write rows and cannot change the schema.

If you rename the roles or the database in `setup-roles.sql`, update the matching constants in [`PostgresIntegrationTest`](src/test/java/lat/soulware/staff/support/PostgresIntegrationTest.java) and the values in `.env.example`.

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs `test` on every push to any branch, and `verify` on pull requests and on pushes to `main` and `develop`.

For further explanation on tests, check [the testing note](docs/testing.md).

## Layout

```
staff/
├── .github/workflows/      CI
├── deploy/examples/        reference configs for Nginx and PgBouncer
├── docs/                   design notes
├── scripts/db/             database provisioning
└── src/
    ├── main/
    │   ├── java/           application code, one package per module
    │   └── resources/      Flyway migrations and message bundles
    └── test/java/
        ├── architecture/   module boundaries and the shape of the code itself
        ├── i18n/           message bundles against the exception hierarchy
        ├── platform/       tests of the schema, migrations, and database roles
        ├── shared/         the kernel's own units, mirroring src/main
        └── support/        shared test fixtures
```

## Further reading

[docs/ddd.md](docs/ddd.md) covers the two distinctions the model rests on (value objects against entities, entities against aggregate roots), why a repository serves only a root, how references cross boundaries, why cross-aggregate consistency is eventual, what a module publishes as its language and what publishing commits it to, and why reads answer a different question from writes.

[docs/packaging.md](docs/packaging.md) covers the package layout of a module, the criteria that decide where a type goes, what the published surface is, and the rules the build enforces to keep it that way.

[docs/shared-kernel.md](docs/shared-kernel.md) covers the base types every module builds on: the identity and model hierarchy, what each generic bound rejects, the persistence adapter base, and how to choose between the domain exceptions.

[docs/persistence.md](docs/persistence.md) covers the two-role privilege split and why the app never holds DDL rights, the separate paths reads and writes take, what opting into soft deletion commits you to, and what keeps a reference across an aggregate boundary truthful.

[docs/logging.md](docs/logging.md) covers the identifier every request is assigned, the three places it is published, and how a log line, an access log record, and a 5xx body are joined by it.

[docs/scaling.md](docs/scaling.md) covers when to introduce a load balancer and a connection pooler, and the prepared-statement tradeoff that comes with PgBouncer's transaction pooling.

[docs/testing.md](docs/testing.md) covers the test harness, what the platform tests assert, where tests live, how the build splits them by cost, and how CI runs them.
