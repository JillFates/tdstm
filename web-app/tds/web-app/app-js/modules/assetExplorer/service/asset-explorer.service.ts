import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { ReportModel, ReportGroupModel, ReportType } from '../model/report.model';
import { QuerySpec } from '../model/report-spec.model';
import { HttpInterceptor } from '../../../shared/providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class AssetExplorerService {

	private assetExplorerUrl = '../ws/{ASSET_EXPLORER_URL}';

	private mockData: ReportModel[] = [
		{
			id: 1,
			name: 'Finance Applications',
			isOwner: false,
			isShared: false,
			isSystem: true,
			schema: {
				domains: ['APPLICATION', 'STORAGE'],
				columns: [],
				filters: [],
				sort: {
					domain: 'APPLICATION',
					property: 'id',
					order: 'a'
				}
			}
		},
		{
			id: 2,
			name: 'HR Applicattions',
			isOwner: false,
			isShared: false,
			isSystem: true,
			schema: {
				domains: ['APPLICATION', 'DEVICE'],
				columns: [],
				filters: [],
				sort: {
					domain: 'APPLICATION',
					property: 'id',
					order: 'a'
				}
			}
		}, {
			id: 3,
			name: 'Legal Applications',
			isOwner: false,
			isShared: false,
			isSystem: true,
			schema: {
				domains: ['APPLICATION'],
				columns: [],
				filters: [],
				sort: {
					domain: 'APPLICATION',
					property: 'id',
					order: 'a'
				}
			}
		}, {
			id: 4,
			name: 'My First Report',
			isOwner: true,
			isShared: false,
			isSystem: false,
			schema: {
				domains: ['DEVICE', 'DATABASE', 'STORAGE'],
				columns: [],
				filters: [],
				sort: {
					domain: 'DEVICE',
					property: 'id',
					order: 'a'
				}
			}
		}, {
			id: 5,
			name: 'My First Shared Report',
			isOwner: true,
			isShared: true,
			isSystem: false
		}, {
			id: 6,
			name: 'Another user awesome report',
			isOwner: false,
			isShared: true,
			isSystem: false,
			schema: {
				domains: ['DEVICE', 'DATABASE', 'STORAGE'],
				columns: [],
				filters: [],
				sort: {
					domain: 'DEVICE',
					property: 'id',
					order: 'a'
				}
			}
		}
	];

	constructor(private http: HttpInterceptor) { }

	getReports(): Observable<ReportGroupModel[]> {
		return Observable.from(this.mockData)
			.bufferCount(this.mockData.length)
			.map((items: ReportModel[]) => {
				return [
					{
						name: 'All',
						items: items,
						open: true,
						type: ReportType.ALL
					}, {
						name: 'Recent',
						items: [],
						open: false,
						type: ReportType.RECENT
					}, {
						name: 'Favorites',
						items: items.filter(r => r['isFavorite']),
						open: false,
						type: ReportType.FAVORITES
					}, {
						name: 'My Reports',
						items: items.filter(r => r.isOwner),
						open: false,
						type: ReportType.MY_REPORTS
					}, {
						name: 'Shared Reports',
						items: items.filter(r => r.isShared),
						open: false,
						type: ReportType.SHARED_REPORTS
					}, {
						name: 'System Reports',
						items: items.filter(r => r.isSystem),
						open: false,
						type: ReportType.SYSTEM_REPORTS
					}
				];
			});
	}

	getReport(id: number): Observable<ReportModel> {
		return Observable.from(this.mockData.filter(r => r.id === +id));
	}

	saveReport(model: ReportModel): Observable<ReportModel> {
		let index = this.mockData.length;
		if (model.id) {
			index = this.mockData.findIndex(d => d.id === model.id);
			this.mockData[index] = model;
		} else {
			model.id = this.mockData.length + 1;
			this.mockData.push(model);
		}
		return Observable.from([this.mockData[index]]);
	}

	queryData(query: QuerySpec): any[] {
		return [];
	}

	deleteReport(id: number): Observable<ReportModel> {
		let index = this.mockData.findIndex(d => d.id === id);
		return Observable.from(this.mockData.splice(index, 1));
	}

}