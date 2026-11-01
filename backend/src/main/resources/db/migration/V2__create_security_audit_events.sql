CREATE TABLE security_audit_events (
  id BIGINT NOT NULL AUTO_INCREMENT,
  actor_username VARCHAR(80) NOT NULL,
  action VARCHAR(60) NOT NULL,
  target_type VARCHAR(40) NOT NULL,
  target_id BIGINT NULL,
  outcome VARCHAR(20) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  INDEX idx_audit_created_at (created_at),
  INDEX idx_audit_actor (actor_username),
  INDEX idx_audit_target (target_type, target_id)
) ENGINE=InnoDB;
