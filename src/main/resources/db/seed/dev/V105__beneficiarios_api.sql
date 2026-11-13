-- The dev beneficiary registry, an API only the program's own services call:
-- it defines no roles for people, and one scope for reading it. sgt's backend
-- holds that scope, to resolve the beneficiaries it is handed.
INSERT INTO api_resources (id, identifier, application_id, environment) VALUES
    ('b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f04', 'beneficiarios-api-dev', 'a1d0e6c2-3b7d-4c1e-8a92-0d4b5c6e7f04', 'dev');

INSERT INTO api_scopes (id, resource_id, value, description) VALUES
    ('e5b4cafa-7fb0-4a5c-8ed6-4b8d9fab0c04', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f04', 'beneficiaries.read', 'Reads beneficiaries from the registry');

INSERT INTO client_resources (client_registration_id, resource_id) VALUES
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab02', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f04');

INSERT INTO scope_grants (client_registration_id, scope_id) VALUES
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab02', 'e5b4cafa-7fb0-4a5c-8ed6-4b8d9fab0c04');
