-- Smart Coding Exam System (MySQL) - Production-ready schema
-- Notes:
-- 1) Use InnoDB for transactions + FK constraints
-- 2) Add indexes for common lookup columns
-- 3) Use utf8mb4 for full Unicode

CREATE DATABASE IF NOT EXISTS smart_coding_exam
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE smart_coding_exam;

-- =========================
-- 1) USERS
-- =========================
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL, -- ROLE_ADMIN / ROLE_STUDENT (teacher can be another role)
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(120),
  mobile VARCHAR(20),
  profile_picture_url VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =========================
-- 2) EXAMS
-- =========================
CREATE TABLE exams (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  created_by_user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  duration_minutes INT NOT NULL,
  is_published BOOLEAN NOT NULL DEFAULT TRUE,
  starts_at DATETIME,
  ends_at DATETIME,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_exams_created_by
    FOREIGN KEY (created_by_user_id) REFERENCES users(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_exams_published ON exams(is_published);
CREATE INDEX idx_exams_starts_ends ON exams(starts_at, ends_at);

-- =========================
-- 3) QUESTIONS
-- =========================
CREATE TABLE questions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  exam_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  problem_statement TEXT NOT NULL,
  input_format MEDIUMTEXT,
  output_format MEDIUMTEXT,
  constraints MEDIUMTEXT,
  sample_input MEDIUMTEXT,
  sample_output MEDIUMTEXT,
  marks INT NOT NULL DEFAULT 10,
  difficulty VARCHAR(20),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_questions_exam
    FOREIGN KEY (exam_id) REFERENCES exams(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_questions_exam ON questions(exam_id);

-- =========================
-- 4) TEST_CASES
-- =========================
CREATE TABLE test_cases (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  question_id BIGINT NOT NULL,
  input_data MEDIUMTEXT NOT NULL,
  expected_output MEDIUMTEXT NOT NULL,
  input_type VARCHAR(50) DEFAULT 'STDIN',
  output_type VARCHAR(50) DEFAULT 'STDOUT',
  sequence_no INT NOT NULL, -- to keep order
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_test_cases_question
    FOREIGN KEY (question_id) REFERENCES questions(id)
    ON DELETE CASCADE,
  UNIQUE KEY uq_test_case_question_seq (question_id, sequence_no)
) ENGINE=InnoDB;

CREATE INDEX idx_test_cases_question ON test_cases(question_id);

-- =========================
-- 5) CODING_SUBMISSIONS
-- =========================
CREATE TABLE coding_submissions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  exam_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  attempt_no INT NOT NULL DEFAULT 1,
  language VARCHAR(30) NOT NULL,
  source_code MEDIUMTEXT NOT NULL,
  submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED', -- SUBMITTED / EVALUATED / FAILED
  CONSTRAINT fk_sub_exam
    FOREIGN KEY (exam_id) REFERENCES exams(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_sub_question
    FOREIGN KEY (question_id) REFERENCES questions(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_sub_student
    FOREIGN KEY (student_id) REFERENCES users(id)
    ON DELETE CASCADE,
  UNIQUE KEY uq_submission (exam_id, question_id, student_id, attempt_no)
) ENGINE=InnoDB;

CREATE INDEX idx_sub_student ON coding_submissions(student_id);
CREATE INDEX idx_sub_exam_question ON coding_submissions(exam_id, question_id);

-- =========================
-- 6) EVALUATION_RESULTS (per test case)
-- =========================
CREATE TABLE evaluation_results (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id BIGINT NOT NULL,
  test_case_id BIGINT NOT NULL,
  pass BOOLEAN NOT NULL,
  actual_output MEDIUMTEXT,
  expected_output MEDIUMTEXT,
  execution_time_ms INT NOT NULL,
  error_message TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_eval_submission
    FOREIGN KEY (submission_id) REFERENCES coding_submissions(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_eval_test_case
    FOREIGN KEY (test_case_id) REFERENCES test_cases(id)
    ON DELETE RESTRICT,
  UNIQUE KEY uq_eval_per_submission_test (submission_id, test_case_id)
) ENGINE=InnoDB;

CREATE INDEX idx_eval_submission ON evaluation_results(submission_id);

-- =========================
-- 7) EXAM_RESULTS (aggregated summary per exam attempt)
-- =========================
CREATE TABLE exam_results (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  exam_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  attempt_no INT NOT NULL DEFAULT 1,
  total_score INT NOT NULL,
  percentage DECIMAL(6,2) NOT NULL,
  is_pass BOOLEAN NOT NULL DEFAULT FALSE,
  started_at DATETIME,
  finished_at DATETIME,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_exam_results_exam
    FOREIGN KEY (exam_id) REFERENCES exams(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_exam_results_student
    FOREIGN KEY (student_id) REFERENCES users(id)
    ON DELETE CASCADE,
  UNIQUE KEY uq_exam_result (exam_id, student_id, attempt_no)
) ENGINE=InnoDB;

CREATE INDEX idx_exam_results_student ON exam_results(student_id);

-- =========================
-- 8) ATTENDANCE
-- =========================
CREATE TABLE attendance (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  exam_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED', -- NOT_STARTED / IN_PROGRESS / SUBMITTED
  last_active_at DATETIME,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_attendance_exam
    FOREIGN KEY (exam_id) REFERENCES exams(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_attendance_student
    FOREIGN KEY (student_id) REFERENCES users(id)
    ON DELETE CASCADE,
  UNIQUE KEY uq_attendance (exam_id, student_id)
) ENGINE=InnoDB;

CREATE INDEX idx_attendance_exam ON attendance(exam_id);

-- End of MySQL schema

