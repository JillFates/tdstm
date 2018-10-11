import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class BulkChangeService {
	// todo make URL work with a FQPN
	private readonly BASE_URL = '../ws/bulkChange';

	constructor(private http: HttpInterceptor) {}

	/**
	 * GET - Build the url passing arg variable segments
	 * @returns {Observable<any>}
	 */
	private getURL(...segments: string[]): string {
		return `${this.BASE_URL}/${segments.join('/')}`;
	}

	/**
	 * GET - List of fields
	 * @returns {Observable<any>}
	 */
	getFields(): Observable<any[]> {
		return this.http.get(this.getURL('fields'))
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
	}

	/**
	 * PUT - Save/Update  a bulk of asset changes.
	 * @param {any[]} assetIds
	 * @param {edits[]} edits
	 * @returns {Observable<any>}
	 */
	bulkUpdate(assetIds: any[], edits: any[]): Observable<any> {
		const defaultUserParams = { sortDomain: 'device', sortProperty: 'id', filters: {domains: []}};
		// const defaultParams = { userParams: defaultUserParams, dataViewId: null, assetIds: [], edits: []};
		// const payload = Object.assign({}, defaultParams, {assetIds, edits});
		const payload = {
			ids: assetIds,
			dataViewId: null,
			edits: edits,
			userParams: defaultUserParams
		}

		return this.http.put(this.getURL(), JSON.stringify(payload))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => {
				return error;
			});
	}

	/**
	 * GET - List of edit actions
	 * @returns {Observable<any>}
	 */
	getActions(): Observable<any[]> {
		return this.http.get(this.getURL('actions'))
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
	}
}