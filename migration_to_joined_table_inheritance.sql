-- Migration Script: Convert to Joined Table Inheritance
-- This script changes the database schema to use Joined Table Inheritance
-- where child tables (admin_rs, dokter, pasien) share the same primary key (id_user) with the parent table (users)

-- WARNING: This script will modify the database structure. 
-- Make sure to backup your database before running this script.

BEGIN;

-- Step 1: Disable foreign key constraints temporarily
SET CONSTRAINTS ALL DEFERRED;

-- Step 2: Update appointment table to use id_user instead of id_pasien
-- First, add a temporary column to store the id_user
ALTER TABLE appointment ADD COLUMN temp_id_user VARCHAR(32);

-- Update the temporary column with the corresponding id_user from pasien table
UPDATE appointment a
SET temp_id_user = p.id_user
FROM pasien p
WHERE a.id_pasien = p.id_pasien;

-- Drop the foreign key constraint to pasien
ALTER TABLE appointment DROP CONSTRAINT appointment_id_pasien_fkey;

-- Drop the id_pasien column
ALTER TABLE appointment DROP COLUMN id_pasien;

-- Rename the temporary column to id_user
ALTER TABLE appointment RENAME COLUMN temp_id_user TO id_user;

-- Add foreign key constraint to users table
ALTER TABLE appointment 
ADD CONSTRAINT appointment_id_user_fkey 
FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

-- Step 3: Update jadwal_praktik table to use id_user instead of id_dokter
-- First, add a temporary column to store the id_user
ALTER TABLE jadwal_praktik ADD COLUMN temp_id_user VARCHAR(32);

-- Update the temporary column with the corresponding id_user from dokter table
UPDATE jadwal_praktik j
SET temp_id_user = d.id_user
FROM dokter d
WHERE j.id_dokter = d.id_dokter;

-- Drop the foreign key constraint to dokter
ALTER TABLE jadwal_praktik DROP CONSTRAINT jadwal_praktik_id_dokter_fkey;

-- Drop the id_dokter column
ALTER TABLE jadwal_praktik DROP COLUMN id_dokter;

-- Rename the temporary column to id_user
ALTER TABLE jadwal_praktik RENAME COLUMN temp_id_user TO id_user;

-- Add foreign key constraint to users table
ALTER TABLE jadwal_praktik 
ADD CONSTRAINT jadwal_praktik_id_user_fkey 
FOREIGN KEY (id_user) REFERENCES dokter(id_user) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

-- Step 4: Convert admin_rs table to Joined Table Inheritance
-- Drop the primary key constraint
ALTER TABLE admin_rs DROP CONSTRAINT admin_rs_pkey;

-- Drop the unique constraint on id_user
ALTER TABLE admin_rs DROP CONSTRAINT admin_rs_id_user_key;

-- Drop the foreign key constraint to users
ALTER TABLE admin_rs DROP CONSTRAINT admin_rs_id_user_fkey;

-- Drop the id_admin column
ALTER TABLE admin_rs DROP COLUMN id_admin;

-- Add primary key constraint on id_user
ALTER TABLE admin_rs ADD CONSTRAINT admin_rs_pkey PRIMARY KEY (id_user);

-- Step 5: Convert dokter table to Joined Table Inheritance
-- Drop the primary key constraint
ALTER TABLE dokter DROP CONSTRAINT dokter_pkey;

-- Drop the unique constraint on id_user
ALTER TABLE dokter DROP CONSTRAINT dokter_id_user_key;

-- Drop the foreign key constraint to users
ALTER TABLE dokter DROP CONSTRAINT dokter_id_user_fkey;

-- Drop the id_dokter column
ALTER TABLE dokter DROP COLUMN id_dokter;

-- Add primary key constraint on id_user
ALTER TABLE dokter ADD CONSTRAINT dokter_pkey PRIMARY KEY (id_user);

-- Step 6: Convert pasien table to Joined Table Inheritance
-- Drop the primary key constraint
ALTER TABLE pasien DROP CONSTRAINT pasien_pkey;

-- Drop the unique constraint on id_user
ALTER TABLE pasien DROP CONSTRAINT pasien_id_user_key;

-- Drop the foreign key constraint to users
ALTER TABLE pasien DROP CONSTRAINT pasien_id_user_fkey;

-- Drop the id_pasien column
ALTER TABLE pasien DROP COLUMN id_pasien;

-- Add primary key constraint on id_user
ALTER TABLE pasien ADD CONSTRAINT pasien_pkey PRIMARY KEY (id_user);

-- Step 7: Update any other tables that might reference the old IDs
-- Check if there are any other tables that need to be updated
-- (Add additional steps here if needed)

COMMIT;

-- Verification queries (run these after the migration to verify the changes)
-- SELECT * FROM admin_rs;
-- SELECT * FROM dokter;
-- SELECT * FROM pasien;
-- SELECT * FROM appointment;
-- SELECT * FROM jadwal_praktik;

-- Rollback script (in case you need to revert the changes)
-- Uncomment and run this section if you need to rollback
/*
BEGIN;

-- Rollback appointment table changes
ALTER TABLE appointment DROP CONSTRAINT appointment_id_user_fkey;
ALTER TABLE appointment ADD COLUMN id_pasien VARCHAR(10);
ALTER TABLE appointment DROP COLUMN id_user;
ALTER TABLE appointment 
ADD CONSTRAINT appointment_id_pasien_fkey 
FOREIGN KEY (id_pasien) REFERENCES pasien(id_pasien) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

-- Rollback jadwal_praktik table changes
ALTER TABLE jadwal_praktik DROP CONSTRAINT jadwal_praktik_id_user_fkey;
ALTER TABLE jadwal_praktik ADD COLUMN id_dokter VARCHAR(10);
ALTER TABLE jadwal_praktik DROP COLUMN id_user;
ALTER TABLE jadwal_praktik 
ADD CONSTRAINT jadwal_praktik_id_dokter_fkey 
FOREIGN KEY (id_dokter) REFERENCES dokter(id_dokter) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

-- Rollback admin_rs table changes
ALTER TABLE admin_rs DROP CONSTRAINT admin_rs_pkey;
ALTER TABLE admin_rs ADD COLUMN id_admin VARCHAR(10) NOT NULL;
ALTER TABLE admin_rs ADD CONSTRAINT admin_rs_pkey PRIMARY KEY (id_admin);
ALTER TABLE admin_rs ADD CONSTRAINT admin_rs_id_user_key UNIQUE (id_user);
ALTER TABLE admin_rs ADD CONSTRAINT admin_rs_id_user_fkey FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

-- Rollback dokter table changes
ALTER TABLE dokter DROP CONSTRAINT dokter_pkey;
ALTER TABLE dokter ADD COLUMN id_dokter VARCHAR(10) NOT NULL;
ALTER TABLE dokter ADD CONSTRAINT dokter_pkey PRIMARY KEY (id_dokter);
ALTER TABLE dokter ADD CONSTRAINT dokter_id_user_key UNIQUE (id_user);
ALTER TABLE dokter ADD CONSTRAINT dokter_id_user_fkey FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

-- Rollback pasien table changes
ALTER TABLE pasien DROP CONSTRAINT pasien_pkey;
ALTER TABLE pasien ADD COLUMN id_pasien VARCHAR(10) NOT NULL;
ALTER TABLE pasien ADD CONSTRAINT pasien_pkey PRIMARY KEY (id_pasien);
ALTER TABLE pasien ADD CONSTRAINT pasien_id_user_key UNIQUE (id_user);
ALTER TABLE pasien ADD CONSTRAINT pasien_id_user_fkey FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

COMMIT;
*/
