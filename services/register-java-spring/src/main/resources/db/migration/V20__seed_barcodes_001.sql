-- Seed: all barcodes ending with 001
BEGIN;
CREATE TABLE IF NOT EXISTS barcodes (
  code TEXT PRIMARY KEY,
  is_available BOOLEAN NOT NULL DEFAULT TRUE,
  size_rule_id INT,
  position TEXT,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('egk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ogk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lgk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('eak001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('oak001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lak001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ekb001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('okb001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lkb001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('eb001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ob001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lb001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ekg001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('okg001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lkg001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('es001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('os001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ls001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('eki001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('oki001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lki001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ei001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('oi001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('li001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('eik001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('oik001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lik001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ek001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ok001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ekn001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('okn001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lkn001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('en001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('on001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ln001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('enk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('onk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lnk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ep001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('op001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lp001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('eg001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('og001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lg001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('epk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('opk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lpk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ekt001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('okt001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lkt001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('et001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ot001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lt001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('etk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('otk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ltk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('eu001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ou001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lu001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('euk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('ouk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('luk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('eyk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('oyk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
INSERT INTO barcodes (code, is_available, size_rule_id, position, updated_at) VALUES ('lyk001', TRUE, NULL, NULL, now()) ON CONFLICT (code) DO NOTHING;
COMMIT;