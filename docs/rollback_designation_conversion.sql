-- =======================================================================
-- ROLLBACK SCRIPT: LEGACY USER DESIGNATIONS
-- Source of Truth: identity.user_designation_backup
-- Do NOT run unless rolling back designation codes to original text.
-- =======================================================================

-- -----------------------------------------------------------------------
-- TENANT 1: los_abc25_db (ABC25)
-- Run inside database: los_abc25_db
-- -----------------------------------------------------------------------
BEGIN;

UPDATE identity.users 
SET designation = 'Compliance Officer' 
WHERE id = 1 AND username = 'sohan';

UPDATE identity.users 
SET designation = 'Relationship Manager' 
WHERE id = 3 AND username = 'sohanverma50';

UPDATE identity.users 
SET designation = 'Relationship Manager' 
WHERE id = 4 AND username = 'sohanvermatechence12';

UPDATE identity.users 
SET designation = 'Relationship Manager' 
WHERE id = 6 AND username = 'akash1';

UPDATE identity.users 
SET designation = 'Compliance Officer' 
WHERE id = 8 AND username = 'sakshi3271';

COMMIT;


-- -----------------------------------------------------------------------
-- TENANT 2: los_boi123_db (BOI123)
-- Run inside database: los_boi123_db
-- -----------------------------------------------------------------------
BEGIN;

UPDATE identity.users 
SET designation = 'Branch Manager' 
WHERE id = 1 AND username = 'sakshi3270';

UPDATE identity.users 
SET designation = 'Relationship Manager' 
WHERE id = 2 AND username = 'akash';

UPDATE identity.users 
SET designation = 'Relationship Manager' 
WHERE id = 3 AND username = 'mahesh';

UPDATE identity.users 
SET designation = 'Relationship Manager' 
WHERE id = 4 AND username = 'pranav';

UPDATE identity.users 
SET designation = 'Relationship Manager' 
WHERE id = 5 AND username = 'richard.fernandes';

UPDATE identity.users 
SET designation = 'Relationship Manager' 
WHERE id = 6 AND username = 'archana25';

COMMIT;
