import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {BehaviorSubject, catchError, filter, Observable, switchMap, take, throwError} from 'rxjs';
import {AuthService} from "./auth.service";
import {AuthResponse} from "./models/auth.response";
import {Router} from "@angular/router";
import {Urls} from "../utils/urls";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

  constructor(private authService: AuthService, private router: Router) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<any> {
    const urlsToSkip = [Urls.signup, Urls.login, Urls.refreshToken, Urls.logout(""), "ztpai"];
    if (urlsToSkip.some(url => request.url.includes(<string>url)))
      return next.handle(request);

    let storedCredentials: AuthResponse | null = this.authService.getStoredCredentials();

    if (storedCredentials !== null)
      request = this.addToken(request, storedCredentials.accessToken);

    return next.handle(request)
      .pipe(catchError(error => {
        if (error instanceof HttpErrorResponse && error.status === 401) {
          return this.handle401Error(request, next);
        } else {
          return throwError(error);
        }
      }));
  }

  private addToken(request: HttpRequest<any>, token: string) {
    return request.clone({
      setHeaders: {
        'Authorization': `Bearer ${token}`
      }
    });
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler) {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      return this.authService.refreshToken().pipe(
        switchMap(response => {
          this.isRefreshing = false;
          this.refreshTokenSubject.next(response.accessToken);
          request = this.addToken(request, response.accessToken);
          return next.handle(request);
        }),
        catchError((err) => {
          this.isRefreshing = false;
          this.authService.logOut().subscribe(() => {
            this.router.navigateByUrl('/login');
          });
          return throwError(err);
        }));
    } else {
      return this.refreshTokenSubject.pipe(
        filter(token => token != null),
        take(1),
        switchMap(jwt => next.handle(this.addToken(request, jwt))));
    }
  }
}
