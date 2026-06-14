ALTER TABLE accounts
    ADD COLUMN email_verified BOOLEAN DEFAULT FALSE;

UPDATE accounts
SET email_verified = FALSE
WHERE email_verified IS NULL;

ALTER TABLE accounts
    ADD CONSTRAINT EMAIL_VERIFIED_NOT_NULL CHECK (email_verified IS NOT NULL);