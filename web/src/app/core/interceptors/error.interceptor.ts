import { HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

/** Logs API errors, which arrive as RFC 9457 Problem Details (see docs/errors.md). */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error) => {
      const problem = error.error;
      const detail = problem?.detail ?? error.message;
      console.error(`${problem?.title ?? 'Request failed'} (${error.status}):`, detail);
      return throwError(() => error);
    }),
  );
};
