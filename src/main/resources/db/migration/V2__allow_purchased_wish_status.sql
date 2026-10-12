ALTER TABLE wish DROP CONSTRAINT chk_wish_status;

ALTER TABLE wish ADD CONSTRAINT chk_wish_status
    CHECK (status IN ('ACTIVE', 'PURCHASED'));
