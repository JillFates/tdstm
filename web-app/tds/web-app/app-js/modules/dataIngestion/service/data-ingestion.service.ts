import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Rx';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {DataScriptRowModel} from '../model/data-script.model';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class DataIngestionService {

	private assetExplorerUrl = '../ws/';

	private mockData: Array<DataScriptRowModel> = [
		{
			name: 'sn_dependencies',
			provider: 'Service Now',
			description: 'Dependencies datasource for Servide Now',
			dateCreated: new Date(),
			lastModified: new Date()
		},
		{
			name: 'sn_database',
			provider: 'Service Now',
			description: 'Database datasource for Servide Now',
			dateCreated: new Date(),
			lastModified: new Date()
		},
		{
			name: 'tm_application_plan',
			provider: 'TransitionManager',
			description: 'Application plan data from Master Project',
			dateCreated: new Date(),
			lastModified: new Date()
		},
		{
			name: 'd42_ipaddr',
			provider: 'Device42',
			description: 'IP datasource for Device 42',
			dateCreated: new Date(),
			lastModified: new Date()
		}
	];

	constructor(private http: HttpInterceptor) {
	}

	getDataScripts(): Observable<DataScriptRowModel[]> {
		return Observable.from(this.mockData).bufferCount(this.mockData.length);
	}
}