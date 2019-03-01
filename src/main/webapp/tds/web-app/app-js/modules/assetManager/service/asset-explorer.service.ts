import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ViewModel, ViewGroupModel, ViewType} from '../../assetExplorer/model/view.model';
import {HttpClient} from '@angular/common/http';
import {Permission} from '../../../shared/model/permission.model';
import {ComboBoxSearchModel} from '../../../shared/components/combo-box/model/combobox-search-param.model';
import {ComboBoxSearchResultModel} from '../../../shared/components/combo-box/model/combobox-search-result.model';
import {PermissionService} from '../../../shared/services/permission.service';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class AssetExplorerService {

	private defaultUrl = '../ws';
	private assetExplorerUrl = '../ws/assetExplorer';
	private assetUrl = '../ws/asset';
	private FAVORITES_MAX_SIZE = 10;
	private ALL_ASSETS = 'All Assets';
	private assetEntitySearch = 'assetEntity';

	constructor(private http: HttpClient, private permissionService: PermissionService) {}

	getReports(): Observable<ViewGroupModel[]> {
		return this.http.get(`${this.assetExplorerUrl}/views`)
			.map((response: any) => {
				let reportGroupModel: ViewGroupModel[] = Object.keys(response.data).map(key => {
					return response.data[key];
				});
				let folders = [
					{
						name: 'All',
						items: reportGroupModel,
						open: true,
						type: ViewType.ALL
					}, {
						name: 'Favorites',
						items: reportGroupModel.filter(r => r['isFavorite']),
						open: false,
						type: ViewType.FAVORITES
					}, {
						name: 'My Views',
						items: reportGroupModel.filter(r => r['isOwner']),
						open: false,
						type: ViewType.MY_VIEWS
					}, {
						name: 'Shared Views',
						items: reportGroupModel.filter(r => r['isShared']),
						open: false,
						type: ViewType.SHARED_VIEWS
					}
				];
				if (this.permissionService.hasPermission(Permission.AssetExplorerSystemList)) {
					folders.push({
						name: 'System Views',
						items: reportGroupModel.filter(r => r['isSystem']),
						open: false,
						type: ViewType.SYSTEM_VIEWS
					});
				}
				return folders as any;
			})
			.catch((error: any) => error);
	}

	getReport(id: number): Observable<ViewModel> {
		return this.http.get(`${this.assetExplorerUrl}/view/${id}`)
			.map((response: any) => {
				if (response && response.status === 'success' && response.data) {
					return response.data.dataView;
				} else {
					throw new Error(response.errors.join(';'));
				}
			})
			.catch((error: any) => error);
	}

	saveReport(model: ViewModel): Observable<ViewModel> {
		if (!model.id) {
			return this.http.post(`${this.assetExplorerUrl}/view`, JSON.stringify(model))
				.map((response: any) => {
					return response && response.status === 'success' && response.data && response.data.dataView;
				})
				.catch((error: any) => error);
		} else {
			return this.http.put(`${this.assetExplorerUrl}/view/${model.id}`, JSON.stringify(model))
				.map((response: any) => {
					return response && response.status === 'success' && response.data && response.data.dataView;
				})
				.catch((error: any) => error);
		}
	}

	deleteReport(id: number): Observable<string> {
		return this.http.delete(`${this.assetExplorerUrl}/view/${id}`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data && response.data.status;
			})
			.catch((error: any) => error);
	}

	query(id: number, schema: any): Observable<any[]> {
		return this.http.post(`${this.assetExplorerUrl}/query/${id}`, JSON.stringify(schema))
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	previewQuery(schema: any): Observable<any[]> {
		return this.http.post(`${this.assetExplorerUrl}/previewQuery`, JSON.stringify(schema))
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	saveFavorite(id: number): Observable<any> {
		return this.http.post(`${this.assetExplorerUrl}/favoriteDataview/${id}`, '')
			.map((response: any) => {
				return response && response.status === 'success' && response.data && response.data.status;
			})
			.catch((error: any) => error);
	}

	deleteFavorite(id: number): Observable<any> {
		return this.http.delete(`${this.assetExplorerUrl}/favoriteDataview/${id}`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data && response.data.status;
			})
			.catch((error: any) => error);
	}

	hasMaximumFavorites(length: number): boolean {
		console.log(length);
		return this.FAVORITES_MAX_SIZE < length;
	}

	deleteAssets(ids: string[]): Observable<any> {
		return this.http.post(`${this.assetUrl}/deleteAssets`, JSON.stringify({ids: ids}))
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Delete an specific Asset Comment
	 * @param {string[]} ids
	 * @returns {Observable<any>}
	 */
	deleteAssetComment(ids: number|string[]): Observable<any> {
		return this.http.post(`${this.assetEntitySearch}/deleteComment`, JSON.stringify({ids: ids}))
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	getFileName(viewName: string): Observable<any> {
		return this.http.post(`${this.defaultUrl}/filename`, JSON.stringify({viewName: viewName}))
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	isAllAssets(model: ViewModel): boolean {
		return model.name === this.ALL_ASSETS;
	}

	isSaveAvailable(model: ViewModel): boolean {
		return model && !this.isAllAssets(model) && this.hasSavePermission(model);
	}

	hasSavePermission(model): boolean {
		const hasPermission = this.permissionService.hasPermission.bind(this.permissionService);
		const {AssetExplorerSystemEdit, AssetExplorerEdit, AssetExplorerSystemCreate, AssetExplorerCreate } = Permission;

		if (model.id && model.isSystem) {
			return hasPermission(AssetExplorerSystemEdit);
		}

		if (model.id) {
			return  model.isOwner && hasPermission(AssetExplorerEdit);
		}

		if (model.isSystem) {
			return hasPermission(AssetExplorerSystemCreate);
		}

		return model.isOwner && hasPermission(AssetExplorerCreate);
	}

	validateUniquenessDataViewByName(dataViewName: string): Observable<boolean> {
		let postRequest = {
			name: dataViewName
		};

		const url = `${this.assetExplorerUrl}/validateUnique`;
		return this.http.post(url, JSON.stringify(postRequest))
			.map((response: any) => {
				return response && response.status === 'success' && response.data && response.data.isUnique;
			})
			.catch((error: any) => error);
	}

	/**
	 *
	 * @param searchParams
	 * @returns {Observable<any>}
	 */
	getAssetListForComboBox(searchParams: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		return this.http.get(`../${this.assetEntitySearch}/assetListForSelect2?q=${searchParams.query}&value=${searchParams.value}&max=${searchParams.maxPage}&page=${searchParams.currentPage}&assetClassOption=${searchParams.metaParam}`)
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

	/**
	 * Get the List of Asset Class Options
	 * @returns {Observable<any>}
	 */
	getAssetClassOptions(): Observable<any> {
		return this.http.get(`${this.assetUrl}/classOptions`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 *
	 * @param changeParams
	 * @returns {Observable<any>}
	 */
	retrieveChangedBundle(changeParams: any): Observable<any> {
		return this.http.post(`${this.assetUrl}/retrieveBundleChange`, JSON.stringify(changeParams))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	getAssetTypesForComboBox(searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		return this.http.get(`../${this.assetEntitySearch}/assetTypesOf?${searchModel.query}`)
			.map((response: any) => {
				let comboBoxSearchResultModel: ComboBoxSearchResultModel = {
					result: response.data.assetTypes,
					total: response.data.assetTypes.length,
					page: 1
				};
				return comboBoxSearchResultModel;
			})
			.catch((error: any) => error);
	}

	getManufacturersForComboBox(searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		return this.http.get(`../${this.assetEntitySearch}/manufacturer?${searchModel.query}`)
			.map((response: any) => {
				let comboBoxSearchResultModel: ComboBoxSearchResultModel = {
					result: response.data.manufacturers,
					total: response.data.manufacturers.length,
					page: 1
				};
				return comboBoxSearchResultModel;
			})
			.catch((error: any) => error);
	}

	getModelsForComboBox(searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		return this.http.get(`../${this.assetEntitySearch}/modelsOf?${searchModel.query}`)
			.map((response: any) => {
				let comboBoxSearchResultModel: ComboBoxSearchResultModel = {
					result: response.data.models,
					total: response.data.models.length,
					page: 1
				};
				return comboBoxSearchResultModel;
			})
			.catch((error: any) => error);
	}

	getRacksForRoom(roomId: number, sourceTarget: 'S'|'T'): Observable<any> {
		const request = {
			roomId: roomId,
			rackId: null,
			sourceTarget: sourceTarget,
			forWhom: 'Edit'
		};

		let mockSourceResponse = [
			{id: 0, text: 'Please Select'},
			{id: 1, text: 'Add Rack...'},
			{id: 14240, text: 'TBD'},
		];
		let mockTargetResponse = [
			{id: 0, text: 'Please Select'},
			{id: -1, text: 'Add Rack...'},
			{id: 13173, text: 'B5'},
			{id: 13161, text: 'B6'},
		];
		return this.http.get(`${this.assetUrl}/retrieveRackSelectOptions/${roomId}`)
			.map((response: Response) => response)
			.catch((error: any) => error);
	}

	getChassisForRoom(roomId: number): Observable<any> {
		return this.http.get(`${this.assetUrl}/retrieveChassisSelectOptions/${roomId}`)
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * Save the Asset
	 * @param model
	 * @returns {Observable<any>}
	 */
	saveAsset(model: any): Observable<any> {
		const request: any = {
			assetClass: model.asset.assetClass.name,
			asset: model.asset,
			dependencyMap: {
				supportAssets: this.prepareDependencies(model.dependencyMap.supportAssets),
				dependentAssets: this.prepareDependencies(model.dependencyMap.dependentAssets)
			}
		};

		return this.http.put(`${this.defaultUrl}/asset/${model.assetId}`, request)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Create an Asset
	 * @param model
	 * @returns {Observable<any>}
	 */
	createAsset(model: any): Observable<any> {
		const request: any = {
			assetClass: model.asset.assetClass.name,
			asset: model.asset,
			dependencyMap: {
				supportAssets: this.prepareDependencies(model.dependencyMap.supportAssets),
				dependentAssets: this.prepareDependencies(model.dependencyMap.dependentAssets)
			}
		};

		return this.http.post(`${this.defaultUrl}/asset`, request)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get an Asset by id
	 * @param assetId
	 * @returns {Observable<any>}
	 */
	getAsset(assetId: number): Observable<any> {
		return this.http.get(`${this.assetUrl}/${assetId}`)
			.map((response: any) => response.data.result)
			.catch((error: any) => error);
	}

	/**
	 * Validate Asset uniqueness by its Name
	 * @param assetId
	 * @returns {Observable<any>}
	 */
	checkAssetForUniqueName(assetToValid: any): Observable<any> {

		return this.http.post(`${this.assetUrl}/checkForUniqueName`, assetToValid)
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * Clone Asset
	 * @param assetId
	 * @returns {Observable<any>}
	 */
	cloneAsset(assetToClone): Observable<any> {

		return this.http.post(`${this.assetUrl}/clone`, assetToClone)
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * Prepare Support Assets and Dependent Assets
	 * @param dependencies
	 * @returns {any}
	 */
	private prepareDependencies(dependencies: any): any {
		if (dependencies && dependencies.length > 0) {
			dependencies.forEach((support: any) => {
				support.assetClass = support.assetClass.id;
				support.moveBundleId = support.assetDepend.moveBundle.id;
				support.targetAsset = {
					id: support.assetDepend.id,
					name: support.assetDepend.text
				};
				delete support.assetDepend;
				delete support.dependencyType;
				if (support.id === 0) {
					delete support.id;
				}
			});
		} else {
			dependencies = [];
		}

		return dependencies;
	}
}