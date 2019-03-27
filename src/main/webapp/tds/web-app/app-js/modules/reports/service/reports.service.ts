import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {catchError, map} from 'rxjs/operators';

/**
 * @name ReportsService
 */
@Injectable()
export class ReportsService {

	// private instance variable to hold base url
	private readonly baseURL = '/tdstm';
	private readonly EVENT_LIST_URL = `${this.baseURL}/ws/event`;

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient) {
	}

	/**
	 * Get events list
	 * @returns {Observable<any>}
	 * TODO: @sam please use the previously already implemented getEvents() from task.service.ts.
	 */
	getEvents(): Observable<any[]> {
		return this.http.get(`${this.baseURL}/ws/moveEvent/list`)
			.map((response: any) => {
				return response && response.data || [];

			})
			.catch((error: any) => error);
	}

	getEventList(): Observable<any> {
		return this.http.get(this.EVENT_LIST_URL).pipe(
			catchError( error => {
				console.error(error);
				return error;
			})
		);
	}

	/**
	 *
	 * Get the report prevents checklist
	 * @param {string} eventId Report id to generate
	 * @returns {Observable<any>}
	 */
	getPreventsCheckList(eventId: string): Observable<any> {
		const payload = { moveEvent: eventId };
		return this.http.post(`${this.baseURL}/ws/reports/generateCheckList`, JSON.stringify(payload), {responseType: 'text'})
			.map((response: any) => {
				return response && response || '';

			})
			.catch((error: any) => error);
	}

	/**
	 * Get the default values for the events ui
	 */
	getDefaults(): Observable<any> {
		return this.http.get(`${this.baseURL}/ws/task/taskCreateDefaults`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}
}
