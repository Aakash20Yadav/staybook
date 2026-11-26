import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from './models';

/** Turns any HTTP failure into a sentence a guest can act on. */
export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'We could not reach the server. Check that the backend is running and try again.';
    }
    const body = err.error as Partial<ApiError> | null;
    if (body && typeof body === 'object' && body.message) {
      const fields = body.fieldErrors ?? [];
      return fields.length ? fields.map((f) => f.message).join(' ') : body.message;
    }
    if (err.status >= 500) {
      return 'Something went wrong on our side. Please try again in a moment.';
    }
  }
  return 'Something unexpected happened. Please try again.';
}

export function errorCode(err: unknown): string | null {
  if (err instanceof HttpErrorResponse && err.error && typeof err.error === 'object') {
    return (err.error as Partial<ApiError>).code ?? null;
  }
  return null;
}

/** Field-level messages keyed by field name, for showing under inputs. */
export function fieldErrors(err: unknown): Record<string, string> {
  const map: Record<string, string> = {};
  if (err instanceof HttpErrorResponse && err.error?.fieldErrors) {
    for (const f of err.error.fieldErrors as ApiError['fieldErrors']) {
      map[f.field] = f.message;
    }
  }
  return map;
}
