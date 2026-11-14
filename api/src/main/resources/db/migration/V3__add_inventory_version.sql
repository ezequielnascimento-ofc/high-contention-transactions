-- Row version for optimistic concurrency control. Every write to an
-- inventory row increments it, whatever strategy performs the write.
ALTER TABLE inventory
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
