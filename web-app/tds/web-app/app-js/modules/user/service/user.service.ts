import {Injectable} from '@angular/core';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Observable} from 'rxjs';
import {Response} from '@angular/http';

/**
 * @name UserService
 */
@Injectable()
export class UserService {

	// private instance variable to hold base url
	private baseURL = '/tdstm';
	private userPreferenceUrl = this.baseURL + '/ws/user';

	// Resolve HTTP using the constructor
	constructor(private http: HttpInterceptor) {
	}

	/**
	 * Used to retrieve all of the model data that will be used by the component
	 */
	fetchComponentModel() {
		return this.http.get(`${this.userPreferenceUrl}/modelForPreferenceManager`)
			.map((res: Response) => {
				let result = res.json();
				let data = result && result.status === 'success' && result.data;
				return data;
			})
			.catch((error: any) => error.json());
	}

	removePreference(prefCode) {
		return this.http.delete(`${this.userPreferenceUrl}/preferences/${prefCode}`, null)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	resetPreferences() {
		return this.http.delete(`${this.userPreferenceUrl}/resetPreferences`, null)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	getMapAreas() {
		return this.http.get(`${this.userPreferenceUrl}/mapAreas`)
			.map((res: Response) => {
				let result = res.json();
				let mapAreas = result && result.status === 'success' && result.data;
				return mapAreas;
			})
			.catch((error: any) => error.json());
	}
}