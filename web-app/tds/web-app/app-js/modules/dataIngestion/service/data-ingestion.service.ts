import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Rx';
import {Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {DataScriptModel, DataScriptMode} from '../model/data-script.model';
import {ProviderModel} from '../model/provider.model';
import {APIActionModel, APIActionParameterModel, EventReactionType} from '../model/api-action.model';
import {AgentModel, CredentialModel, AgentMethodModel} from '../model/agent.model';
import {INTERVAL} from '../../../shared/model/constants';
import {DateUtils} from '../../../shared/utils/date.utils';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class DataIngestionService {

	private dataDefaultUrl = '../ws';
	private dataApiActionUrl = '../ws/apiAction';
	private dataIngestionUrl = '../ws/dataingestion';

	mockData: CredentialModel[] = [
		{
			id: 1,
			name: 'Credential 1'
		},
		{
			id: 2,
			name: 'Credential 2'
		},
		{
			id: 3,
			name: 'Credential 3'
		}
	];

	mockParametersData: APIActionParameterModel[] = [
		{
			id: 1,
			name: 'sys_id',
			description: 'The Unique ID used by ServiceNow (sysid)',
			dataType: 'String',
			context: {
				value: 'Application',
				assetClass: 'APPLICATION'
			},
			value: 'externalRelId'
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
					r.agentMethod = {name: r.agentMethod};
					r.dateCreated = ((r.dateCreated) ? new Date(r.dateCreated) : '');
					r.lastModified = ((r.lastModified) ? new Date(r.lastModified) : '');
					r.producesData = (r.producesData === 1);
					r.polling = {
						frequency: {
							value: r.pollingInterval,
							interval: INTERVAL.SECONDS
						},
						lapsedAfter: {
							value: DateUtils.convertInterval({ value: r.pollingLapsedAfter, interval: INTERVAL.SECONDS }, INTERVAL.MINUTES),
							interval: INTERVAL.MINUTES
						},
						stalledAfter: {
							value: DateUtils.convertInterval({ value: r.pollingStalledAfter, interval: INTERVAL.SECONDS }, INTERVAL.MINUTES),
							interval: INTERVAL.MINUTES
						}
					};
					r.defaultDataScript = (r.defaultDataScript) ? r.defaultDataScript : {id: 0, name: ''};
					APIActionModel.createReactions(r, r.reactionScripts);
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

	getParameters(): Observable<APIActionParameterModel[]> {
		return Observable.from(this.mockParametersData).bufferCount(this.mockParametersData.length);
	}

	getActionMethodById(agentId: number): Observable<AgentMethodModel[]> {
		return this.http.get(`${this.dataApiActionUrl}/agent/${agentId}`)
			.map((res: Response) => {
				let result = res.json();
				let agentMethodModel = new Array<AgentMethodModel>();
				for (let property in result) {
					if (result.hasOwnProperty(property)) {
						agentMethodModel.push({
							id: result[property].name,
							name: result[property].name,
							description: result[property].description
						});
					}
				}
				result = agentMethodModel;
				return result;
			})
			.catch((error: any) => error.json());
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
			provider: model.provider.id,
			agentClass: model.agentClass.id,
			agentMethod: model.agentMethod.id,
			endpointUrl: model.endpointUrl,
			endpointPath: model.endpointPath,
			producesData: (model.producesData) ? 1 : 0,
			isPolling: (model.isPolling) ? 1 : 0,
			pollingInterval: DateUtils.convertInterval(model.polling.frequency, INTERVAL.SECONDS),
			pollingLapsedAfter: DateUtils.convertInterval(model.polling.lapsedAfter, INTERVAL.SECONDS),
			pollingStalledAfter: DateUtils.convertInterval(model.polling.stalledAfter, INTERVAL.SECONDS),
		};

		let reaction = {
			'STATUS': model.eventReactions[0].value,
			'SUCCESS': model.eventReactions[1].value,
			'DEFAULT': model.eventReactions[2].value,
			'ERROR': model.eventReactions[3].value,
			'FAILED': model.eventReactions[4].value,
			'LAPSED': model.eventReactions[5].value,
			'STALLED': model.eventReactions[6].value,
			'PRE': model.eventReactions[7].value,
			'FINAL': model.eventReactions[8].value
		};

		postRequest['reactionScripts'] = JSON.stringify(reaction);

		postRequest['defaultDataScript'] = ((postRequest.producesData === 1) ? model.defaultDataScript.id : null);

		if (!model.id) {
			return this.http.post(`${this.dataDefaultUrl}/apiAction`, JSON.stringify(postRequest))
				.map((res: Response) => {
					let result = res.json();
					let dataItem = (result && result.status === 'success' && result.data);
					return dataItem;
				})
				.catch((error: any) => error.json());
		} else {
			return this.http.put(`${this.dataDefaultUrl}/apiAction/${model.id}`, JSON.stringify(postRequest))
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

	validateCode(): Observable<any> {
		return this.http.post(`${this.dataApiActionUrl}/validateSyntax`, null)
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