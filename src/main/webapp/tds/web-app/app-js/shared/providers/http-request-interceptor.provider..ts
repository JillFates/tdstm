/**
 * Angular Interceptor to compliance new Http Client
 */
import {Injectable} from '@angular/core';
import {
	HttpErrorResponse,
	HttpEvent,
	HttpHandler,
	HttpInterceptor,
	HttpRequest,
	HttpResponse
} from '@angular/common/http';
import {Router} from '@angular/router';
// Module
import {AuthRouteStates} from '../../modules/auth/auth-route.module';
// NGXS
import {Store} from '@ngxs/store';
import {SessionExpired} from '../../modules/auth/action/login.actions';
// Model
import {ERROR_STATUS, FILE_UPLOAD_REMOVE_URL, FILE_UPLOAD_SAVE_URL} from '../model/constants';
import {AlertType} from '../model/alert.model';
import {CSRF} from '../../modules/auth/model/user-context.model';
import {UserContextState} from '../../modules/auth/state/user-context.state';
// Service
import {NotifierService} from '../services/notifier.service';
import {
	FILE_SYSTEM_URL,
	ETL_SCRIPT_UPLOAD_URL,
	ASSET_IMPORT_UPLOAD_URL,
	IMAGE_UPLOAD_URL
} from '../services/kendo-file-handler.service';
import {WindowService} from '../services/window.service';
// Other
import {Observable, throwError} from 'rxjs';
import {map, catchError, finalize} from 'rxjs/operators';

export const MULTIPART_FORM_DATA = 'multipart/form-data';
export const APPLICATION_JSON = 'application/json';
export const TOKEN_GUARD = ['POST', 'PUT', 'DELETE', 'PATCH'];

@Injectable()
export class HttpRequestInterceptor implements HttpInterceptor {

	constructor(
		private notifierService: NotifierService,
		private router: Router,
		private store: Store,
		private windowService: WindowService) {
	}

	intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		// kendo-file-upload.interceptor.ts
		if (
			request.url === FILE_UPLOAD_SAVE_URL ||
			request.url === FILE_UPLOAD_REMOVE_URL ||
			request.url === FILE_SYSTEM_URL ||
			request.url === ETL_SCRIPT_UPLOAD_URL ||
			request.url === ASSET_IMPORT_UPLOAD_URL ||
			request.url === IMAGE_UPLOAD_URL) {
			// Continue its flows so it can be catch by another Interceptor
			return next.handle(request);
		}
		// All request
		return this.handleRequest(request, next);
	}

	/**
	 * Listen to all Request to provide the Loader and Validation Handler
	 * @param request
	 * @param next
	 */
	private handleRequest(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

		let contentType = APPLICATION_JSON;
		// Verify this request is as JSON, if not, do not overwrite the original request
		let originalRequestType = request.headers.get('Content-Type');
		if (originalRequestType !== null && originalRequestType !== APPLICATION_JSON) {
			contentType = originalRequestType;
		}

		const csrfToken = <CSRF>this.store.selectSnapshot(UserContextState.getCSRFToken);

		const headers = {'Content-Type': contentType};
		if (csrfToken && TOKEN_GUARD.indexOf(request.method) !== -1) {
			headers[csrfToken.tokenHeaderName] = csrfToken.token;
		}

		const authReq = request.clone({
			setHeaders: headers
		});

		this.notifierService.broadcast({
			name: 'httpRequestInitial'
		});

		return next.handle(authReq).pipe(
			map((event: HttpEvent<any>) => {
				if (event instanceof HttpResponse) {
					// Detects if the user has been rejected do time Session expiration
					if (event.headers.get('x-login-url')) {
						this.store.dispatch(new SessionExpired()).subscribe( () => {
							this.redirectUser(event.headers.get('x-login-url'));
						});
					}
					// Handle Errors
					this.intercept200Errors(event);
				}
				return event;
			}),
			catchError((error: HttpErrorResponse) => {
				let errorMessage = error.message;

				if (error && error.status === 404) {
					errorMessage = 'The requested resource was not found';
				} else if (error.headers && error.headers.get('x-login-url')) {
					if (window.location.href.indexOf(error.headers.get('x-login-url')) < 0) {
						errorMessage = '';
						this.store.dispatch(new SessionExpired()).subscribe( () => {
							this.redirectUser(error.headers.get('x-login-url'));
						});
					}
				} else if (error && error.status === 403) {
					errorMessage = '';
					if (this.router.url !== '/' + AuthRouteStates.LOGIN.url) {
						this.router.navigate(['/security/unauthorized']);
					}
				} else {
					errorMessage = 'Bad Request';
				}

				if (
					window.location.href.indexOf(error.headers.get('x-login-url')) < 0 &&
					this.router.url !== '/' + AuthRouteStates.LOGIN.url &&
					errorMessage !== ''
				) {
					this.notifierService.broadcast({
						name: AlertType.DANGER,
						message: errorMessage
					});
				}

				return throwError(error);
			}), finalize(() => {
				this.notifierService.broadcast({
					name: 'httpRequestCompleted',
				});
			}));
	}

	/**
	 * The app return 200 even when an error occurs
	 * This interceptor read the response looking for a error in the response
	 * if exists, pop up the information to the user.
	 * @param response
	 */
	private intercept200Errors(response: any): void {
		if (response && response['body']) {
			try {
				let bodyResponse = response.body;
				if (bodyResponse.status === ERROR_STATUS) {
					// drop any error from some pages
					if (this.router.url !== '/' + AuthRouteStates.LOGIN.url) {
						this.notifierService.broadcast({
							name: AlertType.DANGER,
							message: bodyResponse.errors
						});
					}
					this.notifierService.broadcast({
						name: 'httpRequestHandlerCompletedWithErrors',
					});
				}
			} catch (error) {
				console.warn('Error by processing the Response');
			}
		}
	}

	/**
	 * Redirect the user to the Configured Login Path
	 * @param fullPath
	 */
	private redirectUser(fullPath: string): void {
		// if (RouterUtils.isAngularRoute(fullPath)) {
		// 	this.router.navigate(RouterUtils.getAngularRoute(fullPath));
		// } else {
			this.windowService.getWindow().location.href = fullPath;
		// }
	}
}

export function HTTPFactory(notifierService: NotifierService, store: Store, router: Router, windowService: WindowService) {
	return new HttpRequestInterceptor(notifierService, router, store, windowService);
}
