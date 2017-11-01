import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Rx';
import {Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {DataScriptModel, ModeType} from '../model/data-script.model';
import {ProviderModel} from '../model/provider.model';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class DataIngestionService {

	private dataIngestionUrl = '../ws/dataingestion';

	private mockDataScripts: Array<DataScriptModel> = [
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
			description: 'Description',
			comment: 'My comment',
			dateCreated: new Date()
		}
	];

	constructor(private http: HttpInterceptor) {
	}

	getDataScripts(): Observable<DataScriptModel[]> {
		return Observable.from(this.mockDataScripts).bufferCount(this.mockDataScripts.length);
	}

	getProviders(): Observable<ProviderModel[]> {
		return Observable.from(this.mockDataProvider).bufferCount(this.mockDataProvider.length);
	}

	saveDataScript(model: DataScriptModel): Observable<DataScriptModel> {
		let postRequest = {
			name: model.name,
			description: model.description,
			target: model.view,
			mode: model.mode === ModeType.IMPORT ? 'Import' : 'Export',
			providerId: model.provider.id
		};
		return this.http.post(`${this.dataIngestionUrl}/datascript`, JSON.stringify(postRequest))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	saveProvider(model: ProviderModel): Observable<ProviderModel> {
		let postRequest = {
			name: model.name,
			description: model.description
		};
		return this.http.post(`${this.dataIngestionUrl}/provider`, JSON.stringify(postRequest))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	validateUniquenessDataScriptByName(model: DataScriptModel): Observable<DataScriptModel> {
		let postRequest = {
			providerId: model.provider.id
		};

		return this.http.post(`${this.dataIngestionUrl}/datascript/validateunique/${model.name}`, JSON.stringify(postRequest))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	validateUniquenessProviderByName(model: ProviderModel): Observable<ProviderModel> {
		return this.http.post(`${this.dataIngestionUrl}/provider/validateunique/${model.name}`, null)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	deleteDataScript(id: number): Observable<string> {
		return this.http.delete(`${this.dataIngestionUrl}/datascript/${id}`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	deleteProvider(id: number): Observable<string> {
		return this.http.delete(`${this.dataIngestionUrl}/provider/${id}`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}
}