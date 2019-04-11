import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {catchError, map} from 'rxjs/operators';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';

/**
 * @name ReportsService
 */
@Injectable()
export class ReportsService {

	// private instance variable to hold base url
	private readonly baseURL = '/tdstm/ws';
	private readonly EVENT_LIST_URL = `${this.baseURL}/event`;
	private readonly MOVE_BUNDLES_LIST_URL = `${this.baseURL}/reports/moveBundles`;
	private readonly TASKS_REPORT_URL = `${this.baseURL}/reports/tasksReport`;
	private readonly SERVER_CONFLICTS_REPORT_URL = `${this.baseURL}/reports/generateServerConflicts`;

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient, private sanitizer: DomSanitizer) {
	}

	getSafeHtml(content: string): SafeHtml {
		return this.sanitizer.bypassSecurityTrustHtml(content);
	}

	/**
	 * Get events list
	 * @returns {Observable<any>}
	 * TODO: @sam please use the previously already implemented getEvents() from task.service.ts.
	 */
	getEvents(): Observable<any[]> {
		return this.http.get(`${this.baseURL}/moveEvent/list`)
			.map((response: any) => {
				return response && response.data || [];

			})
			.catch((error: any) => error);
	}

	/**
	 * Get move bundle list
	 * @returns {Observable<any>}
	 */
	getMoveBundles(): Observable<any[]> {
		return this.http.get(this.MOVE_BUNDLES_LIST_URL)
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
	 * POST - Retrieve task report from server.
	 */
	getTaskReport(
		events: Array<string>,
		reportType: 'Generate Web' | 'Generate Xls',
		includeComments = false,
		includeOnlyRemainingTasks = false,
		includeUnpublishedTasks = false): Observable<any> {
		const request = {
			moveEvent: events,
			_action_tasksReport: reportType,
			wUnresolved: includeOnlyRemainingTasks,
			viewUnpublished: includeUnpublishedTasks,
			wComment: includeComments
		}
		if (reportType === 'Generate Web') {
			return this.http.post(this.TASKS_REPORT_URL, request, {responseType: 'text'}).pipe(
				catchError(error => {
					console.error(error);
					return error;
				})
			);
		} else {
			return this.http.post(this.TASKS_REPORT_URL, request, {responseType: 'blob'}).pipe(
				map(result => {
					return new Blob([result], { type: 'application/vnd.ms-excel' });
				}),
				catchError(error => {
					console.error(error);
					return error;
				})
			);
		}
	}

	/**
	 *
	 * Get the report prevents checklist
	 * @param {string} eventId Report id to generate
	 * @returns {Observable<any>}
	 */
	getPreventsCheckList(eventId: string): Observable<any> {
		const payload = { moveEvent: eventId };
		return this.http.post(`${this.baseURL}/reports/generateCheckList`, JSON.stringify(payload), {responseType: 'text'})
			.map((response: any) => {
				return response && response || '';

			})
			.catch((error: any) => error);
	}

	/**
	 * Get the default values for the events ui
	 */
	getDefaults(): Observable<any> {
		return this.http.get(`${this.baseURL}/task/taskCreateDefaults`)
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
		return this.http.post(this.SERVER_CONFLICTS_REPORT_URL, request, {responseType: 'text'}).pipe(
			catchError(error => {
				console.error(error);
				return error
			})
		);
	}
}
