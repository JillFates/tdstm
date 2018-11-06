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
	private userPreferenceUrl = '../ws/user';

	// Resolve HTTP using the constructor
	constructor(private http: HttpInterceptor) {
	}

	getStartPageOptions() {
		return this.http.get(`${this.userPreferenceUrl}/startPageOptions`)
			.map((res: Response) => {
				let result = res.json();
				let options = result && result.status === 'success' && result.data;
				return options;
			})
			.catch((error: any) => error.json());
	}

	getPreference(prefCodes) {
		return this.http.get(`${this.userPreferenceUrl}/preferences/${prefCodes}`)
			.map((res: Response) => {
				let result = res.json();
				let preference = result && result.status === 'success' && result.data;
				return preference;
			})
			.catch((error: any) => error.json());
	}

	getCurrentUser() {
		return this.http.get(`${this.userPreferenceUrl}`)
			.map((res: Response) => {
				let result = res.json();
				let user = result && result.status === 'success' && result.data;
				return user;
			})
			.catch((error: any) => error.json());
	}

	getUserPreferences() {
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
		return this.http.post(`${this.userPreferenceUrl}/removePreference/${prefCode}`, null)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	resetPreferences() {
		return this.http.post(`${this.userPreferenceUrl}/resetPreferences`, null)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	savePreference(pref) {
		return this.http.post(`${this.userPreferenceUrl}/preference/${pref.code}&${pref.value}`, null)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	updateAccount(params) {
		return this.http.post(`../person/updateAccount/${params}`, null)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

}