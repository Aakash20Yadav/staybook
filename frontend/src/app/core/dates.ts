/** Date helpers that work on plain YYYY-MM-DD strings in the browser's local time. */

export function toIso(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

export function fromIso(iso: string): Date {
  const [y, m, d] = iso.split('-').map(Number);
  return new Date(y, m - 1, d);
}

export function todayIso(): string {
  return toIso(new Date());
}

export function addDays(iso: string, days: number): string {
  const date = fromIso(iso);
  date.setDate(date.getDate() + days);
  return toIso(date);
}

export function nightsBetween(checkIn: string, checkOut: string): number {
  const ms = fromIso(checkOut).getTime() - fromIso(checkIn).getTime();
  return Math.round(ms / 86_400_000);
}

export const MAX_NIGHTS = 30;

/**
 * Mirrors the backend rules so guests get instant feedback.
 * The backend still validates everything; this is only for convenience.
 */
export function validateStay(checkIn: string, checkOut: string): string | null {
  if (!checkIn || !checkOut) {
    return 'Please choose both check-in and check-out dates.';
  }
  if (checkIn < todayIso()) {
    return 'Check-in date cannot be in the past.';
  }
  if (checkOut <= checkIn) {
    return 'Check-out date must be after the check-in date.';
  }
  if (nightsBetween(checkIn, checkOut) > MAX_NIGHTS) {
    return `A single booking can be at most ${MAX_NIGHTS} nights.`;
  }
  return null;
}
