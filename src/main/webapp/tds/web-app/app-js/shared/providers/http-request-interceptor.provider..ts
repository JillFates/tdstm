/**
 * Angular Interceptor to compliance new Http Client
 */
import {
	HttpErrorResponse,
	HttpEvent,
	HttpHandler,
	HttpInterceptor,
	HttpRequest,
	HttpResponse
} from '@angular/common/http';
// Model
import {ERROR_STATUS} from '../model/constants';
import {AlertType} from '../model/alert.model';
// Service
import {NotifierService} from '../services/notifier.service';
// Other
import {Observable, throwError} from 'rxjs';
import {map, catchError, finalize} from 'rxjs/operators';

export class HttpRequestInterceptor implements HttpInterceptor {

	constructor(private notifierService: NotifierService) {
	}

	intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

		const authReq = request.clone({
			setHeaders: { 'Content-Type': 'application/json' }
		});

		this.notifierService.broadcast({
			name: 'httpRequestInitial'
		});

		return next.handle(authReq).pipe(
			map((event: HttpEvent<any>) => {
				if (event instanceof HttpResponse) {
					// Detects if the user has been rejected do time Session expiration
					if (event.headers.get('x-login-url')) {
						window.location.href = event.headers.get('x-login-url');
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
				} else {
					errorMessage = 'Bad Request';
				}

				this.notifierService.broadcast({
					name: AlertType.DANGER,
					message: errorMessage
				});
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
					this.notifierService.broadcast({
						name: AlertType.DANGER,
						message: bodyResponse.errors
					});
					this.notifierService.broadcast({
						name: 'httpRequestHandlerCompletedWithErrors',
					});
				}
			} catch (error) {
				// There is only one place doing it, the Asset Explorer.
				console.warn('Error by processing the Response');
			}
		}
	}
}

export function HTTPFactory(notifierService: NotifierService) {
	return new HttpRequestInterceptor(notifierService);
}