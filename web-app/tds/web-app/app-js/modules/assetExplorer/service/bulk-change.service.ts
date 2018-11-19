import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {BulkChangeType} from '../components/bulk-change/model/bulk-change.model';

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
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
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
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Execute a bulk dependencies deleted
	 * @param {string[]} ids: array of ids dependencies to be deleted
	 * @returns {Observable<any>}
	 */
	bulkDeleteDependencies(ids: string[]): Observable<any> {
		return this.http.post(`/tdstm/wsAsset/bulkDeleteDependencies`, JSON.stringify({dependencies: ids}))
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
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

	getAssetListOptions(assetClass: string): Observable<any> {
		return this.http.get(`../ws/asset/defaultCreateModel/${assetClass}`)
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
	}
}