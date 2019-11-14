import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {IArchitectureGraphAsset, IAssetType} from '../model/architecture-graph-asset.model';
import {ArchitectreGraphAssetPreference} from '../model/architectre-graph-asset-preference.model';

@Injectable({
	providedIn: 'root'
})
export class ArchitectureGraphService {
	private baseURL = '/tdstm';
	private readonly ASSET_DETAILS = `${ this.baseURL }/ws/architectureGraph`;

	constructor(
		private http: HttpClient
	) {
		// TODO
	}

	/**
	 * asset details endpoint
	 */
	getAssetDetails(id: any, levelsUp?: number, levelsDown?: number, mode?: string): Observable<IArchitectureGraphAsset> {
		const params = new HttpParams()
			.set('assetId', id)
			.set('levelsUp', `${levelsUp || 0}`)
			.set('levelsDown', `${levelsDown || 3}`)
			.set('mode', mode || 'assetId');

		return this.http.get<IArchitectureGraphAsset>(this.ASSET_DETAILS, {
			params,
			observe: 'response'
		})
			.map(res => res.body);
	}

	/**
	 * asset details endpoint
	 */
	getPreferences(id: any, levelsUp?: number, levelsDown?: number): Observable<ArchitectreGraphAssetPreference> {
		const params = new HttpParams()
			.set('assetId', id)
			.set('levelsUp', `${levelsUp || 0}`)
			.set('levelsDown', `${levelsDown || 3}`)

		return this.http.get<ArchitectreGraphAssetPreference>(this.ASSET_DETAILS, {
			params,
			observe: 'response'
		})
			.map(res => res.body);
	}

	/**
	 * asset details endpoint
	 */
	getLegend(): Observable<IAssetType> {

		return this.http.get<IAssetType>(this.ASSET_DETAILS, { observe: 'response'})
			.map(res => res.body);
	}
}
