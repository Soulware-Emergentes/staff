-- The dev applications. Each frontend is a public client that logs people into
-- its own API. sgt's backend is a confidential client getting tokens for itself
-- from poi's and the directory's APIs; its secret is sgt-service-dev-secret,
-- stored as its BCrypt hash.
INSERT INTO client_registrations (id, client_id, application_id, environment, kind, secret_hash, home_uri) VALUES
    -- SGT, dev: the Angular app, and its backend
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab01', 'sgt-web-dev', 'a1d0e6c2-3b7d-4c1e-8a92-0d4b5c6e7f01', 'dev', 'PUBLIC', NULL, 'http://localhost:4200/'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab02', 'sgt-service-dev', 'a1d0e6c2-3b7d-4c1e-8a92-0d4b5c6e7f01', 'dev', 'CONFIDENTIAL', '$2a$10$zlyeSuj4kr2vX6nJrfTNZOuk9GGVnPD/sfqY/S4Ff9i.Y6ir6EOTi', NULL),
    -- POI, dev: the Angular app
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab03', 'poi-web-dev', 'a1d0e6c2-3b7d-4c1e-8a92-0d4b5c6e7f02', 'dev', 'PUBLIC', NULL, 'http://localhost:4201/');

-- Where staff may send the browser back to. LOGIN is the redirect_uri the frontend
-- receives the authorization code on after signing in; LOGOUT is the
-- post_logout_redirect_uri it lands on after signing out.
INSERT INTO client_redirect_uris (client_registration_id, uri, kind) VALUES
    -- sgt-web-dev: the sgt Angular app, as ng serve serves it
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab01', 'http://localhost:4200/callback', 'LOGIN'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab01', 'http://localhost:4200/', 'LOGOUT'),
    -- poi-web-dev: the poi Angular app, on the port next to sgt's
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab03', 'http://localhost:4201/callback', 'LOGIN'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab03', 'http://localhost:4201/', 'LOGOUT');

INSERT INTO client_resources (client_registration_id, resource_id) VALUES
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab01', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f01'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab02', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f02'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab02', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f03'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab03', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f02');

INSERT INTO scope_grants (client_registration_id, scope_id) VALUES
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab02', 'e5b4cafa-7fb0-4a5c-8ed6-4b8d9fab0c01'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab02', 'e5b4cafa-7fb0-4a5c-8ed6-4b8d9fab0c02'),
    ('d4a3b9f5-6eaf-4f4b-9dc5-3a7c8e9fab02', 'e5b4cafa-7fb0-4a5c-8ed6-4b8d9fab0c03');
