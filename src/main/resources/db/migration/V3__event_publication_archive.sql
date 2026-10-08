-- Where completed publications go under spring.modulith.events.completion-mode=archive:
-- the row is removed from event_publication and inserted here with its
-- completion_date set, so the live table holds the backlog and nothing else.
-- ArchivedJpaEventPublication maps the same @MappedSuperclass as the incomplete
-- entity, so the two tables carry identical columns.
CREATE TABLE IF NOT EXISTS event_publication_archive (
    id                     UUID NOT NULL,
    listener_id            TEXT NOT NULL,
    event_type             TEXT NOT NULL,
    serialized_event       TEXT NOT NULL,
    publication_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date        TIMESTAMP WITH TIME ZONE,
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    completion_attempts    INTEGER NOT NULL,
    status                 TEXT,
    PRIMARY KEY (id)
);

-- Purging by age is the only query that filters this table. The lookup by
-- payload runs against event_publication while the row is still there, so the
-- hash index on serialized_event is not repeated here.
CREATE INDEX IF NOT EXISTS event_publication_archive_by_completion_date_idx
    ON event_publication_archive (completion_date);