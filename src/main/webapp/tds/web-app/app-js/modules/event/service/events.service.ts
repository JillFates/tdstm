import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {catchError, map} from 'rxjs/operators';

/**
 * @name EventsService
 */
@Injectable()
export class EventsService {

	// private instance variable to hold base url
	private readonly baseURL = '/tdstm/ws';
	private readonly APP_EVENT_LISTS_URL = `${this.baseURL}/moveEvent/list`;

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient) {
	}

	/**
	 * Get events list
	 * @returns {Observable<any>}
	 * TODO: @sam please use the previously already implemented getEvents() from task.service.ts.
	 */
	getEvents(): Observable<any[]> {
		return this.http.get(`${this.APP_EVENT_LISTS_URL}`)
			.map((response: any) => {
				return response && response.data || [];
			})
			.catch((error: any) => error);
	}
}
