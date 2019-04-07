import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {catchError} from 'rxjs/operators';

/**
 * @name ReportsService
 */
@Injectable()
export class ReportsService {

	// private instance variable to hold base url
	private baseURL = '/tdstm';

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient) {
	}

	/**
	 * Get events list
	 * @returns {Observable<any>}
	 */
	getEvents(): Observable<any> {
		return this.http.get(`${this.baseURL}/ws/moveEvent/list`)
			.catch((error: any) => error);
	}

	/**
	 * Get move bundle list
	 * @returns {Observable<any>}
	 */
	getMoveBundles(): Observable<any[]> {
		return this.http.get(`${this.baseURL}/ws/reports/moveBundles`)
			.map((response: any) => {
				return response && response.data || [];
			})
			.catch((error: any) => error);
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

	/**
	 * POST - Generate Server Conflicts report.
	 * @param moveBundleId: number
	 * @param bundleConflict: boolean
	 * @param unresolvedDependencies: boolean
	 * @param noSupportDependencies: boolean
	 * @param noVmHost: boolean
	 * @param maxAssetsToReport: number
	 */
	generateServerConflictsReport(
		moveBundleId: number,
		bundleConflict = false,
		unresolvedDependencies = false,
		noSupportDependencies = false,
		noVmHost = false,
		maxAssetsToReport = 100): Observable<any> {
		const request = {
			moveBundle: moveBundleId,
			bundleConflicts: bundleConflict,
			unresolvedDep: unresolvedDependencies,
			noRuns: noSupportDependencies,
			vmWithNoSupport: noVmHost,
			report_max_assets: maxAssetsToReport
		}
		return this.http.post(`${this.baseURL}/ws/reports/generateServerConflicts`, request, {responseType: 'text'}).pipe(
			catchError(error => {
				console.error(error);
				return error
			})
		);
	}
}
