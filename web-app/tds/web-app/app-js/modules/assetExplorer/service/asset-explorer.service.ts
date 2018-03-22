import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Rx';
import {Response} from '@angular/http';
import {ViewModel, ViewGroupModel, ViewType} from '../model/view.model';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Permission} from '../../../shared/model/permission.model';
import {PermissionService} from '../../../shared/services/permission.service';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class AssetExplorerService {

	private defaultUrl = '../ws';
	private assetExplorerUrl = '../ws/assetExplorer';
	private assetUrl = '../ws/asset';
	private FAVORITES_MAX_SIZE = 10;

	constructor(private http: HttpInterceptor, private permissionService: PermissionService) {
	}

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
			.do(null, err => console.log(err));
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

	getFileName(viewName: string): Observable<any> {
		return this.http.post(`${this.defaultUrl}/filename`, JSON.stringify({viewName: viewName}))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	isAllAssets(model: ViewModel): boolean {
		return Boolean(!model.createdBy);
	}

	isSaveAvailable(model: ViewModel): boolean {
		return !this.isAllAssets(model) && this.hasSavePermission(model);
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

}