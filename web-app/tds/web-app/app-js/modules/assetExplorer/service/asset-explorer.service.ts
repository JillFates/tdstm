import { Injectable } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { ReportModel } from '../model/report.model';
import { ReportGroupModel } from '../model/report-group.model';
import { HttpInterceptor } from '../../../shared/providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class AssetExplorerService {

	private assetExplorerUrl = '../ws/customDomain/fieldSpec';

	private mockData: Array<ReportGroupModel> = [
		{
			name: 'Recent',
			open: false,
			items: []
		},
		{
			name: 'Favorites',
			open: false,
			items: []
		},
		{
			name: 'My Reports',
			open: false,
			items: []
		},
		{
			name: 'System Reports',
			open: true,
			items: [{
				id: 1,
				name: 'Finance Applications',
				favorite: false,
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
				subscribe: false
			}]
		}
	];

	constructor(private http: HttpInterceptor) { }

	getReports(): Observable<ReportGroupModel[]> {
		return Observable.from(this.mockData).bufferCount(this.mockData.length);
	}

}