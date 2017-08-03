import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { ReportModel, ReportGroupModel, ReportFolderIcon } from '../model/report.model';
import { HttpInterceptor } from '../../../shared/providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class AssetExplorerService {

	private assetExplorerUrl = '../ws/customDomain/fieldSpec';

	private mockData: Array<ReportGroupModel> = [
		{
			name: 'All',
			open: true,
			items: [],
			icon: ReportFolderIcon.folder
		},
		{
			name: 'Recent',
			open: false,
			items: [],
			icon: ReportFolderIcon.folder
		},
		{
			name: 'Favorites',
			open: false,
			items: [],
			icon: ReportFolderIcon.start
		},
		{
			name: 'My Reports',
			open: false,
			items: [],
			icon: ReportFolderIcon.folder
		},
		{
			name: 'System Reports',
			open: false,
			items: [{
				id: 1,
				name: 'Finance Applications',
				favorite: true,
				shared: true,
				subscribe: false
			}, {
				id: 2,
				name: 'HR Applicattions',
				favorite: false,
				shared: true,
				subscribe: false
			}, {
				id: 3,
				name: 'Legal Applications',
				favorite: false,
				shared: true,
				subscribe: true
			}],
			icon: ReportFolderIcon.folder
		}
	];

	constructor(private http: HttpInterceptor) { }

	getReports(): Observable<ReportGroupModel[]> {
		return Observable.from(this.mockData).bufferCount(this.mockData.length);
	}

}