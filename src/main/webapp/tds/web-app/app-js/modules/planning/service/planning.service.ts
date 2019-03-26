import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

import {Observable} from 'rxjs';

@Injectable()
export class PlanningService {
	// private instance variable to hold base url
	private baseURL = '/tdstm';
	private dashboardUrl = this.baseURL + '/ws/dashboard';
	private userPreferenceUrl = this.baseURL + '/ws/user';

	constructor(private http: HttpClient) {}

	fetchModelForPlanningDashboard() {
		return this.http.get(`${this.userPreferenceUrl}/getPlanningStats`)
			.map((response: any) => {
				let data = response && response.status === 'success' && response.data;
				return data;
			})
			.catch((error: any) => error);
	}
}