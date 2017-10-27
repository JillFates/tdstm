import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Rx';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {DataScriptModel, ModeType} from '../model/data-script.model';
import {ProviderModel} from '../model/provider.model';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class DataIngestionService {

	private assetExplorerUrl = '../ws/';

	private mockData: Array<DataScriptModel> = [
		{
			name: 'sn_dependencies',
			provider: {
				id: 1,
				name: 'Service Now'
			},
			mode: ModeType.EXPORT,
			description: 'Dependencies datasource for Servide Now',
			dateCreated: new Date(),
			lastModified: new Date()
		},
		{
			name: 'sn_database',
			provider: {
				id: 1,
				name: 'Service Now'
			},
			mode: ModeType.IMPORT,
			description: 'Database datasource for Servide Now',
			dateCreated: new Date(),
			lastModified: new Date()
		},
		{
			name: 'tm_application_plan',
			provider: {
				id: 1,
				name: 'TransitionManager'
			},
			description: 'Application plan data from Master Project',
			mode: ModeType.IMPORT,
			dateCreated: new Date(),
			lastModified: new Date()
		},
		{
			name: 'd42_ipaddr',
			provider: {
				id: 1,
				name: 'Device42'
			},
			description: 'IP datasource for Device 42',
			mode: ModeType.EXPORT,
			dateCreated: new Date(),
			lastModified: new Date()
		}
	];

	private mockDataProvider: Array<ProviderModel> = [
		{
			id: 1,
			name: 'Service Now',
		}
	];

	constructor(private http: HttpInterceptor) {
	}

	getDataScripts(): Observable<DataScriptModel[]> {
		return Observable.from(this.mockData).bufferCount(this.mockData.length);
	}

	getProviders(): Observable<ProviderModel[]> {
		return Observable.from(this.mockDataProvider).bufferCount(this.mockDataProvider.length);
	}
}