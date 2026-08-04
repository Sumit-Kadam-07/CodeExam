-- Clean all data from the online exam system
-- Keeps only the admin user (username='admin') with existing password hash
-- Run this after the schema is created:
--   mysql -u root -p < database/clean_data.sql

SET @admin_id = (SELECT id FROM users WHERE username = 'admin');
SET SQL_SAFE_UPDATES = 0;

-- Remove child records that reference other deleted data
DELETE FROM activity_logs;
DELETE FROM evaluation_results;
DELETE FROM coding_submissions;
DELETE FROM exam_results;
DELETE FROM attendance;
DELETE FROM test_cases;
DELETE FROM questions;
DELETE FROM exams;
DELETE FROM users WHERE id != @admin_id;

SET SQL_SAFE_UPDATES = 1;
