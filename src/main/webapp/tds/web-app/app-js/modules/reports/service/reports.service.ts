import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
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
	constructor(private http: HttpClient) {
	}

	/**
	 * Get events list
	 * @returns {Observable<any>}
	 */
	getEvents(): Observable<any[]> {
		return this.http.get(`${this.baseURL}/ws/moveEvent/list`)
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
}