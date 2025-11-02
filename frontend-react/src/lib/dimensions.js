// frontend-react/src/lib/dimensions.js
export function parseDimensionToMM(input) {
  if (input == null) return null;
  let s = String(input).trim().toLowerCase();
  if (!s) return null;

  // support German decimals
  s = s.replace(',', '.');

  // extract the first number
  const match = s.match(/[-+]?\d*\.?\d+/);
  if (!match) return null;
  const n = parseFloat(match[0]);
  if (!isFinite(n)) return null;

  // explicit units
  if (s.includes('mm')) return Math.round(n);
  if (s.includes('cm')) return Math.round(n * 10);
  if (s.includes('in') || s.includes('"')) return Math.round(n * 25.4);

  // no unit given: heuristic for books
  // - numbers < 60 → centimeters (e.g., 21 => 210 mm, 10.5 => 105 mm)
  // - numbers >= 60 → millimeters (e.g., 105 => 105 mm)
  return n < 60 ? Math.round(n * 10) : Math.round(n);
}