import {Injectable} from '@angular/core';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Observable} from 'rxjs';
import {Response} from '@angular/http';
import {UserPreferencesModel} from '../model/user-preferences.model';

/**
 * @name UserService
 */
@Injectable()
export class UserService {

	// private instance variable to hold base url
	private baseURL = '/tdstm';
	private userPreferenceUrl = '../ws/user';

	// Resolve HTTP using the constructor
	constructor(private http: HttpInterceptor) {
	}

	getUserPreferences(): Observable<UserPreferencesModel[]> {
		return this.http.get(`${this.userPreferenceUrl}/preferences`)
			.map((res: Response) => {
				let result = res.json();
				let providerModels = result && result.status === 'success' && result.data;
				return providerModels;
			})
			.catch((error: any) => error.json());
	}

	getUserName() {
		return this.http.get(`${this.userPreferenceUrl}/person`)
			.map((res: Response) => {
				let result = res.json();
				let person = result && result.status === 'success' && result.data;
				return person;
			})
			.catch((error: any) => error.json());
	}

	removePreference(prefCode) {
		return this.http.post(`${this.userPreferenceUrl}/removePreference/${prefCode}`,null)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	resetPreferences() {
		return this.http.post(`${this.userPreferenceUrl}/resetPreferences`,null)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}
}