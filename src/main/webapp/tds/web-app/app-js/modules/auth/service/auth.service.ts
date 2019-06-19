// Angular
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
// Other
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {Login} from '../action/login.actions';
import {UserContextModel} from '../model/user-context.model';
import {Store} from '@ngxs/store';
import {TDSAppState} from '../../../app/state/tds-app.state';

export enum AUTH_API_URLS {
	SIGN_IN = '/tdstm/auth/signIn',
	LOG_OUT = '/tdstm/auth/signOut'
}

@Injectable()
export class AuthService {

	constructor(
		private http: HttpClient,
		private store: Store) {
	}

	/**
	 * POST Authenticate user by calling endpoint
	 * @param username: string
	 * @param password: string
	 */
	public login({payload}: Login): Observable<UserContextModel> {
		const params = {
			username: payload.username,
			password: payload.password
		};
		return this.http.post(AUTH_API_URLS.SIGN_IN.toString(), JSON.stringify(params)).pipe(
			map((result: any) => {
				if (result.userContext && result.userContext.user) {
					return {...result.userContext, notices: result.notices};
				}
				return {};
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
