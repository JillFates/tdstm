// Angular
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
// NGXS
import {Store} from '@ngxs/store';
import {TDSAppState} from '../../../app/state/tds-app.state';
import {Login} from '../action/login.actions';
// Models
import {USER_CONTEXT_REQUEST, UserContextModel} from '../model/user-context.model';
// Services
import {PermissionService} from '../../../shared/services/permission.service';
import {UserService} from './user.service';
import {WindowService} from '../../../shared/services/window.service';
// Other
import {from, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {PostNoticesService} from './post-notices.service';
import {APP_STATE_KEY} from '../../../shared/providers/localstorage.provider';

export enum AUTH_API_URLS {
	SIGN_IN = '/tdstm/auth/signIn',
	LOG_OUT = '/tdstm/auth/signOut',
	LOGIN_PAGE = '/tdstm/module/auth/login'
}

@Injectable()
export class AuthService {

	constructor(
		private http: HttpClient,
		private permissionService: PermissionService,
		private userService: UserService,
		private windowService: WindowService,
		private postNoticesService: PostNoticesService,
		// private router: Router,
		private store: Store) {
	}

	/**
	 * Log the user and get all the Context to enter to the application
	 * @param payload
	 */
	public getUserContext({payload}: Login): Observable<any> {
		return new Observable(observer => {
			this.login({payload}).subscribe(userContext => {
				let contextPromises = [];
				// Something goes wrong from the first login
				if (userContext['error']) {
					observer.error(userContext['error']);
					return observer.complete();
				}
				contextPromises.push(from(new Promise(resolve => resolve(userContext))));
				contextPromises.push(this.userService.getLicense());
				contextPromises.push(this.permissionService.getPermissions());
				contextPromises.push(this.postNoticesService.getPostNotices());

				Observable.forkJoin(contextPromises)
					.subscribe((contextResponse: any) => {
						let userContext = contextResponse[USER_CONTEXT_REQUEST.USER];
						userContext.license = contextResponse[USER_CONTEXT_REQUEST.LICENSE];
						userContext.permissions = contextResponse[USER_CONTEXT_REQUEST.PERMISSIONS];
						userContext.postNotices = contextResponse[USER_CONTEXT_REQUEST.NOTICES];
						observer.next(userContext);
						observer.complete();
					});
			});
		});
	}

	/**
	 * POST Authenticate user by calling endpoint
	 * @param username: string
	 * @param password: string
	 */
	public login({payload}: Login): Observable<UserContextModel> {
		let params: any = {
			username: payload.username,
			password: payload.password
		};

		if (payload.authority) {
			params.authority = payload.authority;
		}
		return this.http.post(AUTH_API_URLS.SIGN_IN, JSON.stringify(params)).pipe(
			map((result: any) => {
				if (result.userContext && result.userContext.user) {
					return {...result.userContext, notices: result.notices, csrf: result.csrf};
				}
				if (result.error) {
					return result;
				}
				return {};
			})
		);
	}

	/**
	 * GET Logout user by calling api endpoint
	 */
	public logout(): Observable<boolean> {
		localStorage.removeItem(APP_STATE_KEY);
		return this.http.get(AUTH_API_URLS.LOG_OUT).pipe(
			map(() => {
				this.windowService.getWindow().location.href = AUTH_API_URLS.LOGIN_PAGE;
				// this.router.navigate([AUTH_API_URLS.LOGIN_PAGE]);
				return true;
			})
		);
	}

	/**
	 * Verify if the user is already logged or not
	 */
	public isAuthenticated(): boolean {
		const token = this.store.selectSnapshot(TDSAppState);
		return token.userContext && token.userContext.user !== undefined;
	}
}
