import {Injectable} from '@angular/core';
import {Response, RequestOptions, Headers} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Observable} from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

/**
 * @name ReportsService
 */
@Injectable()
export class ReportsService {

	// private instance variable to hold base url
	private baseURL = '/tdstm';

	// Resolve HTTP using the constructor
	constructor(private http: HttpInterceptor) {
	}

	/**
	 *
	 * Get events list
	 * @returns {Observable<any>}
	 */
	getEvents(): Observable<any[]> {
		return this.http.get(`${this.baseURL}/ws/moveEvent/list`)
			.map((res: Response) => {
				let response = res.json();
				return response && response.data || [];

			})
			.catch((error: any) => error.json());
	}

	/**
	 * Get the default values for the events ui
	 */
	getDefaults(): Observable<any> {
		return this.http.get(`${this.baseURL}/ws/task/taskCreateDefaults`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}
}