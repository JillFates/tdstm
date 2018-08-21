import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Response} from '@angular/http';
import {ViewModel, ViewGroupModel, ViewType} from '../model/view.model';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Permission} from '../../../shared/model/permission.model';
import {ComboBoxSearchModel} from '../../../shared/components/combo-box/model/combobox-search-param.model';
import {ComboBoxSearchResultModel} from '../../../shared/components/combo-box/model/combobox-search-result.model';
import {PermissionService} from '../../../shared/services/permission.service';
import {DeviceModel} from '../components/device/model-device/model/device-model.model';
import {DeviceManufacturer} from '../components/device/manufacturer/model/device-manufacturer.model';

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

	constructor(private http: HttpInterceptor, private permissionService: PermissionService) {}

	getReports(): Observable<ViewGroupModel[]> {
		return this.http.get(`${this.assetExplorerUrl}/views`)
			.map((res: Response) => {
				let response = res.json().data;
				let reportGroupModel: ViewGroupModel[] = Object.keys(response).map(key => {
					return response[key];
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
			.catch((error: any) => error.json());
	}

	getReport(id: number): Observable<ViewModel> {
		return this.http.get(`${this.assetExplorerUrl}/view/${id}`)
			.map((res: Response) => {
				let result = res.json();
				if (result && result.status === 'success' && result.data) {
					return result.data.dataView;
				} else {
					throw new Error(result.errors.join(';'));
				}
			})
			.catch((error: any) => error.json());
	}

	saveReport(model: ViewModel): Observable<ViewModel> {
		if (!model.id) {
			return this.http.post(`${this.assetExplorerUrl}/view`, JSON.stringify(model))
				.map((res: Response) => {
					let result = res.json();
					return result && result.status === 'success' && result.data && result.data.dataView;
				})
				.catch((error: any) => error.json());
		} else {
			return this.http.put(`${this.assetExplorerUrl}/view/${model.id}`, JSON.stringify(model))
				.map((res: Response) => {
					let result = res.json();
					return result && result.status === 'success' && result.data && result.data.dataView;
				})
				.catch((error: any) => error.json());
		}
	}

	deleteReport(id: number): Observable<string> {
		return this.http.delete(`${this.assetExplorerUrl}/view/${id}`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data && result.data.status;
			})
			.catch((error: any) => error.json());
	}

	query(id: number, schema: any): Observable<any[]> {
		return this.http.post(`${this.assetExplorerUrl}/query/${id}`, JSON.stringify(schema))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	previewQuery(schema: any): Observable<any[]> {
		return this.http.post(`${this.assetExplorerUrl}/previewQuery`, JSON.stringify(schema))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	saveFavorite(id: number): Observable<any> {
		return this.http.post(`${this.assetExplorerUrl}/favoriteDataview/${id}`, '')
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data && result.data.status;
			})
			.catch((error: any) => error.json());
	}

	deleteFavorite(id: number): Observable<any> {
		return this.http.delete(`${this.assetExplorerUrl}/favoriteDataview/${id}`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data && result.data.status;
			})
			.catch((error: any) => error.json());
	}

	hasMaximumFavorites(length: number): boolean {
		console.log(length);
		return this.FAVORITES_MAX_SIZE < length;
	}

	deleteAssets(ids: string[]): Observable<any> {
		return this.http.post(`${this.assetUrl}/deleteAssets`, JSON.stringify({ids: ids}))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Delete an specific Asset Comment
	 * @param {string[]} ids
	 * @returns {Observable<any>}
	 */
	deleteAssetComment(ids: number[]): Observable<any> {
		return this.http.post(`${this.assetEntitySearch}/deleteComment`, JSON.stringify({ids: ids}))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	getFileName(viewName: string): Observable<any> {
		return this.http.post(`${this.defaultUrl}/filename`, JSON.stringify({viewName: viewName}))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
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
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data && result.data.isUnique;
			})
			.catch((error: any) => error.json());
	}

	/**
	 *
	 * @param searchParams
	 * @returns {Observable<any>}
	 */
	getAssetListForComboBox(searchParams: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		return this.http.get(`../${this.assetEntitySearch}/assetListForSelect2?q=${searchParams.query}&value=${searchParams.value}&max=${searchParams.maxPage}&page=${searchParams.currentPage}&assetClassOption=${searchParams.metaParam}`)
			.map((res: Response) => {
				let response = res.json();
				let comboBoxSearchResultModel: ComboBoxSearchResultModel = {
					result: response.results,
					total: response.total,
					page: response.page
				};
				return comboBoxSearchResultModel;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Get the List of Asset Class Options
	 * @returns {Observable<any>}
	 */
	getAssetClassOptions(): Observable<any> {
		return this.http.get(`${this.assetUrl}/classOptions`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 *
	 * @param changeParams
	 * @returns {Observable<any>}
	 */
	retrieveChangedBundle(changeParams: any): Observable<any> {
		return this.http.post(`${this.assetUrl}/retrieveBundleChange`, JSON.stringify(changeParams))
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	getAssetTypesForComboBox(searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		return this.http.get(`../${this.assetEntitySearch}/assetTypesOf?${searchModel.query}`)
			.map((res: Response) => {
				let response = res.json();
				let comboBoxSearchResultModel: ComboBoxSearchResultModel = {
					result: response.data.assetTypes,
					total: response.data.assetTypes.length,
					page: 1
				};
				return comboBoxSearchResultModel;
			})
			.catch((error: any) => error.json());
	}

	getManufacturersForComboBox(searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		return this.http.get(`../${this.assetEntitySearch}/manufacturer?${searchModel.query}`)
			.map((res: Response) => {
				let response = res.json();
				let comboBoxSearchResultModel: ComboBoxSearchResultModel = {
					result: response.data.manufacturers,
					total: response.data.manufacturers.length,
					page: 1
				};
				return comboBoxSearchResultModel;
			})
			.catch((error: any) => error.json());
	}

	getModelsForComboBox(searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		return this.http.get(`../${this.assetEntitySearch}/modelsOf?${searchModel.query}`)
			.map((res: Response) => {
				let response = res.json();
				let comboBoxSearchResultModel: ComboBoxSearchResultModel = {
					result: response.data.models,
					total: response.data.models.length,
					page: 1
				};
				return comboBoxSearchResultModel;
			})
			.catch((error: any) => error.json());
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
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	getChassisForRoom(roomId: number): Observable<any> {
		return this.http.get(`${this.assetUrl}/retrieveChassisSelectOptions/${roomId}`)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
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
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
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

	isValidAlias(alias: string, id: number, parentName: string): Observable<boolean> {
		const url = `/tdstm/manufacturer/validateAliasForForm?alias=${alias}&id=${id}&parentName=${parentName}` ;

		return this.http.get(url, '')
			.map((res: any) => res === 'valid')
	}

	getModelAsJSON(id: string): Observable<DeviceModel> {
		const url = '/tdstm/model/retrieveModelAsJSON?id=' + id;

		return this.http.post(url, '')
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
	}

	getDeviceManufacturer(id: string): Observable<DeviceManufacturer> {
		const url = '/tdstm/manufacturer/retrieveManufacturerAsJSON?id=' + id;
		return this.http.post(url, '')
			.map((res: Response) => res.json())
			.map((res: any) => res && Object.assign({aka: res.aliases || '', akaCollection: res.akaCollection}, res.manufacturer) || {})
			.catch((error: any) => error.json());
	}
}