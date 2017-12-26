import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Rx';
import {Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {DataScriptModel, DataScriptMode} from '../model/data-script.model';
import {ProviderModel} from '../model/provider.model';
import {APIActionModel} from '../model/api-action.model';
import {AgentModel, CredentialModel, AgentMethodModel} from '../model/agent.model';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class DataIngestionService {

	private dataDefaultUrl = '../ws';
	private dataApiActionUrl = '../ws/apiAction';
	private dataIngestionUrl = '../ws/dataingestion';

	mockData: CredentialModel[] = [
		{
			id: '1',
			name: 'Credential 1'
		},
		{
			id: '2',
			name: 'Credential 2'
		},
		{
			id: '3',
			name: 'Credential 3'
		}
	];

	mockDataAgentMethod: AgentMethodModel[] = [
		{
			'description': 'Used to generate Simple Notification Service (SNS) messages',
			'method': 'sendSns',
			'name': 'sendSnsNotification',
			'params': {
				'queueName': {
					'type': 'java.lang.String',
					'description': 'The name of the queue/topic to send message to'
				},
				'callbackMethod': {
					'type': 'java.lang.String',
					'description': 'The name of the callback method that the async response should trigger'
				},
				'message': {
					'type': 'java.lang.Object',
					'description': 'The data to pass to the message'
				}
			},
			'results': {
				'status': {
					'type': 'java.lang.String',
					'description': 'Status of process (success|error|failed|running)'
				},
				'cause': {
					'type': 'java.lang.String',
					'description': 'The cause of an error or failure'
				}
			}
		},
		{
			'description': 'Used to generate Simple Queue Service (SQS) messages',
			'method': 'sendSqs',
			'name': 'sendSqsMessage',
			'params': {
				'queueName': {
					'type': 'java.lang.String',
					'description': 'The name of the queue/topic to send message to'
				},
				'callbackMethod': {
					'type': 'java.lang.String',
					'description': 'The name of the callback method that the async response should trigger'
				},
				'message': {
					'type': 'java.lang.Object',
					'description': 'The data to pass to the message'
				}
			},
			'results': {
				'status': {
					'type': 'java.lang.String',
					'description': 'Status of process (success|error|failed|running)'
				},
				'cause': {
					'type': 'java.lang.String',
					'description': 'The cause of an error or failure'
				}
			}
		}
	];

	constructor(private http: HttpInterceptor) {
	}

	getDataScripts(): Observable<DataScriptModel[]> {
		return this.http.get(`${this.dataIngestionUrl}/datascript/list`)
			.map((res: Response) => {
				let result = res.json();
				let dataScriptModels = result && result.status === 'success' && result.data;
				dataScriptModels.forEach((r) => {
					r.mode = ((r.mode === 'Import') ? DataScriptMode.IMPORT : DataScriptMode.EXPORT);
					r.dateCreated = ((r.dateCreated) ? new Date(r.dateCreated) : '');
					r.lastUpdated = ((r.lastUpdated) ? new Date(r.lastUpdated) : '');
				});
				return dataScriptModels;
			})
			.catch((error: any) => error.json());
	}

	getProviders(): Observable<ProviderModel[]> {
		return this.http.get(`${this.dataIngestionUrl}/provider/list`)
			.map((res: Response) => {
				let result = res.json();
				let providerModels = result && result.status === 'success' && result.data;
				providerModels.forEach((r) => {
					r.dateCreated = ((r.dateCreated) ? new Date(r.dateCreated) : '');
					r.lastUpdated = ((r.lastUpdated) ? new Date(r.lastUpdated) : '');
				});
				return providerModels;
			})
			.catch((error: any) => error.json());
	}

	getAPIActions(): Observable<APIActionModel[]> {
		return this.http.get(`${this.dataDefaultUrl}/apiAction`)
			.map((res: Response) => {
				let result = res.json();
				let dataScriptModels = result && result.status === 'success' && result.data;
				dataScriptModels.forEach((r) => {
					r.agentMethod = { name: r.agentMethod };
					r.dateCreated = ((r.dateCreated) ? new Date(r.dateCreated) : '');
					r.lastModified = ((r.lastModified) ? new Date(r.lastModified) : '');
					r.producesData = (r.producesData === 1);
					r.pollingInterval = (r.pollingInterval === 1);
					r.agentClass = { id: r.agentClass };
				});
				return dataScriptModels;
			})
			.catch((error: any) => error.json());
	}

	getAgents(): Observable<AgentModel[]> {
		return this.http.get(`${this.dataApiActionUrl}/agent`)
			.map((res: Response) => {
				let result = res.json();
				let agentModels = result; // && result.status === 'success' && result.data;
				return agentModels;
			})
			.catch((error: any) => error.json());
	}

	getCredentials(): Observable<AgentModel[]> {
		return Observable.from(this.mockData).bufferCount(this.mockData.length);
	}

	getActionMethodById(agentId: string): Observable<AgentMethodModel[]> {
		return Observable.from(this.mockDataAgentMethod).bufferCount(this.mockDataAgentMethod.length);
	}

	saveDataScript(model: DataScriptModel): Observable<DataScriptModel> {
		let postRequest = {
			name: model.name,
			description: model.description,
			mode: model.mode === DataScriptMode.IMPORT ? 'Import' : 'Export',
			providerId: model.provider.id
		};
		if (!model.id) {
			return this.http.post(`${this.dataIngestionUrl}/datascript`, JSON.stringify(postRequest))
				.map((res: Response) => {
					let result = res.json();
					let dataItem = (result && result.status === 'success' && result.data);
					dataItem.dataScript.mode = (dataItem.dataScript.mode === 'Import') ? DataScriptMode.IMPORT : DataScriptMode.EXPORT;
					return dataItem;
				})
				.catch((error: any) => error.json());
		} else {
			return this.http.put(`${this.dataIngestionUrl}/datascript/${model.id}`, JSON.stringify(postRequest))
				.map((res: Response) => {
					let result = res.json();
					return result && result.status === 'success' && result.data;
				})
				.catch((error: any) => error.json());
		}
	}

	saveProvider(model: ProviderModel): Observable<ProviderModel> {
		let postRequest = {
			name: model.name,
			description: model.description,
			comment: model.comment
		};
		if (!model.id) {
			return this.http.post(`${this.dataIngestionUrl}/provider`, JSON.stringify(postRequest))
				.map((res: Response) => {
					let result = res.json();
					return result && result.status === 'success' && result.data;
				})
				.catch((error: any) => error.json());
		} else {
			return this.http.put(`${this.dataIngestionUrl}/provider/${model.id}`, JSON.stringify(postRequest))
				.map((res: Response) => {
					let result = res.json();
					return result && result.status === 'success' && result.data;
				})
				.catch((error: any) => error.json());
		}
	}

	saveAPIAction(model: APIActionModel): Observable<DataScriptModel> {
		let postRequest = {
			name: model.name,
			description: model.description,
			providerId: model.provider.id
		};
		if (!model.id) {
			return this.http.post(`${this.dataIngestionUrl}/datascript`, JSON.stringify(postRequest))
				.map((res: Response) => {
					let result = res.json();
					let dataItem = (result && result.status === 'success' && result.data);
					dataItem.dataScript.mode = (dataItem.dataScript.mode === 'Import') ? DataScriptMode.IMPORT : DataScriptMode.EXPORT;
					return dataItem;
				})
				.catch((error: any) => error.json());
		} else {
			return this.http.put(`${this.dataIngestionUrl}/datascript/${model.id}`, JSON.stringify(postRequest))
				.map((res: Response) => {
					let result = res.json();
					return result && result.status === 'success' && result.data;
				})
				.catch((error: any) => error.json());
		}
	}

	validateUniquenessDataScriptByName(model: DataScriptModel): Observable<DataScriptModel> {
		let postRequest = {
			providerId: model.provider.id
		};
		if (model.id) {
			postRequest['dataScriptId'] = model.id;
		}
		return this.http.post(`${this.dataIngestionUrl}/datascript/validateunique/${model.name}`, JSON.stringify(postRequest))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	validateUniquenessProviderByName(model: ProviderModel): Observable<ProviderModel> {
		let postRequest = {};
		if (model.id) {
			postRequest['providerId'] = model.id;
		}
		return this.http.post(`${this.dataIngestionUrl}/provider/validateunique/${model.name}`, JSON.stringify(postRequest))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	validateUniquenessAPIActionByName(model: APIActionModel): Observable<APIActionModel> {
		let postRequest = {
			providerId: model.provider.id
		};
		if (model.id) {
			postRequest['dataScriptId'] = model.id;
		}
		return this.http.post(`${this.dataIngestionUrl}/datascript/validateunique/${model.name}`, JSON.stringify(postRequest))
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

	deleteAPIAction(id: number): Observable<string> {
		return this.http.delete(`${this.dataDefaultUrl}/apiAction/${id}`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}
}