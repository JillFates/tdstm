import {Injectable, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

import {PREFERENCES_LIST, PreferenceService} from '../../../shared/services/preference.service';

/**
 * @name InsightService
 */
@Injectable()
export class InsightService {
	private readonly baseURL = '/tdstm';
	private readonly APP_EVENT_STATUS_DATA = `${this.baseURL}/ws/dashboard/insight`;

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient, private preferenceService: PreferenceService) {
		this.preferenceService.getPreference(PREFERENCES_LIST.CURR_TZ).subscribe();
	}

	/**
	 * Get the data for insight data for dashboard
	 * @param {number} max
	 * @param {number} lowRange Bundle id
	 * @param {number} highRange Bundle id
	 * @returns {Observable<any>} Event insight data
	 */
	getInsightData(max?: number, lowRange?: number, highRange?: number): Observable<any> {
		return this.http.get(`${this.APP_EVENT_STATUS_DATA}`)
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}

}
