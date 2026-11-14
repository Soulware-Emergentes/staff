-- People reach the dev beneficiary registry too, no longer only services: a
-- field agent registering a plot looks up its beneficiary by DNI from sgt's
-- frontend. The registry defines one role for that, and sgt's frontend may ask
-- for a token addressed to it, which it does silently right after a login.
-- Developers hold the role, as they hold every role, to act as a field agent.
INSERT INTO api_roles (id, resource_id, value, description) VALUES
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a07', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f04', 'field-agent', 'Looks up beneficiaries by document while registering plots');

INSERT INTO role_assignments (role_id, group_id) VALUES
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a07', 'a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e01'),
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a07', 'a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e03');

INSERT INTO client_resources (client_registration_id, resource_id) VALUES
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab01', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f04');
