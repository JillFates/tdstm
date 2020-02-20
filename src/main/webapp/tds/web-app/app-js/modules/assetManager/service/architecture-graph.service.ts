import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {IArchitectureGraphAsset, IAssetType} from '../../assetExplorer/model/architecture-graph-asset.model';
import {ArchitectreGraphAssetPreference} from '../../assetExplorer/model/architectre-graph-asset-preference.model';
import {ComboBoxSearchModel} from '../../../shared/components/combo-box/model/combobox-search-param.model';
import {ComboBoxSearchResultModel} from '../../../shared/components/combo-box/model/combobox-search-result.model';

@Injectable({
	providedIn: 'root'
})
export class ArchitectureGraphService {
	private baseURL = '/tdstm/ws/architectureGraph';
	private readonly ASSET_DETAILS = `${ this.baseURL }/`;
	private readonly GRAPH_PREFERENCES = `${this.baseURL}/preferences/`;
	private readonly ARCHITECTURE_GRAPH_DATA = `${this.baseURL}`;
	private readonly ARCHITECTURE_GRAPH_LEGEND = `${this.baseURL}/legend/`;
	private readonly  ARCHITECTURE_ASSET_SELECT = '/tdstm/assetEntity/assetListForSelect2';

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
			.set('levelsDown', `${levelsDown || 3}`);

		return this.http.get<ArchitectreGraphAssetPreference>(this.ASSET_DETAILS, {
			params,
			observe: 'response'
		})
			.map(res => res.body);
	}

	getArchitectureGraphPreferences(): Observable<any> {
		return this.http.get(`${this.GRAPH_PREFERENCES}`)
			.map( (response: any) => {
				return response || [];
			})
			.catch( (error: any) => error);
	}

	/**
	 * asset details endpoint
	 */
	getLegend(): Observable<IAssetType> {

		return this.http.get<IAssetType>(this.ASSET_DETAILS, { observe: 'response'})
			.map(res => res.body);
	}

	/**
	 * Get events list
	 * @returns {Observable<any>}
	 * TODO: @sam please use the previously already implemented getEvents() from task.service.ts.
	 */
	getArchitectureGraphData(assetId: number, levelsUp: number, levelsDown: number, mode: string): Observable<any> {
		let params;
		params = new HttpParams()
			.set('assetId', String(assetId))
			.set('levelsUp', String(levelsUp))
			.set('levelsDown', String(levelsDown))
			.set('mode', mode);
		return this.http.get(`${this.ARCHITECTURE_GRAPH_DATA}`, {params})
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}

	// todo promote to share
	getAssetsForArchitectureGraph(searchParams: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		return this.http.get(`${ this.ARCHITECTURE_ASSET_SELECT}?q=${ searchParams.query }
		&value=${ searchParams.value || '' }
		&max=${ searchParams.maxPage }
		&page=${ searchParams.currentPage }
		&assetClassOption=${ searchParams.metaParam }`)
			.map((response: any) => {
				let comboBoxSearchResultModel: ComboBoxSearchResultModel = {
					result: response.results,
					total: response.total,
					page: response.page
				};
				return comboBoxSearchResultModel;
			})
			.catch((error: any) => error);
	}
}
