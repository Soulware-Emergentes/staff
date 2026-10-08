-- Which group's members hold which role in dev. Developers hold every role in
-- sgt and poi, so they can act as anyone while working on either.
INSERT INTO role_assignments (role_id, group_id) VALUES
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a01', 'a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e01'),
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a02', 'a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e02'),
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a04', 'a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e04'),
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a05', 'a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e05'),
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a01', 'a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e03'),
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a02', 'a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e03'),
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a03', 'a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e03'),
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a04', 'a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e03'),
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a05', 'a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e03'),
    ('c3f2a8e4-5d9f-4e3a-8cb4-2f6b7d8e9a06', 'a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e03');
