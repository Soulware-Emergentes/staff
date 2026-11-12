-- The identity provider: what Entra ID or Keycloak holds once it is in place.
-- Every deployment of an application is registered on its own, so dev and prod
-- are separate resources and clients with separate assignments.
--
-- Groups are named by the directory's objectGUIDs and carry no foreign key,
-- since the directory may live in another system.

-- An API tokens are issued for. Its identifier is the audience they carry.
CREATE TABLE api_resources (
    id UUID PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

-- The roles an API defines for people. Their meaning is the API's own.
CREATE TABLE api_roles (
    id UUID PRIMARY KEY,
    resource_id UUID NOT NULL REFERENCES api_resources (id) ON DELETE CASCADE,
    value VARCHAR(100) NOT NULL,
    description TEXT,
    UNIQUE (resource_id, value)
);

-- The scopes an API defines for services calling it on their own behalf.
CREATE TABLE api_scopes (
    id UUID PRIMARY KEY,
    resource_id UUID NOT NULL REFERENCES api_resources (id) ON DELETE CASCADE,
    value VARCHAR(100) NOT NULL,
    description TEXT,
    UNIQUE (resource_id, value)
);

-- A registered application. A public client logs people in with PKCE; a
-- confidential one holds a secret and gets tokens for itself.
CREATE TABLE client_registrations (
    id UUID PRIMARY KEY,
    client_id VARCHAR(255) NOT NULL UNIQUE,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('PUBLIC', 'CONFIDENTIAL')),
    secret_hash VARCHAR(255),
    CHECK ((kind = 'CONFIDENTIAL') = (secret_hash IS NOT NULL))
);

-- Where a login or a logout may send the browser back to.
CREATE TABLE client_redirect_uris (
    client_registration_id UUID NOT NULL REFERENCES client_registrations (id) ON DELETE CASCADE,
    uri VARCHAR(1000) NOT NULL,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('LOGIN', 'LOGOUT')),
    PRIMARY KEY (client_registration_id, uri, kind)
);

-- The APIs an application may get tokens for.
CREATE TABLE client_resources (
    client_registration_id UUID NOT NULL REFERENCES client_registrations (id) ON DELETE CASCADE,
    resource_id UUID NOT NULL REFERENCES api_resources (id) ON DELETE CASCADE,
    PRIMARY KEY (client_registration_id, resource_id)
);

-- A group's members hold a role on an API. A person may hold several roles,
-- and acts as one at a time.
CREATE TABLE role_assignments (
    role_id UUID NOT NULL REFERENCES api_roles (id) ON DELETE CASCADE,
    group_id UUID NOT NULL,
    PRIMARY KEY (role_id, group_id)
);

CREATE INDEX role_assignments_group ON role_assignments (group_id);

-- A confidential application holds a scope on an API.
CREATE TABLE scope_grants (
    client_registration_id UUID NOT NULL REFERENCES client_registrations (id) ON DELETE CASCADE,
    scope_id UUID NOT NULL REFERENCES api_scopes (id) ON DELETE CASCADE,
    PRIMARY KEY (client_registration_id, scope_id)
);
