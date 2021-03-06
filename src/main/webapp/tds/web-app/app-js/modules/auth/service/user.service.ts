/**
 * User Service is a top level service to retrieve information of the current user logged
 * For more specific endpoint, changes should be done on its specific module instead
 */
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import { ProjectService } from '../../project/service/project.service';

@Injectable()
export class UserService {

	private userUrl = '../ws/user';
	private licenseUrl = '../ws';

	constructor(private http: HttpClient) {	}

	getUserContext(): Observable<any> {
		return this.http.get(`${this.userUrl}/context`)
			.map((response: any) => {
				if (response && response.status === 'success') {
					return response.data;
				}
			}).catch((error: any) => error);
	}

	/**
	 * Get only the license applied to the user
	 * That helps to show or hide different sections of the app
	 */
	getLicense(): Observable<any> {
		return this.http.get(`${this.licenseUrl}/license/info`)
			.map((response: any) => {
				if (response && response.status === 'success') {
					return response.data.license;
				}
				return {};
			}).catch((error: any) => error);
	}
}
