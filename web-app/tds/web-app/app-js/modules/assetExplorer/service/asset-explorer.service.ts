import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { Response } from '@angular/http';
import { ReportModel, ReportGroupModel, ViewType } from '../model/report.model';
import { QuerySpec } from '../model/report-spec.model';
import { HttpInterceptor } from '../../../shared/providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class AssetExplorerService {

	private assetExplorerUrl = '../ws/assetExplorer/report';

	constructor(private http: HttpInterceptor) {
	}

	getReports(): Observable<ReportGroupModel[]> {
		return this.http.get(`${this.assetExplorerUrl}s`)
			.map((res: Response) => {
				let response = res.json().data;
				let reportGroupModel: ReportGroupModel[] = Object.keys(response).map(key => {
					return response[key];
				});
				return [
					{
						name: 'All',
						items: reportGroupModel,
						open: true,
						type: ViewType.ALL
					}, {
						name: 'Recent',
						items: [],
						open: false,
						type: ViewType.RECENT
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
					}, {
						name: 'System Views',
						items: reportGroupModel.filter(r => r['isSystem']),
						open: false,
						type: ViewType.SYSTEM_VIEWS
					}
				] as any;
			})
			.catch((error: any) => error.json());
	}

	getReport(id: number): Observable<ReportModel> {
		return this.http.get(`${this.assetExplorerUrl}/${id}`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data && result.data.dataView;
			})
			.catch((error: any) => error.json());
	}

	saveReport(model: ReportModel): Observable<ReportModel> {
		if (!model.id) {
			return this.http.post(this.assetExplorerUrl, JSON.stringify(model))
				.map((res: Response) => {
					let result = res.json();
					return result && result.status === 'success' && result.data && result.data.dataView;
				})
				.catch((error: any) => error.json());
		} else {
			return this.http.put(`${this.assetExplorerUrl}/${model.id}`, JSON.stringify(model))
				.map((res: Response) => {
					let result = res.json();
					return result && result.status === 'success' && result.data && result.data.dataView;
				})
				.catch((error: any) => error.json());
		}
	}

	queryData(query: QuerySpec): any[] {
		return [];
	}

	deleteReport(id: number): Observable<string> {
		return this.http.delete(`${this.assetExplorerUrl}/${id}`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data && result.data.status;
			})
			.catch((error: any) => error.json());
	}

}