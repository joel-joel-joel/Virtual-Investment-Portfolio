-- V3__Add_Spring_Modulith_Tables.sql
-- Add Spring Modulith event publication table

CREATE TABLE event_publication (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listener_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    serialized_event TEXT NOT NULL,
    publication_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completion_date TIMESTAMP
);

CREATE INDEX idx_event_publication_completion_date ON event_publication(completion_date);
CREATE INDEX idx_event_publication_publication_date ON event_publication(publication_date);
CREATE INDEX idx_event_publication_listener_id ON event_publication(listener_id);
