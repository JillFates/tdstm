import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import { ApplicationConflict, DatabaseConflict } from '../model/application-conflicts.model';
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
	private readonly SME_LIST_URL = `${this.baseURL}/reports/smeList/{id}`;
	private readonly APP_OWNER_LIST_URL = `${this.baseURL}/reports/appOwnerList/{id}`;
	private readonly APP_EVENT_RESULTS_LISTS_URL = `${this.baseURL}/reports/generateApplicationMigration/{id}`;
	private readonly APP_EVENT_RESULTS_REPORT_URL = `${this.APP_EVENT_RESULTS_LISTS_URL}`;
	private readonly APPLICATION_PROFILES_REPORT_URL = `${this.baseURL}/reports/generateApplicationProfiles`;
	private readonly PROJECT_METRICS_LISTS_URL = `${this.baseURL}/reports/projectMetricsLists`;
	private readonly PROJECT_METRICS_REPORT_URL = `${this.baseURL}/reports/generateProjectMetrics`;

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
	 * GET - Get move bundle list
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
	 * GET - Return the list of SME that belongs to a move bundle.
	 * @param moveBundleId: number
	 */
	getSmeList(moveBundleId: number): Observable<any> {
		return this.http.get(this.SME_LIST_URL.replace('{id}', moveBundleId.toString())).pipe(
			map( (response: any) => response && response.data || []),
			catchError(error => {
				console.error(error);
				return error;
			})
		)
	}

	/**
	 * GET - Return the list of App Owner that belongs to a move bundle.
	 * @param moveBundleId: number
	 */
	getAppOwnerList(moveBundleId: number): Observable<any> {
		return this.http.get(this.APP_OWNER_LIST_URL.replace('{id}', moveBundleId.toString())).pipe(
			map( (response: any) => response && response.data || []),
			catchError(error => {
				console.error(error);
				return error;
			})
		)
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
			return this.http.post(this.TASKS_REPORT_URL, request, {observe: 'response', responseType: 'blob'}).pipe(
				map((result: any) => {
					let filename: string = result.headers.get('content-disposition');
					filename = filename.replace('attachment; filename=', '');
					filename = filename.replace(new RegExp('"', 'g'), '');
					return {
						blob: new Blob([result.body], { type: result.body.type }),
						filename: filename
					}
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

	/**
	 * GET - Return the list of SME that belongs to a move bundle.
	 * @param moveBundleId
	*/
	getApplicationEventReportLists(moveBundleId: number): Observable<any> {
		return this.http.get(this.APP_EVENT_RESULTS_LISTS_URL.replace('{id}', moveBundleId.toString())).pipe(
			map( (response: any) => response && response.data || null),
			catchError(error => {
				console.error(error);
				return error;
			})
		)
	}

	/**
	 * POST - Return the list of SME that belongs to a move bundle.
	 * @param moveBundleId
	 */
	getApplicationEventReport(
		moveBundle: number,
		sme: number,
		startCategory: string,
		stopCategory: string,
		testing: number,
		outageWindow: string): Observable<any> {
		const request = {
			moveBundle: moveBundle,
			sme: sme === -1 ? 'null' : sme,
			startCategory: startCategory,
			stopCategory: stopCategory,
			outageWindow: outageWindow,
			testing: testing
		};
		return this.http.post(
			this.APP_EVENT_RESULTS_REPORT_URL.replace('{id}', moveBundle.toString()),
			request,
			{responseType: 'text'}).pipe(
			catchError(error => {
				console.error(error);
				return error;
			})
		)
	}

	/**
	 * GET - Return the list of default bundles
	 */
	getBundles(): Observable<any> {
		return this.http.get(`${this.baseURL}/reports/moveBundlesForSelection`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * GET - Return the options lists of Project Metrics Report.
	 */
	getProjectMetricsLists(): Observable<any> {
		return this.http.get(`${this.PROJECT_METRICS_LISTS_URL}`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * GET - Return the owners filtered by bundle
	 * @param {string} moveBundleId Bundle id to filter
	 */
	getOwnersByBundle(moveBundleId: string): Observable<any> {
		return this.http.get(`${this.baseURL}/reports/appOwnersForBundle/${moveBundleId}`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * GET - Return the list of application conflicts
	 * @param bundle: string
	 * @param owner: string
	 * @param conflicts: boolean
	 * @param missing: boolean
	 * @param unresolved: boolean
	 * @param max: number
	*/
	getApplicationConflicts(
		bundle: string,
		owner: string,
		conflicts: boolean,
		missing: boolean,
		unresolved: boolean,
		max: number,
		moveBundleList: any[]
		): Observable<Array<ApplicationConflict>> {
			const url = `${this.baseURL}/reports/applicationConflicts?`;
			const params = `moveBundle=${bundle}&appOwner=${owner}&bundleConflicts=${conflicts}` +
			`&missingDependencies=${missing}&unresolvedDependencies=${unresolved}&maxAssets=${max}`;

			return this.http.get(`${url}${params}`)
			.map((response: any) => {
				const data =  (response && response.status === 'success' && response.data || null);
				return data;

				/*
				return data == null ? [] : data.appList
					.map((appItem: any) => {
						let bundleName = data.moveBundle.name;
						if (!bundleName) {
							let currentBundle = moveBundleList.find((current) => current.id === appItem.app.moveBundle.id.toString());
							bundleName = (currentBundle) ? currentBundle.name : '';
						}

						return {
							'application': {
								'id': appItem.app.id,
								'name': appItem.app.assetName,
								'assetClass': appItem.app.assetClass.name
							},
							'bundle': {
								'id': data.moveBundle.id,
								'name': bundleName
							},
							supports: appItem.supportsList
								.map((support: any) => {
									return {
										'type': support.type,
										'class': support.asset.assetClass,
										'name': support.asset.name,
										'frequency': support.dataFlowFreq,
										'bundle':  support.asset.moveBundle,
										'status': support.status
									};
								}),
							dependencies: appItem.dependsOnList
							.map((dependency: any) => {
								return {
									'type': dependency.type,
									'class': dependency.dependent.assetClass,
									'name': dependency.dependent.name,
									'frequency': dependency.dataFlowFreq,
									'bundle': dependency.dependent.moveBundle,
									'status': dependency.status
								};
							})
						}
					})
					*/
			})
			.catch((error: any) => error);
	}

	/**
	 * POST - Return the list of SME that belongs to a move bundle.
	 * @param moveBundleId
	 */
	generateApplicationProfilesReport(
		moveBundle: number,
		sme: number,
		appOwner: number,
		reportMaxAssets: number): Observable<any> {
		const request = {
			moveBundle: moveBundle,
			sme: sme === -1 ? 'null' : sme,
			appOwner: appOwner === -1 ? 'null' : appOwner,
			reportMaxAssets: reportMaxAssets.toString()
		};
		return this.http.post(
			this.APPLICATION_PROFILES_REPORT_URL,
			request,
			{responseType: 'text'}).pipe(
			catchError(error => {
				console.error(error);
				return error;
			})
		)
	}

	/**
	 * POST - Returns the excel spreadsheet file.
	 * @param projectIds: Array<string>
	 * @param startDate: Date
	 * @param endDate: Date
	 * @param includeNonPlanning: boolean
	 */
	generateActivityMetricsReport(projectIds: Array<string>, startDate: Date, endDate: Date, includeNonPlanning: boolean): Observable<any> {
		if (projectIds.length === 1 && projectIds[0] === '-1') {
			projectIds = ['all'];
		}
		const request = {
			projectIds: projectIds,
			startDate: `${startDate.getMonth() + 1}/${startDate.getDate()}/${startDate.getFullYear()}`,
			endDate: `${endDate.getMonth() + 1}/${endDate.getDate()}/${endDate.getFullYear()}`,
			includeNonPlanning: includeNonPlanning
		};
		return this.http.post(this.PROJECT_METRICS_REPORT_URL, request, {observe: 'response', responseType: 'blob'}).pipe(
			map((result: any) => {
				let filename: string = result.headers.get('content-disposition');
				filename = filename.replace('attachment; filename=', '');
				filename = filename.replace(new RegExp('"', 'g'), '');
				return {
					blob: new Blob([result.body], { type: result.body.type }),
					filename: filename
				}
			}),
			catchError(error => {
				console.error(error);
				return error;
			})
		);
	}

	/**
	 * GET - Return the list of database conflicts
	 * @param {string} bundle: Bundle ID related
	 * @param {boolean} conflicts: Flag to get references to assets assigned to unrelated bundles
	 * @param {boolean} missing: Flag to get missing applications
	 * @param {boolean} unresolved: Flag to get dependencies with status Unknown or Questioned
	 * @param {boolean} unsupported: Flag to get having no Requires dependency indication where database resides
	 * @param {number} max: Max number of records to retrive
	*/
	getDatabaseConflicts(
		bundle: string,
		conflicts: boolean,
		missing: boolean,
		unresolved: boolean,
		unsupported: boolean,
		max: number,
		): Observable<any> {
			const url = `${this.baseURL}/reports/databaseConflicts?`;
			const params = `moveBundle=${bundle}&bundleConflicts=${conflicts}` +
			`&missingApplications=${missing}&unresolvedDependencies=${unresolved}&unsupportedDependencies=${unsupported}&maxAssets=${max}`;

			return this.http.get(`${url}${params}`)
				.map((response: any) => {
					const data =  (response && response.status === 'success' && response.data || null);
					return data;
				})
				.catch((error: any) => error);
		}
}
