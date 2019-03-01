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

	getStartPageOptions() {
		return this.http.get(`${this.userPreferenceUrl}/startPageOptions`)
			.map((res: Response) => {
				let result = res.json();
				let options = result && result.status === 'success' && result.data;
				return options;
			})
			.catch((error: any) => error.json());
	}
	/**
	 * Used to retrieve all of the model data that will be used by the user preferences component
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

	fetchModelForUserDashboard(projId = '') {
		return this.http.get(`${this.userPreferenceUrl}/modelForUserDashboard/${projId}`)
			.map((res: Response) => {
				let result = res.json();
				let data = result && result.status === 'success' && result.data;
				return data;
			})
			.catch((error: any) => error.json());
	}

	getAssignedEvents() {
		return this.http.get(`${this.userPreferenceUrl}/assignedEvents`)
			.map((res: Response) => {
				let result = res.json();
				let data = result && result.status === 'success' && result.data;
				return data;
			})
			.catch((error: any) => error.json());
	}

	getAssignedEventNews() {
		return this.http.get(`${this.userPreferenceUrl}/assignedEventNews`)
			.map((res: Response) => {
				let result = res.json();
				let data = result && result.status === 'success' && result.data;
				return data;
			})
			.catch((error: any) => error.json());
	}

	getAssignedTasks() {
		return this.http.get(`${this.userPreferenceUrl}/assignedTasks`)
			.map((res: Response) => {
				let result = res.json();
				let data = result && result.status === 'success' && result.data;
				return data;
			})
			.catch((error: any) => error.json());
	}

	getAssignedApplications() {
		return this.http.get(`${this.userPreferenceUrl}/assignedApplications`)
			.map((res: Response) => {
				let result = res.json();
				let data = result && result.status === 'success' && result.data;
				return data;
			})
			.catch((error: any) => error.json());
	}

	getAssignedPeople() {
		return this.http.get(`${this.userPreferenceUrl}/assignedPeople`)
			.map((res: Response) => {
				let result = res.json();
				let data = result && result.status === 'success' && result.data;
				return data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Used to retrieve all of the model data that will be used by the manage staff component
	 */
	fetchModelForStaffViewEdit(id) {
		return this.http.get(`${this.userPreferenceUrl}/modelForStaffViewEdit/${id}`)
			.map((res: Response) => {
				let result = res.json();
				let data = result && result.status === 'success' && result.data;
				return data;
			})
			.catch((error: any) => error.json());
	}

	getUser() {
		return this.http.get(`${this.userPreferenceUrl}`)
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

	/**
	 * Used to save account info from the edit person module (No admin privileges)
	 */
	updateAccount(params) {
		return this.http.post(`${this.userPreferenceUrl}/updateAccount`, params)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Used to save account info from the manage staff module (With admin privileges)
	 */
	updateAccountAdmin(params) {
		return this.http.post(`${this.userPreferenceUrl}/updateAccountAdmin`, params)
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
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	getTimezones() {
		return this.http.get(`${this.userPreferenceUrl}/timezones`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	saveDateAndTimePreferences(params: any): Observable<any> {
		return this.http.post(`${this.userPreferenceUrl}/saveDateAndTimePreferences`, params)
			.map((res: Response) => {
				return res.json();
			}).catch((error: any) => error.json());
	}
}