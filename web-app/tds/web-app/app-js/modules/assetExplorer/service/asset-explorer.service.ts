import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { ReportModel, ReportGroupModel, ReportFolderIcon } from '../model/report.model';
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
			isSystem: true
		},
		{
			id: 2,
			name: 'HR Applicattions',
			isOwner: false,
			isShared: false,
			isSystem: true
		}, {
			id: 3,
			name: 'Legal Applications',
			isOwner: false,
			isShared: false,
			isSystem: true
		}, {
			id: 4,
			name: 'My First Report',
			isOwner: true,
			isShared: false,
			isSystem: false
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
			isSystem: false
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
						icon: ReportFolderIcon.folder
					}, {
						name: 'Recent',
						items: [],
						open: false,
						icon: ReportFolderIcon.folder
					}, {
						name: 'Favorites',
						items: items.filter(r => r['isFavorite']),
						open: false,
						icon: ReportFolderIcon.star
					}, {
						name: 'My Reports',
						items: items.filter(r => r.isOwner),
						open: false,
						icon: ReportFolderIcon.folder
					}, {
						name: 'Shared Reports',
						items: items.filter(r => r.isShared),
						open: false,
						icon: ReportFolderIcon.folder
					}, {
						name: 'System Reports',
						items: items.filter(r => r.isSystem),
						open: false,
						icon: ReportFolderIcon.folder
					}
				];
			});
	}

	getReport(id: number): Observable<ReportModel> {
		return Observable.from(this.mockData.filter(r => r.id === +id));
	}

	saveReport(model: ReportModel): void {
		console.log('save method');
	}

	queryData(query: QuerySpec): any[] {
		return [];
	}

	deleteReport(id: number) {
		return true;
	}

}