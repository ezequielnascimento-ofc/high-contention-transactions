import { HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error) => {
      if (error.status === 409) {
        console.error('Conflito de negócio:', error.error?.message ?? error.message);
      } else if (error.status === 404) {
        console.error('Recurso não encontrado:', error.error?.message ?? error.message);
      } else if (error.status === 400) {
        console.error('Requisição inválida:', error.error?.message ?? error.message);
      } else {
        console.error('Erro inesperado:', error.error?.message ?? error.message);
      }
      return throwError(() => error);
    }),
  );
};
