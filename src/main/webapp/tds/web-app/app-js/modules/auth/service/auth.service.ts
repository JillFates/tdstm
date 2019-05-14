// Angular
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
// Services
import {CookieService} from 'ngx-cookie-service';
// Other
import {BehaviorSubject, Observable} from 'rxjs';
import {map} from 'rxjs/operators';

export enum AUTH_API_URLS {
	SIGN_IN = '/tdstm/auth/signIn',
	LOG_OUT = '/tdstm/auth/signOut'
}

export const SESSION_KEY = 'JSESSIONID';
export const SERVER_URL_KEY = 'SERVERURL';

@Injectable()
export class AuthService {
	private serverURL: string;
	private loggedIn: boolean;
	private loggedInSubject$ = new BehaviorSubject(this.loggedIn);

	constructor(
		private http: HttpClient,
		private cookieService: CookieService) {
	}

	/**
	 * POST Authenticate user by calling endpoint, set the user logged in flag true.
	 * @param realm: string
	 * @param username: string
	 * @param password: string
	 */
	login(realm: string, username: string, password: string): Observable<boolean> {
		const params = new URLSearchParams({
			username: username,
			password: password,
			authority: realm
		});
		return this.http.post(AUTH_API_URLS.SIGN_IN.toString(), params.toString()).pipe(
			map((result: any) => {
				const success = result.status && result.status === 'success';
				this.setLoggedIn(success);
				return success;
			})
		);
	}

	/**
	 * GET Logout user by calling api endpoint, set the user logged in flag false.
	 */
	logout(): Observable<boolean> {
		return this.http.get(AUTH_API_URLS.LOG_OUT.toString()).pipe(
			map(() => {
				this.cookieService.delete(SESSION_KEY);
				this.cookieService.delete(SERVER_URL_KEY);
				this.setLoggedIn(false);
				this.serverURL = null;
				return true;
			})
		);
	}

	/**
	 * Validates if the current user still authenticated.
	 */
	isUserAuthenticated(): boolean {
		if (this.serverURL && this.loggedIn
			&& this.cookieService.check(SESSION_KEY)
			&& this.cookieService.check(SERVER_URL_KEY)) {
			return true;
		}
		return false;
	}

	/**
	 * Set User as Logged In
	 */
	private setLoggedIn(value: boolean): void {
		// Update login status subject
		this.loggedInSubject$.next(value);
		this.loggedIn = value;
	}

	/**
	 * Set the servers url which will be used among all the API calls.
	 * @param url: string
	 */
	setServerURL(url: string): void {
		this.serverURL = url;
		this.cookieService.set(SERVER_URL_KEY, url);
	}

	/**
	 * Returns api servers url.
	 */
	getServerURL(): string {
		if (this.serverURL) {
			return this.serverURL;
		} else if (this.cookieService.check(SERVER_URL_KEY)) {
			return this.cookieService.get(SERVER_URL_KEY);
		} else {
			return null;
		}
	}
}