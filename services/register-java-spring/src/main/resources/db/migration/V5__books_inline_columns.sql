-- V5__books_inline_columns.sql
-- NOTE: this version is TEXT-friendly (books.id is TEXT from Mongo). It also repairs
-- any pre-existing book_barcodes table that used UUID by mistake.

-- Basic required inline columns
ALTER TABLE books
  ADD COLUMN IF NOT EXISTS author                      TEXT,
  ADD COLUMN IF NOT EXISTS publisher                   TEXT,
  ADD COLUMN IF NOT EXISTS pages                       INT;

-- Keywords + positions
ALTER TABLE books
  ADD COLUMN IF NOT EXISTS title_keyword               TEXT,
  ADD COLUMN IF NOT EXISTS title_keyword_position      INT,
  ADD COLUMN IF NOT EXISTS title_keyword2              TEXT,
  ADD COLUMN IF NOT EXISTS title_keyword2_position     INT,
  ADD COLUMN IF NOT EXISTS title_keyword3              TEXT,
  ADD COLUMN IF NOT EXISTS title_keyword3_position     INT;

-- Dimensions in mm
ALTER TABLE books
  ADD COLUMN IF NOT EXISTS width_mm                    INT,
  ADD COLUMN IF NOT EXISTS height_mm                   INT;

-- Reading status & top flag
ALTER TABLE books
  ADD COLUMN IF NOT EXISTS reading_status              TEXT,
  ADD COLUMN IF NOT EXISTS top_book                    BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS reading_status_updated_at   TIMESTAMPTZ;


CREATE TABLE IF NOT EXISTS book_barcodes (
  book_id TEXT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
  barcode TEXT NOT NULL,
  PRIMARY KEY (book_id, barcode)
);

DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name   = 'book_barcodes'
      AND column_name  = 'book_id'
      AND data_type    = 'uuid'
  ) THEN
    ALTER TABLE book_barcodes DROP CONSTRAINT IF EXISTS book_barcodes_book_id_fkey;
    ALTER TABLE book_barcodes
      ALTER COLUMN book_id TYPE TEXT USING book_id::text;
    ALTER TABLE book_barcodes
      ADD CONSTRAINT book_barcodes_book_id_fkey
      FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE;
  END IF;
END $$;

