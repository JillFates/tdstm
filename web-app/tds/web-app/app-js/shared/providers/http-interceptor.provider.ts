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
import {NotifierService} from "../services/notifier.service";
import {AlertType} from '../model/alert.model';

export class HttpInterceptor extends Http {

    private notifierService;

    constructor(backend: ConnectionBackend, defaultOptions: RequestOptions, notifierService: NotifierService) {
        super(backend, defaultOptions);
        this.notifierService = notifierService;
    }

    request(url: string | Request, options?: RequestOptionsArgs): Observable<Response> {
        return this.intercept(super.request(url, options), url);
    }

    get(url: string, options?: RequestOptionsArgs): Observable<Response> {
        return this.intercept(super.get(url, options), url);
    }

    post(url: string, body: string, options?: RequestOptionsArgs): Observable<Response> {
        console.debug('Post Request to: ', url);
        return this.intercept(super.post(url, body, this.getRequestOptionArgs(options)), url);
    }

    put(url: string, body: string, options?: RequestOptionsArgs): Observable<Response> {
        return this.intercept(super.put(url, body, this.getRequestOptionArgs(options)), url);
    }

    delete(url: string, options?: RequestOptionsArgs): Observable<Response> {
        return this.intercept(super.delete(url, options), url);
    }

    getRequestOptionArgs(options?: RequestOptionsArgs): RequestOptionsArgs {
        if (options == null) {
            options = new RequestOptions();
        }
        if (options.headers == null) {
            options.headers = new Headers();
        }
        options.headers.append('Content-Type', 'application/json');
        return options;
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
        return observable.catch((err, source) => {
            if (err.status == 401) {
                return Observable.empty();
            } else {
                return Observable.throw(err);
            }
        }).finally(() => {
            //Invokes after the source observable sequence terminates gracefully or exceptionally.
            if (requestInfo) {
                this.notifierService.broadcast({
                    name: 'httpRequestCompleted',
                });
            }
        });
    }

    onError(error: any): Observable<any> {
        this.notifierService.broadcast({
            name: AlertType.DANGER,
            message: error.message
        });

        return Observable.throw(error.json().error || 'Server error');
    }

    onSuccess(res: any): Observable<any> {
        if (res.headers.get("x-login-url")) {
            window.location.href = res.headers.get("x-login-url");
        }
        return res.json();
    }
}

export let HttpServiceProvider =
    {
        provide: HttpInterceptor,
        useFactory: (xhrBackend: XHRBackend, requestOptions: RequestOptions, notifierService: NotifierService) => new HttpInterceptor(xhrBackend, requestOptions, notifierService),
        deps: [XHRBackend, RequestOptions, NotifierService]
    };