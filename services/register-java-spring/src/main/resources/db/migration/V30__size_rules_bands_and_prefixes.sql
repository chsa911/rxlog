-- Size rules as per SizeRules.md (inclusive width bins + eq/lt/gt bands + prefixes)
-- Units: store everything in mm. The doc uses cm; we convert cm*10 below.
-- Positions: down=e, left=l, up=o

BEGIN;

-- 1) Extend schema to store the full rule
ALTER TABLE size_rules
  ADD COLUMN IF NOT EXISTS min_width_mm    INT,
  ADD COLUMN IF NOT EXISTS max_width_mm    INT,            -- NULL means open
  ADD COLUMN IF NOT EXISTS min_height_mm   INT DEFAULT 0,  -- keep inclusive model
  ADD COLUMN IF NOT EXISTS max_height_mm   INT,
  ADD COLUMN IF NOT EXISTS color_code      TEXT,           -- e.g. 'gk','ak','kg',...
  ADD COLUMN IF NOT EXISTS t_height_mm     INT,            -- threshold T for lt/gt
  ADD COLUMN IF NOT EXISTS eq_values_mm    INT[],          -- exact H matches (mm)
  ADD COLUMN IF NOT EXISTS gt_exclude_mm   INT[],          -- excluded exact H for gt
  ADD COLUMN IF NOT EXISTS prefix_down     TEXT,           -- 'e'||color_code
  ADD COLUMN IF NOT EXISTS prefix_left     TEXT,           -- 'l'||color_code
  ADD COLUMN IF NOT EXISTS prefix_up       TEXT;           -- 'o'||color_code

-- 2) Staging of bins in cm (as in the doc). We will upsert into size_rules with mm.
DROP TABLE IF EXISTS _sr_bins_cm;
CREATE TEMP TABLE _sr_bins_cm (
  bin_id      INT PRIMARY KEY,
  wmin_cm     NUMERIC,
  wmax_cm     NUMERIC,
  t_cm        NUMERIC,
  color_code  TEXT,
  eq1_cm      NUMERIC,
  eq2_cm      NUMERIC,
  eq3_cm      NUMERIC,
  ex1_cm      NUMERIC,
  ex2_cm      NUMERIC,
  ex3_cm      NUMERIC
);

-- Fill all bins exactly as in the documentation (cm values).
INSERT INTO _sr_bins_cm VALUES
-- bin,  wmin,   wmax,   T,     color,  eqs...,           excludes...
( 0,   0.0001, 10.5 , 17.5,  'gk',   20.5, 21.0, 21.5,   20.5, 21.0, 21.5),
( 1,  10.6  , 11.0 , 18.0,  'ak',   20.5, 21.0, 21.5,   20.5, 21.0, 21.5),
( 2,  11.1  , 11.4 , 18.0,  'kb',   20.5, 21.0, 21.5,   20.5, 21.0, 21.5),
( 3,  11.5  , 11.5 , 18.0,  'b' ,   20.5, 21.0, 21.5,   20.5, 21.0, 21.5),
( 4,  11.6  , 11.9 , 19.0,  'kg',   20.5, 21.0, 21.5,   20.5, 21.0, 21.5),
( 5,  12.0  , 12.0 , 18.5,  's' ,   20.5, 21.0, 21.5,   20.5, 21.0, 21.5),
( 6,  12.1  , 12.4 , 19.0,  'ki',   20.5, 21.0, 21.5,   20.5, 21.0, 21.5),
( 7,  12.5  , 12.5 , 19.0,  'i' ,   20.5, 21.0, 21.5,   20.5, 21.0, 21.5),
( 8,  12.6  , 13.0 , 20.0,  'k' ,   20.5, 21.0, 21.5,   20.5, 21.0, 21.5),
( 9,  13.1  , 13.4 , 21.0,  'kn',   20.5, NULL, 21.5,   20.5, NULL, 21.5),
(10,  13.5  , 13.5 , 21.0,  'n' ,   20.5, NULL, 21.5,   20.5, NULL, 21.5),
(11,  13.6  , 14.0 , 21.0,  'nk',   20.5, NULL, 21.5,   20.5, NULL, 21.5),
(12,  14.1  , 14.5 , 21.0,  'p' ,   20.5, NULL, 21.5,   20.5, NULL, 21.5),
(13,  14.6  , 15.0 , 21.0,  'g' ,   20.5, NULL, 21.5,   20.5, NULL, 21.5),
(14,  15.1  , 15.5 , 22.0,  'pk',   20.5, 21.0, 21.5,   NULL , NULL, NULL ),
(15,  15.6  , 17.4 , 23.0,  'kt',   20.5, 21.0, 21.5,   NULL , NULL, NULL ),
(16,  17.5  , 17.5 , 23.0,  't' ,   20.5, 21.0, 21.5,   NULL , NULL, NULL ),
(17,  17.6  , 22.5 , 23.0,  'tk',   20.5, 21.0, 21.5,   NULL , NULL, NULL ),
(18,  22.6  , 24.0 , 28.0,  'u' ,   20.5, 21.0, 21.5,   NULL , NULL, NULL ),
(19,  24.1  , 24.5 , 29.0,  'uk',   20.5, 21.0, 21.5,   NULL , NULL, NULL ),
(20,  24.6  , 40.0 , 32.0,  'yk',   20.5, 21.0, 21.5,   NULL , NULL, NULL );

-- 3) Upsert into size_rules in mm and compute position prefixes from color_code
--    left = 'l'||color_code, down='e'||color_code, up='o'||color_code
WITH mm AS (
  SELECT
    bin_id,
    ROUND(wmin_cm*10)::INT   AS min_w_mm,
    CASE WHEN wmax_cm IS NULL THEN NULL ELSE ROUND(wmax_cm*10)::INT END AS max_w_mm,
    0                        AS min_h_mm,
    NULL::INT                AS max_h_mm,
    ROUND(t_cm*10)::INT      AS t_mm,
    color_code               AS cc,
    ARRAY_REMOVE(ARRAY[CASE WHEN eq1_cm IS NULL THEN NULL ELSE ROUND(eq1_cm*10)::INT END,
                            CASE WHEN eq2_cm IS NULL THEN NULL ELSE ROUND(eq2_cm*10)::INT END,
                            CASE WHEN eq3_cm IS NULL THEN NULL ELSE ROUND(eq3_cm*10)::INT END], NULL) AS eq_mm,
    ARRAY_REMOVE(ARRAY[CASE WHEN ex1_cm IS NULL THEN NULL ELSE ROUND(ex1_cm*10)::INT END,
                            CASE WHEN ex2_cm IS NULL THEN NULL ELSE ROUND(ex2_cm*10)::INT END,
                            CASE WHEN ex3_cm IS NULL THEN NULL ELSE ROUND(ex3_cm*10)::INT END], NULL) AS ex_mm
  FROM _sr_bins_cm
)
INSERT INTO size_rules AS sr
  (id,           name,                  min_width_mm, max_width_mm, min_height_mm, max_height_mm,
   color,  color_code, t_height_mm,     eq_values_mm, gt_exclude_mm,
   prefix_down,       prefix_left,       prefix_up)
SELECT
  bin_id+1,      CONCAT('Bin ',bin_id), min_w_mm,     max_w_mm,     min_h_mm,     max_h_mm,
  NULL::TEXT,    cc,        t_mm,       eq_mm,        ex_mm,
  'e'||cc,       'l'||cc,               'o'||cc
FROM mm
ON CONFLICT (id) DO UPDATE SET
  min_width_mm  = EXCLUDED.min_width_mm,
  max_width_mm  = EXCLUDED.max_width_mm,
  min_height_mm = EXCLUDED.min_height_mm,
  max_height_mm = EXCLUDED.max_height_mm,
  color_code    = EXCLUDED.color_code,
  t_height_mm   = EXCLUDED.t_height_mm,
  eq_values_mm  = EXCLUDED.eq_values_mm,
  gt_exclude_mm = EXCLUDED.gt_exclude_mm,
  prefix_down   = EXCLUDED.prefix_down,
  prefix_left   = EXCLUDED.prefix_left,
  prefix_up     = EXCLUDED.prefix_up;

COMMIT;