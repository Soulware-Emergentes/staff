-- The dev groups. Membership follows the job, the way a dynamic group fills
-- itself from a directory attribute, with exceptions added by hand. No job
-- title says who plans or analyses in poi, so those groups start empty.
INSERT INTO groups (id, name, description) VALUES
    ('a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e01', 'SGT-Field-Agents', 'Field agents who submit field sheets in sgt'),
    ('a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e02', 'SGT-GM-Assistants', 'General management assistants who review field sheets in sgt'),
    ('a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e03', 'SGT-Developers', 'Developers who work on sgt'),
    ('a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e04', 'POI-Planners', 'People who define the operational plan in poi'),
    ('a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e05', 'POI-Analysts', 'People who follow the plan''s progress in poi');

INSERT INTO group_members (group_id, person_id)
SELECT 'a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e01', id FROM people WHERE job_title = 'Field agent';

INSERT INTO group_members (group_id, person_id)
SELECT 'a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e02', id FROM people WHERE job_title = 'General management assistant';

INSERT INTO group_members (group_id, person_id) VALUES
    ('a1f0e6c2-3b7d-4c1e-8a92-0d4f5b6c7e03', '5d1e0c3a-7b52-4a8e-9f1d-2c6b8e4a9d01');
