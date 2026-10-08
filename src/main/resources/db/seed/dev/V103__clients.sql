-- The dev applications. Each frontend is a public client that logs people into
-- its own API. sgt's backend is a confidential client getting tokens for itself
-- from poi's and the directory's APIs; its secret is sgt-service-dev-secret,
-- stored as its BCrypt hash.
INSERT INTO client_registrations (id, client_id, kind, secret_hash) VALUES
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab01', 'sgt-web-dev', 'PUBLIC', NULL),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab02', 'sgt-service-dev', 'CONFIDENTIAL', '$2a$10$zlyeSuj4kr2vX6nJrfTNZOuk9GGVnPD/sfqY/S4Ff9i.Y6ir6EOTi'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab03', 'poi-web-dev', 'PUBLIC', NULL);

INSERT INTO client_redirect_uris (client_registration_id, uri, kind) VALUES
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab01', 'http://localhost:5173/callback', 'LOGIN'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab01', 'http://localhost:5173/', 'LOGOUT'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab03', 'http://localhost:5174/callback', 'LOGIN'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab03', 'http://localhost:5174/', 'LOGOUT');

INSERT INTO client_resources (client_registration_id, resource_id) VALUES
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab01', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f01'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab02', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f02'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab02', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f03'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab03', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f02');

INSERT INTO scope_grants (client_registration_id, scope_id) VALUES
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab02', 'e5b4cafa-7fb0-4a5c-8ed6-4b8d9fab0c01'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab02', 'e5b4cafa-7fb0-4a5c-8ed6-4b8d9fab0c02'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab02', 'e5b4cafa-7fb0-4a5c-8ed6-4b8d9fab0c03');
