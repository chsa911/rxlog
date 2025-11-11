-- V102: add registered_at and top_book_set_at to books

BEGIN;

-- 1) add columns if missing
ALTER TABLE public.books
  ADD COLUMN IF NOT EXISTS registered_at      timestamptz,
  ADD COLUMN IF NOT EXISTS top_book_set_at    timestamptz;

-- 2) backfill existing rows
-- if a book already has top_book=true but no timestamp, set it to "now()" as a reasonable default
UPDATE public.books
SET top_book_set_at = COALESCE(top_book_set_at, now())
WHERE top_book = true;

-- if registered_at is null, use the best available source; if none, fallback to "now()"
-- (replace created_at if you have such a column)
UPDATE public.books
SET registered_at = COALESCE(registered_at, reading_status_updated_at, now())
WHERE registered_at IS NULL;

-- 3) optional helpful indexes
CREATE INDEX IF NOT EXISTS books_registered_at_idx   ON public.books (registered_at DESC);
CREATE INDEX IF NOT EXISTS books_top_book_idx        ON public.books (top_book DESC, top_book_set_at DESC);

COMMIT;