import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Observable} from 'rxjs';

/**
 * @name UserService
 */
@Injectable()
export class UserService {

	// private instance variable to hold base url
	private baseURL = '/tdstm';
	private userPreferenceUrl = this.baseURL + '/ws/user';

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient) {
	}

	getStartPageOptions() {
		return this.http.get(`${this.userPreferenceUrl}/startPageOptions`)
			.map((response: any) => {
				let options = response && response.status === 'success' && response.data;
				return options;
			})
			.catch((error: any) => error);
	}
	/**
	 * Used to retrieve all of the model data that will be used by the user preferences component
	 */
	fetchComponentModel() {
		return this.http.get(`${this.userPreferenceUrl}/modelForPreferenceManager`)
			.map((response: any) => {
				let data = response && response.status === 'success' && response.data;
				return data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Used to retrieve all of the model data that will be used by the manage staff component
	 */
	fetchModelForStaffViewEdit(id) {
		return this.http.get(`${this.userPreferenceUrl}/modelForStaffViewEdit/${id}`)
			.map((response: any) => {
				let data = response && response.status === 'success' && response.data;
				return data;
			})
			.catch((error: any) => error);
	}

	getUser() {
		return this.http.get(`${this.userPreferenceUrl}`)
			.map((response: any) => {
				let data = response && response.status === 'success' && response.data;
				return data;
			})
			.catch((error: any) => error);
	}

	removePreference(prefCode) {
		return this.http.delete(`${this.userPreferenceUrl}/preferences/${prefCode}`, null)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	resetPreferences() {
		return this.http.delete(`${this.userPreferenceUrl}/resetPreferences`, null)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Used to save account info from the edit person module (No admin privileges)
	 */
	updateAccount(params) {
		return this.http.post(`${this.userPreferenceUrl}/updateAccount`, params)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Used to save account info from the manage staff module (With admin privileges)
	 */
	updateAccountAdmin(params) {
		return this.http.post(`${this.userPreferenceUrl}/updateAccountAdmin`, params)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	getMapAreas() {
		return this.http.get(`${this.userPreferenceUrl}/mapAreas`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	getTimezones() {
		return this.http.get(`${this.userPreferenceUrl}/timezones`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	saveDateAndTimePreferences(params: any): Observable<any> {
		return this.http.post(`${this.userPreferenceUrl}/saveDateAndTimePreferences`, params)
			.map((response: any) => response)
			.catch((error: any) => error);
	}
}