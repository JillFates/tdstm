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
			description: 'dependencies datasource for Servide Now',
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