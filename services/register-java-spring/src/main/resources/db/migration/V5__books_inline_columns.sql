-- Align books table with current JPA entity (inline author/publisher + extra fields)

-- Basic required inline columns
ALTER TABLE books
  ADD COLUMN IF NOT EXISTS author                 TEXT,
  ADD COLUMN IF NOT EXISTS publisher              TEXT,
  ADD COLUMN IF NOT EXISTS pages                  INT;

-- Keywords + positions
ALTER TABLE books
  ADD COLUMN IF NOT EXISTS title_keyword          TEXT,
  ADD COLUMN IF NOT EXISTS title_keyword_position INT,
  ADD COLUMN IF NOT EXISTS title_keyword2         TEXT,
  ADD COLUMN IF NOT EXISTS title_keyword2_position INT,
  ADD COLUMN IF NOT EXISTS title_keyword3         TEXT,
  ADD COLUMN IF NOT EXISTS title_keyword3_position INT;

-- Dimensions in mm
ALTER TABLE books
  ADD COLUMN IF NOT EXISTS width_mm               INT,
  ADD COLUMN IF NOT EXISTS height_mm              INT;

-- Reading status & top flag
ALTER TABLE books
  ADD COLUMN IF NOT EXISTS reading_status         TEXT,
  ADD COLUMN IF NOT EXISTS top_book               BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS reading_status_updated_at TIMESTAMPTZ;

-- If you used old foreign keys, keep them or drop them explicitly if no longer used:
-- ALTER TABLE books DROP COLUMN IF EXISTS author_id;
-- ALTER TABLE books DROP COLUMN IF EXISTS publisher_id;

-- Optional: a helper table for barcodes (if you store them normalized)
CREATE TABLE IF NOT EXISTS book_barcodes (
  book_id UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
  barcode TEXT NOT NULL,
  PRIMARY KEY (book_id, barcode)
);