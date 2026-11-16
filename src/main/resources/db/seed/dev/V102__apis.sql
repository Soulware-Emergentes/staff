-- The dev APIs tokens are issued for, the roles each defines for people, and
-- the scopes each defines for services calling it on their own behalf.
-- The systems, whatever their environment.
INSERT INTO applications (id, name) VALUES
    ('a1d0e6c2-3b7d-4c1e-8a92-0d4b5c6e7f01', 'SGT'),
    ('a1d0e6c2-3b7d-4c1e-8a92-0d4b5c6e7f02', 'POI'),
    ('a1d0e6c2-3b7d-4c1e-8a92-0d4b5c6e7f03', 'Staff directory'),
    ('a1d0e6c2-3b7d-4c1e-8a92-0d4b5c6e7f04', 'Beneficiarios');

INSERT INTO api_resources (id, identifier, application_id, environment) VALUES
    ('b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f01', 'sgt-api-dev', 'a1d0e6c2-3b7d-4c1e-8a92-0d4b5c6e7f01', 'dev'),
    ('b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f02', 'poi-api-dev', 'a1d0e6c2-3b7d-4c1e-8a92-0d4b5c6e7f02', 'dev'),
    ('b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f03', 'staff-directory-api-dev', 'a1d0e6c2-3b7d-4c1e-8a92-0d4b5c6e7f03', 'dev');

INSERT INTO api_roles (id, resource_id, value, description) VALUES
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a01', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f01', 'field-agent', 'Submits field sheets'),
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a02', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f01', 'general-management-assistant', 'Reviews field sheets'),
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a03', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f01', 'admin', 'Does anything in sgt'),
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a04', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f02', 'planner', 'Defines plans, objectives, activities, and tasks'),
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a05', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f02', 'analyst', 'Reads the plan''s progress'),
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a06', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f02', 'admin', 'Does anything in poi');

INSERT INTO api_scopes (id, resource_id, value, description) VALUES
    ('e5b4cafa-7fb0-4a5c-8ed6-4b8d9fab0c01', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f02', 'tasks.read', 'Reads tasks'),
    ('e5b4cafa-7fb0-4a5c-8ed6-4b8d9fab0c02', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f02', 'tasks.write', 'Registers, completes, reopens and relinks task deliveries'),
    ('e5b4cafa-7fb0-4a5c-8ed6-4b8d9fab0c03', 'b2e1f7d3-4c8e-4d2f-9ba3-1e5a6c7d8f03', 'people.read', 'Reads people from the directory');
