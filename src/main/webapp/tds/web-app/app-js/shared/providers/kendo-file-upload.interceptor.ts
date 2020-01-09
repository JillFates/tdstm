/**
 * Kendo Interceptor
 */
import {Injectable} from '@angular/core';
import {
	HttpErrorResponse,
	HttpEvent,
	HttpEventType,
	HttpHandler,
	HttpInterceptor,
	HttpProgressEvent,
	HttpRequest, HttpResponse,
} from '@angular/common/http';
// NGXS
import {Store} from '@ngxs/store';
// Model
import {CSRF} from '../../modules/auth/model/user-context.model';
import {UserContextState} from '../../modules/auth/state/user-context.state';
import {
	ASSET_IMPORT_FILE_UPLOAD_TYPE,
	ETL_SCRIPT_FILE_UPLOAD_TYPE, FILE_UPLOAD_REMOVE_URL, FILE_UPLOAD_SAVE_URL,
	FILE_UPLOAD_TYPE_PARAM,
	REMOVE_FILENAME_PARAM, SAVE_FILENAME_PARAM
} from '../model/constants';
import {TOKEN_GUARD} from './http-request-interceptor.provider.';
// Service
import {
	ASSET_IMPORT_UPLOAD_URL,
	ETL_SCRIPT_UPLOAD_URL,
	FILE_SYSTEM_URL, IMAGE_UPLOAD_URL,
	KendoFileHandlerService
} from '../services/kendo-file-handler.service';
// Other
import {Observable, throwError} from 'rxjs';
import {map, catchError} from 'rxjs/operators';
import 'rxjs/add/operator/delay';
import 'rxjs/add/observable/concat';
import {FileRestrictions} from '@progress/kendo-angular-upload';
import {SessionExpired} from '../../modules/auth/action/login.actions';
import {AuthRouteStates} from '../../modules/auth/auth-route.module';
import {Router} from '@angular/router';
import {WindowService} from '../services/window.service';

/**
 * Mainly used by Kendo Upload Component.
 * @see Kendo Upload Component docs.
 */

@Injectable()
export class KendoFileUploadInterceptor implements HttpInterceptor {

	constructor(
		private kendoFileHandlerService: KendoFileHandlerService,
		private router: Router,
		private store: Store,
		private windowService: WindowService
	) {}

	intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		if (request.url === FILE_UPLOAD_SAVE_URL) {
			let events: Observable<HttpEvent<any>>[] = [100].map((x) => Observable.of(<HttpProgressEvent>{
				type: HttpEventType.UploadProgress,
				loaded: x,
				total: 100
			}).delay(1000));
			if (request.body.get(FILE_UPLOAD_TYPE_PARAM) === ETL_SCRIPT_FILE_UPLOAD_TYPE) {
				events.push(this.kendoFileHandlerService.uploadETLScriptFile(request.body));
			} else if (request.body.get(FILE_UPLOAD_TYPE_PARAM) === ASSET_IMPORT_FILE_UPLOAD_TYPE) {
				events.push(this.kendoFileHandlerService.uploadAssetImportFile(request.body));
			} else {
				events.push(this.kendoFileHandlerService.uploadFile(request.body));
			}
			return Observable.concat(...events);
		}
		if (request.url === FILE_UPLOAD_REMOVE_URL) {
			let filename = request.body.get(REMOVE_FILENAME_PARAM);
			return this.kendoFileHandlerService.deleteFile(filename);
		}

		if (
			request.url === FILE_SYSTEM_URL ||
			request.url === ETL_SCRIPT_UPLOAD_URL ||
			request.url === ASSET_IMPORT_UPLOAD_URL ||
			request.url === IMAGE_UPLOAD_URL) {
			// Upload Request
			return this.handleRequest(request, next);
		}
		// All request
		return next.handle(request);
	}

	/**
	 * Listen to all Request to provide the Loader and Validation Handler
	 * @param request
	 * @param next
	 */
	private handleRequest(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

		const csrfToken = <CSRF>this.store.selectSnapshot(UserContextState.getCSRFToken);

		const headers = {};
		if (csrfToken && TOKEN_GUARD.indexOf(request.method) !== -1) {
			headers[csrfToken.tokenHeaderName] = csrfToken.token;
		}

		const authReq = request.clone({
			setHeaders: headers
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
				}
				return event;
			}),
			catchError((error: HttpErrorResponse) => {
				if (error && error.status === 404) {
					// Nothing to do
				} else if (error.headers && error.headers.get('x-login-url')) {
					if (window.location.href.indexOf(error.headers.get('x-login-url')) < 0) {
						this.store.dispatch(new SessionExpired()).subscribe( () => {
							this.redirectUser(error.headers.get('x-login-url'));
						});
					}
				} else if (error && error.status === 403) {
					if (this.router.url !== '/' + AuthRouteStates.LOGIN.url) {
						this.router.navigate(['/security/unauthorized']);
					}
				}

				return throwError(error);
			}));
	}

	/**
	 * Redirect the user to the Configured Login Path
	 * @param fullPath
	 */
	private redirectUser(fullPath: string): void {
		this.windowService.getWindow().location.href = fullPath;
	}
}

export class KendoFileUploadBasicConfig {

	uploadRestrictions: FileRestrictions;
	uploadSaveUrl: string;
	uploadDeleteUrl: string;
	autoUpload: boolean;
	saveField: string;
	removeField: string;
	multiple: boolean;
	[x: string]: any; // this enables the model to add any extra property as needed.

	constructor() {
		this.uploadRestrictions = { allowedExtensions: ['csv', 'xml', 'json', 'xlsx', 'xls'] };
		this.uploadSaveUrl = FILE_UPLOAD_SAVE_URL;
		this.uploadDeleteUrl = FILE_UPLOAD_REMOVE_URL;
		this.autoUpload = false;
		this.saveField =  SAVE_FILENAME_PARAM;
		this.removeField = REMOVE_FILENAME_PARAM;
		this.multiple = false;
	}
}

export function HTTPKendoFactory(kendoFileHandlerService: KendoFileHandlerService, store: Store, router: Router, windowService: WindowService) {
	return new KendoFileUploadInterceptor(kendoFileHandlerService, router, store, windowService);
}
