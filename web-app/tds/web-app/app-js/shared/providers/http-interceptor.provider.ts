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

import {Observable} from 'rxjs/Observable';
import {NotifierService} from '../services/notifier.service';
import {AlertType} from '../model/alert.model';
// Please refer to https://kb.transitionmanager.com/display/TMENG/FE%3A+Workaround #2
function cache<T>(): any {
	return this;
}

Observable.prototype.cache = cache;

declare module 'rxjs/Observable' {
	interface Observable<T> {
		cache: any;
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
		if (options === null) {
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

			this.notifierService.broadcast({
				name: AlertType.DANGER,
				message: error.message
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

}

export let HttpServiceProvider = {
	provide: HttpInterceptor,
	useFactory: (xhrBackend: XHRBackend, requestOptions: RequestOptions, notifierService: NotifierService) =>
		new HttpInterceptor(xhrBackend, requestOptions, notifierService),
	deps: [XHRBackend, RequestOptions, NotifierService]
};