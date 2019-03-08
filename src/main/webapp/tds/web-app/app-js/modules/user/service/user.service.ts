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

	fetchModelForUserDashboard(projId = '') {
		return this.http.get(`${this.userPreferenceUrl}/modelForUserDashboard/${projId}`)
			.map((response: any) => {
				let data = response && response.status === 'success' && response.data;
				return data;
			})
			.catch((error: any) => error);
	}

	getAssignedEvents() {
		return this.http.get(`${this.userPreferenceUrl}/assignedEvents`)
			.map((response: any) => {
				let data = response && response.status === 'success' && response.data;
				return data;
			})
			.catch((error: any) => error);
	}

	getAssignedEventNews() {
		return this.http.get(`${this.userPreferenceUrl}/assignedEventNews`)
			.map((response: any) => {
				let data = response && response.status === 'success' && response.data;
				return data;
			})
			.catch((error: any) => error);
	}

	getAssignedTasks() {
		return this.http.get(`${this.userPreferenceUrl}/assignedTasks`)
			.map((response: any) => {
				let data = response && response.status === 'success' && response.data;
				return data;
			})
			.catch((error: any) => error);
	}

	getAssignedApplications() {
		return this.http.get(`${this.userPreferenceUrl}/assignedApplications`)
			.map((response: any) => {
				let data = response && response.status === 'success' && response.data;
				return data;
			})
			.catch((error: any) => error);
	}

	getAssignedPeople() {
		return this.http.get(`${this.userPreferenceUrl}/assignedPeople`)
			.map((response: any) => {
				let data = response && response.status === 'success' && response.data;
				return data;
			})
			.catch((error: any) => error);
	}

}