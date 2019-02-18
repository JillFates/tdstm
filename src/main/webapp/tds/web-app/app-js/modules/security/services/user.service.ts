/**
 * User Service is a top level service to retrieve information of the current user logged
 * For more specific endpoint, changes should be done on its specific module instead
 */
import {Injectable} from '@angular/core';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Observable} from 'rxjs';
import {Response} from '@angular/http';

@Injectable()
export class UserService {

	private userUrl = '../ws/user';
	private licenseUrl = '../ws';

	constructor(private http: HttpInterceptor) {
	}

	getUserContext(): Observable<any> {
		return this.http.get(`${this.userUrl}/context`)
			.map((res: Response) => {
				let result = res.json();
				if (result && result.status === 'success') {
					return result.data;
				}
			})
			.catch((error: any) => error.json());
	}

	getLicenseInfo(): Observable<any> {
		return this.http.get(`${this.licenseUrl}/license/info`)
			.map((res: Response) => {
				let result = res.json();
				if (result && result.status === 'success') {
					return result.data;
				}
			})
			.catch((error: any) => error.json());
	}
}