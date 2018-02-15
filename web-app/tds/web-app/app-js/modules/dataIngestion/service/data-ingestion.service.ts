import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Rx';
import {Headers, Http, RequestOptions, Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {DataScriptModel, DataScriptMode} from '../model/data-script.model';
import {ProviderModel} from '../model/provider.model';
import {APIActionModel, APIActionParameterModel, EventReactionType} from '../model/api-action.model';
import {AgentModel, CredentialModel, AgentMethodModel} from '../model/agent.model';
import {INTERVAL} from '../../../shared/model/constants';
import {DateUtils} from '../../../shared/utils/date.utils';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {HttpEvent, HttpEventType, HttpProgressEvent, HttpResponse} from '@angular/common/http';

@Injectable()
export class DataIngestionService {

	private dataDefaultUrl = '../ws';
	private dataApiActionUrl = '../ws/apiAction';
	private dataIngestionUrl = '../ws/dataingestion';
	private dataScriptUrl = '../ws/dataScript';
	private fileSystemUrl = '../ws/fileSystem';

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
					if (r.reactionScripts && r.reactionScripts !== null && r.reactionScripts !== 'null') {
						APIActionModel.createReactions(r, r.reactionScripts);
					} else {
						APIActionModel.createBasicReactions(r);
					}
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

	/**
	 * Validate and wrap the content of the Parameters inside the Model and creates and observable from it.
	 * @param {APIActionModel} model
	 * @returns {Observable<APIActionParameterModel[]>}
	 */
	getParameters(model: APIActionModel): Observable<APIActionParameterModel[]> {
		return new Observable(observer => {
			if (model.methodParams && model.methodParams !== null && model.methodParams !== '') {
				observer.next(JSON.parse(model.methodParams));
			}
			observer.complete();
		});
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

	saveAPIAction(model: APIActionModel, parameterList: any): Observable<DataScriptModel> {
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

		if (parameterList && parameterList.data && parameterList.data.length > 0) {
			parameterList.data.forEach( (param) => {
				delete param.currentFieldList;
			});
			postRequest['methodParams'] = JSON.stringify(parameterList.data);
		}

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

	validateCode(scripts: any): Observable<any> {
		return this.http.post(`${this.dataApiActionUrl}/validateSyntax`, JSON.stringify({scripts: scripts}))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Validate if the current DataScript is being or not used somewhere
	 * @param {number} id
	 * @returns {Observable<string>}
	 */
	validateDeleteScript(id: number): Observable<string> {
		return this.http.get(`${this.dataIngestionUrl}/datascript/validateDelete/${id}`)
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

	saveScript(id: number, script: string): Observable<any> {
		let postRequest = {
			id: id,
			script: script
		};
		return this.http.post(`${this.dataScriptUrl}/saveScript`, JSON.stringify(postRequest))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success';
			})
			.catch((error: any) => error.json());
	}

	testScript(script: string, filename: string): Observable<any> {
		let postRequest = {
			script: script,
			fileName: filename
		};
		return this.http.post(`${this.dataScriptUrl}/testScript`, JSON.stringify(postRequest))
			.map((res: Response) => {
				let response = res.json();
				response.data.domains = [];
				for (let domain of Object.keys(response.data.data)) {
					response.data.domains.push(domain);
				}
				return response;
			})
			.catch((error: any) => error.json());
	}

	checkSyntax(script: string, filename: string): Observable<any> {
		let postRequest = {
			script: script,
			fileName: filename
		};
		return this.http.post(`${this.dataScriptUrl}/checkSyntax`, JSON.stringify(postRequest))
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	uploadText(content: string, extension: string): Observable<any> {
		let postRequest = {
			content: content,
			extension: extension
		};
		return this.http.post(`${this.fileSystemUrl}/uploadText`, JSON.stringify(postRequest))
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	uploadFile(formdata: any): Observable<any | HttpResponse<any>> {
		const headers = new Headers({});
		const options = new RequestOptions({ headers: headers });
		return this.http.post(`${this.fileSystemUrl}/uploadFile`, formdata, options)
			.map((res: Response) => {
				let response = res.json().data;
				return new HttpResponse({status: 200, body: { data : response } });
			})
			.catch((error: any) => error.json());
	}

	deleteFile(filename: string): Observable<any | HttpResponse<any>> {
		let body = JSON.stringify({filename: filename} );
		let headers = new Headers({ 'Content-Type': 'application/json' });
		let options = new RequestOptions({
			headers: headers,
			body : body
		});
		return this.http.delete(`${this.fileSystemUrl}/delete`, options)
			.map((res: Response) => {
				let response = res.json();
				response.operation = 'delete';
				return new HttpResponse({status: 200, body: { data : response } });
			})
			.catch((error: any) => error.json());
	}
}