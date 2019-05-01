import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import { ApplicationConflict } from '../model/application-conflicts.model';
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
	private readonly APP_EVENT_RESULTS_LISTS_URL = `${this.baseURL}/reports/generateApplicationMigration/{id}`;
	private readonly APP_EVENT_RESULTS_REPORT_URL = `${this.APP_EVENT_RESULTS_LISTS_URL}`;

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
	 * @param moveBundleId
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

	getApplicationConflicts(
		moveBundle: string,
		appOwner: string,
		bundleConflicts: boolean,
		missingDependencies: boolean,
		unresolvedDependencies: boolean,
		maxAssets: number
		): Observable<Array<ApplicationConflict>> {
		return this.http.get(`${this.baseURL}/reports/applicationConflicts?moveBundle=${moveBundle}&appOwner=${appOwner}&bundleConflicts=${bundleConflicts}&missingDependencies=${missingDependencies}&unresolvedDependencies=${unresolvedDependencies}&maxAssets=${maxAssets}`)
		.map((response: any) => {
			console.log('The response is:');
			console.log(response);
			return response && response.status === 'success' && response.data;
		})
		.catch((error: any) => error);

		/*
		const data =  [
			{
				'application': {
					'id': 1,
					'name': 'Enterprise App with new Buttons - by CN',
				},
				'bundle': {
					'id': 2,
					'name': '123110_qae2e'
				},
				'supports': [
					{
						'type': 'Backup',
						'class': 'Application',
						'name': 'BlackBerry Enterprise Server Test',
						'frequency': 'Unknow',
						'bundle': 'M1 - Physical',
						'status': 'Unknown'
					},
					{
						'type': 'Backup',
						'class': 'Storage',
						'name': 'Storage Enterprise Device - by CN',
						'frequency': 'Unknow',
						'bundle': '123110_qae2e',
						'status': 'Unknown'
					}
				],
				'dependencies': []
			},
			{
				'application': {
					'id': 2,
					'name': 'testing tm 12077 app',
				},
				'bundle': {
					'id': 2,
					'name': '123110_qae2e'
				},
				'supports': [
					{
						'type': 'DB',
						'class': 'Storage',
						'name': 'DS4243-1',
						'frequency': 'constant',
						'bundle': 'TBD',
						'status': 'Unknown'
					}
				],
				'dependencies': [
					{
						'type': 'Hosts',
						'class': 'VM',
						'name': 'ACME-WB-84',
						'frequency': 'daily',
						'bundle': 'M1-Phy',
						'status': 'Future'
					}
				]
			},
			{
				'application': {
					'id': 3,
					'name': 'Enterprise - by CN',
				},
				'bundle': {
					'id': 2,
					'name': '123110_qae2e'
				},
				'supports': [
					{
						'type': 'Backup',
						'class': 'Application',
						'name': 'BlackBerry Enterprise Server',
						'frequency': 'Unknown',
						'bundle': 'Buildout',
						'status': 'Unknown'
					},
					{
						'type': 'Backup',
						'class': 'Application',
						'name': '13181 - by CN',
						'frequency': 'Unknown',
						'bundle': '123110_qae2e',
						'status': 'Unknown'
					},
					{
						'type': 'Backup',
						'class': 'Database',
						'name': 'Device - by CN',
						'frequency': 'Unknown',
						'bundle': 'e2e test*',
						'status': 'Unknown'
					}
				],
				'dependencies': [
					{
						'type': 'Backup',
						'class': 'PDU',
						'name': 'A8 PDU1 B - by CN',
						'frequency': 'Unknown',
						'bundle': 'Master Bundle',
						'status': 'Unknown'
					},
					{
						'type': 'Backup',
						'class': 'Application',
						'name': '13181 - by CN',
						'frequency': 'Unknown',
						'bundle': '123110_qae2e',
						'status': 'Archived'
					},
					{
						'type': 'Backup',
						'class': 'Application',
						'name': 'Cris Enterprise App',
						'frequency': 'Unknown',
						'bundle': '123110_qae2e',
						'status': 'Archived'
					},
					{
						'type': 'Backup',
						'class': 'Database',
						'name': 'Smoke Enterprise DB - by CN',
						'frequency': 'Unknown',
						'bundle': '123110_qae2e',
						'status': 'Archived'
					}
				]
			}
		];

		return Observable.of(data);
		*/
	}

	getDefaultsApplicationConflicts(): Observable<any> {
		const data = {
			moveBundleList: [{'name': '123110_qae2e', 'id': 5849}, {'name': 'B01', 'id': 5910}, {
				'name': 'B1',
				'id': 5913
			}, {'name': 'Buildout', 'id': 3239}, {
				'name': 'Bundle - Moving Devices from Here to there',
				'id': 5689
			}, {'name': 'Bundle Created - by CN', 'id': 5888}, {
				'name': 'e2e test*',
				'id': 5854
			}, {'name': 'e2e writing test', 'id': 5853}, {
				'name': 'ERP App Environment',
				'id': 3181
			}, {'name': 'FooBundle', 'id': 5887}, {
				'name': 'inconsistency bundle',
				'id': 5864
			}, {'name': 'M1 - Physical', 'id': 3174}, {'name': 'M1-Phy', 'id': 5779}, {
				'name': 'M2-Hybrid',
				'id': 3180
			}, {'name': 'Master Bundle', 'id': 2466}, {
				'name': 'New Bundle',
				'id': 5881
			}, {'name': 'NON planning bundle 10261', 'id': 5742}, {
				'name': 'Not Moving',
				'id': 3179
			}, {'name': 'Planning - by CN', 'id': 5837}, {
				'name': 'planning bundle 10261',
				'id': 5741
			}, {'name': 'QAE2E IwV5q Planning', 'id': 5907}, {
				'name': 'qae2e tst',
				'id': 5855
			}, {'name': 'QAE2E UQsv0 NON-Planning', 'id': 5906}, {
				'name': 'QAE2E VwYQG Planning Edited',
				'id': 5908
			}, {'name': 'Retired', 'id': 3178}, {'name': 'Retiring', 'id': 3177}, {
				'name': 'TBD',
				'id': 5657
			}, {'name': 'Wave1- Bundle1', 'id': 3661}
			],
			appOwnerList: [
				{id: '5711', name: 'Varo'},
				{id: '5893', name: 'Blocktest'},
				{id: '5662', name: 'Admin, TDS'}
			]
		};

		return Observable.of(data);
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
	 * Get the default bundles
	 */
	getBundles(): Observable<any> {
		return this.http.get(`${this.baseURL}/reports/moveBundles`)
			.map((response: any) => {
				console.log('The response is:');
				console.log(response);
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the default bundles
	 */
	getOwnersByBundle(moveBundleId: string): Observable<any> {
		return this.http.get(`${this.baseURL}/reports/appOwnersForBundle/${moveBundleId}`)
			.map((response: any) => {
				console.log('The response is:');
				console.log(response);
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}


}
