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
        this.notifierService.broadcast({
            name: 'httpRequestInitial'
        });
        console.debug('Request to: ', requestInfo.url);
        return observable.catch((err, source) => {
            if (err.status == 401 /*&& !_.endsWith(err.url, 'api/auth/login')*/) {
                //this._router.navigate(['/login']);
                return Observable.empty();
            } else {
                return Observable.throw(err);
            }
        }).finally(() => {
            this.notifierService.broadcast({
                name: 'httpRequestCompleted',
            });
            console.log('Request Completed ', requestInfo.url);
        });
    }
}

export let HttpServiceProvider =
    {
        provide: HttpInterceptor,
        useFactory: (xhrBackend: XHRBackend, requestOptions: RequestOptions, notifierService: NotifierService) => new HttpInterceptor(xhrBackend, requestOptions, notifierService),
        deps: [XHRBackend, RequestOptions, NotifierService]
    };