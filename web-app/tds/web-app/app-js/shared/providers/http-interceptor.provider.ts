/**
 * Angular 2 does not have a interceptor service, for being able to listen to each cll
 * we extends the original Http class to add several methods to our purpose.
 */

import {
	Http,
	Request,
	RequestOptionsArgs,
	Response,
	XHRBackend,
	RequestOptions,
	ConnectionBackend,
	Headers
} from '@angular/http';

import {Observable} from 'rxjs';
import {catchError, finalize} from 'rxjs/operators';
import {NotifierService} from '../services/notifier.service';
import {AlertType} from '../model/alert.model';
// Please refer to https://kb.transitionmanager.com/display/TMENG/FE%3A+Workaround #2
import {cache, CacheSignature} from '../services/cache';
import {ERROR_STATUS} from '../model/constants';

Observable.prototype['cache'] = cache;

declare module 'rxjs/Observable' {
	interface Observable<T> {
		cache: CacheSignature<T>;
	}
}

export class HttpInterceptor extends Http {

	private notifierService;

	constructor(backend: ConnectionBackend, defaultOptions: RequestOptions, notifierService: NotifierService) {
		super(backend, defaultOptions);
		this.notifierService = notifierService;
	}

	request(url: string | Request, options?: RequestOptionsArgs): Observable<Response> {
		return this.intercept(super.request(url, options).map(res => {
			return this.onSuccessHandler(res);
		}), url);
	}

	get(url: string, options?: RequestOptionsArgs): Observable<Response> {
		return this.intercept(super.request(url, options).map(res => {
			return this.onSuccessHandler(res);
		}), url);
	}

	post(url: string, body: string, options?: RequestOptionsArgs): Observable<Response> {
		return this.intercept(super.post(url, body, this.getRequestOptionArgs(options)).map(res => {
			return this.onSuccessHandler(res);
		}), url);
	}

	put(url: string, body: string, options?: RequestOptionsArgs): Observable<Response> {
		return this.intercept(super.put(url, body, this.getRequestOptionArgs(options)).map(res => {
			return this.onSuccessHandler(res);
		}), url);
	}

	delete(url: string, options?: RequestOptionsArgs): Observable<Response> {
		return this.intercept(super.delete(url, options).map(res => {
			return this.onSuccessHandler(res);
		}), url);
	}

	getRequestOptionArgs(options?: RequestOptionsArgs): RequestOptionsArgs {
		let headers = new Headers({'Content-Type': 'application/json'});
		if (!options) {
			options = new RequestOptions({headers: headers});
		}
		return options;
	}

	/**
	 * Handle the on Success, currently server always returns 200, but the x-login-url can be catch
	 * when the session has already expired
	 * @param response
	 * @returns {any}
	 */
	onSuccessHandler(response: any): any {
		if (response.headers.get('x-login-url')) {
			window.location.href = response.headers.get('x-login-url');
		}

		this.intercept200Errors(response);

		return response;
	}

	/**
	 * Intercept the observable to track errors
	 */
	intercept(observable: Observable<Response>, requestInfo): Observable<Response> {
		if (requestInfo) {
			this.notifierService.broadcast({
				name: 'httpRequestInitial'
			});
		}
		return observable.catch((error, source) => {

			let errorMessage = error.message;

			// TODO: For Angular 5, Found a way to inject Translate Pipe or move this outside the interceptor
			if (error && error.status === 404) {
				errorMessage = 'The requested resource was not found';
			} else {
				errorMessage = 'Bad Request';
			}

			this.notifierService.broadcast({
				name: AlertType.DANGER,
				message: errorMessage
			});

			if (error.status === 401) {
				return Observable.empty();
			} else {
				return Observable.throw(error);
			}
		}).finally(() => {
			// Invokes after the source observable sequence terminates gracefully or exceptionally.
			if (requestInfo) {
				this.notifierService.broadcast({
					name: 'httpRequestCompleted',
				});
			}
		});
	}

	/**
	 * The app return 200 even when an error occurs
	 * This interceptor read the response looking for a error in the response
	 * if exists, pop up the information to the user.
	 * @param response
	 */
	intercept200Errors(response: any): void {
		if (response && response['_body']) {
			try {
				let bodyResponse = JSON.parse(response['_body']);
				if (bodyResponse['status'] === ERROR_STATUS) {
					this.notifierService.broadcast({
						name: AlertType.DANGER,
						message: bodyResponse['errors']
					});
					this.notifierService.broadcast({
						name: 'httpRequestHandlerCompletedWithErrors',
					});
				}
			} catch (error) {
				// There is only one place doing it, the Asset Explorer.
				console.warn('Request Body is not a JSON object');
			}
		}
	}

}

export let HttpServiceProvider = {
	provide: HttpInterceptor,
	useFactory: (xhrBackend: XHRBackend, requestOptions: RequestOptions, notifierService: NotifierService) =>
		new HttpInterceptor(xhrBackend, requestOptions, notifierService),
	deps: [XHRBackend, RequestOptions, NotifierService]
};