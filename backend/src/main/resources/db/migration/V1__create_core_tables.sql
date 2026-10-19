CREATE TABLE staff_accounts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(80) NOT NULL,
  display_name VARCHAR(100) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  CONSTRAINT uk_staff_username UNIQUE (username),
  CONSTRAINT chk_staff_role CHECK (role IN ('ADMIN','DOCTOR','RECEPTIONIST'))
) ENGINE=InnoDB;

CREATE TABLE patient_records (
  id BIGINT NOT NULL AUTO_INCREMENT,
  full_name VARCHAR(100) NOT NULL,
  date_of_birth DATE NOT NULL,
  address VARCHAR(180),
  phone VARCHAR(30),
  clinical_notes VARCHAR(2000),
  prescription_notes VARCHAR(2000),
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  INDEX idx_patient_name (full_name)
) ENGINE=InnoDB;
