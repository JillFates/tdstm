import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {BulkChangeType} from '../components/bulk-change/model/bulk-change.model';

@Injectable()
export class BulkChangeService {
	// todo make URL work with a FQPN
	private readonly BASE_URL = '../ws/bulkChange';

	constructor(private http: HttpClient) {}

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
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * PUT - Save/Update  a bulk of asset changes.
	 * @param {any[]} assetIds
	 * @param {edits[]} edits
	 * @returns {Observable<any>}
	 */
	bulkUpdate(assetIds: any[], edits: any[], type: string): Observable<any> {
		const defaultUserParams = { sortDomain: 'device', sortProperty: 'id', filters: {domains: []}};
		const payload = {
			ids: assetIds,
			dataViewId: null,
			edits: edits,
			userParams: defaultUserParams,
			type: type
		};

		return this.http.put(this.getURL(), JSON.stringify(payload))
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => {
				return error;
			});
	}

	/**
	 * Execute a bulk delete operation over assets or dependencies elements
	 * @param {string[]} ids: array of ids to be deleted
	 * @returns {Observable<any>}
	 */
	bulkDelete(actionType: BulkChangeType, ids: string[]): Observable<any> {
		return (actionType === BulkChangeType.Assets) ? this.bulkDeleteAssets(ids) : this.bulkDeleteDependencies(ids);
	}

	/**
	 * Execute a bulk assets deleted
	 * @param {string[]} ids: array of ids assets to be deleted
	 * @returns {Observable<any>}
	 */
	bulkDeleteAssets(ids: string[]): Observable<any> {
		return this.http.post(`../ws/asset/deleteAssets`, JSON.stringify({ids: ids}))
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Execute a bulk dependencies deleted
	 * @param {string[]} ids: array of ids dependencies to be deleted
	 * @returns {Observable<any>}
	 */
	bulkDeleteDependencies(ids: string[]): Observable<any> {
		return this.http.post(`/tdstm/wsAsset/bulkDeleteDependencies`, JSON.stringify({dependencies: ids}))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * GET - List of edit actions
	 * @returns {Observable<any>}
	 */
	getActions(): Observable<any[]> {
		return this.http.get(this.getURL('actions'))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	getAssetListOptions(assetClass: string): Observable<any> {
		return this.http.get(`../ws/asset/defaultCreateModel/${assetClass}`)
			.map((response: any) => response)
			.catch((error: any) => error);
	}
}