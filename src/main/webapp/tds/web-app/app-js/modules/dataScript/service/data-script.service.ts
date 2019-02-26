import {CredentialModel} from '../../credential/model/credential.model';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {PreferenceService} from '../../../shared/services/preference.service';
import {DataScriptModel, DataScriptMode, SampleDataModel} from '../model/data-script.model';
import {ProviderModel} from '../../provider/model/provider.model';
import {APIActionModel, APIActionParameterModel} from '../../apiAction/model/api-action.model';
import {AgentMethodModel} from '../../apiAction/model/agent.model';
import {AUTH_METHODS, ENVIRONMENT, CREDENTIAL_STATUS, REQUEST_MODE} from '../../credential/model/credential.model';
import {INTERVAL} from '../../../shared/model/constants';
import {DateUtils} from '../../../shared/utils/date.utils';
import {HttpResponse} from '@angular/common/http';
import {DOMAIN} from '../../../shared/model/constants';
import * as R from 'ramda';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Flatten, DefaultBooleanFilterData} from '../../../shared/model/data-list-grid.model';
import {ApiResponseModel} from '../../../shared/model/ApiResponseModel';

export const PROGRESSBAR_COMPLETED_STATUS = 'COMPLETED';
export const PROGRESSBAR_FAIL_STATUS = 'Failed';

@Injectable()
export class DataScriptService {

	private dataDefaultUrl = '../ws';
	private jobProgressUrl = '../ws/progress';
	private dataApiActionUrl = '../ws/apiAction';
	private readonly dataIngestionUrl = '../ws/dataingestion';
	private dataScriptUrl = '../ws/dataScript';
	private credentialUrl = '../ws/credential';
	private fileSystemUrl = '../ws/fileSystem';
	private ETLScriptUploadURL = '../ws/fileSystem/uploadFileETLDesigner';
	private ETLScriptUploadTextURL = '../ws/fileSystem/uploadTextETLDesigner';
	private assetImportUploadURL = '../ws/fileSystem/uploadFileETLAssetImport';
	private readonly GET_SAMPLE_DATA_URL = this.dataIngestionUrl.concat('/datascript/{0}/sampleData/{1}?originalFileName={2}&rootNode={3}');
	private readonly GET_ETL_SCRIPT_BY_ID_URL = this.dataIngestionUrl.concat('/datascript/{0}');

	constructor(private http: HttpClient, private preferenceService: PreferenceService) {
	}

	getDataScripts(): Observable<DataScriptModel[]> {
		return this.http.get(`${this.dataIngestionUrl}/datascript/list`)
			.map((response: any) => {
				let dataScriptModels = response && response.status === 'success' && response.data;
				dataScriptModels.forEach((r) => {
					r.mode = ((r.mode === 'Import') ? DataScriptMode.IMPORT : DataScriptMode.EXPORT);
					r.dateCreated = ((r.dateCreated) ? new Date(r.dateCreated) : '');
					r.lastUpdated = ((r.lastUpdated) ? new Date(r.lastUpdated) : '');
				});
				return dataScriptModels;
			})
			.catch((error: any) => error);
	}

	getProviders(): Observable<ProviderModel[]> {
		return this.http.get(`${this.dataIngestionUrl}/provider/list`)
			.map((response: any) => {
				let providerModels = response && response.status === 'success' && response.data;
				providerModels.forEach((r) => {
					r.dateCreated = ((r.dateCreated) ? new Date(r.dateCreated) : '');
					r.lastUpdated = ((r.lastUpdated) ? new Date(r.lastUpdated) : '');
				});
				return providerModels;
			})
			.catch((error: any) => error);
	}

	getAPIActions(): Observable<APIActionModel[]> {
		return this.http.get(`${this.dataDefaultUrl}/apiAction`)
			.map((response: any) => {
				let dataScriptModels = response && response.status === 'success' && response.data;
				dataScriptModels.forEach((model) => {
					this.transformApiActionModel(model);
				});
				return dataScriptModels;
			})
			.catch((error: any) => error);
	}

	getAPIAction(id: number): Observable<APIActionModel> {
		return this.http.get(`${this.dataDefaultUrl}/apiAction/${id}`)
			.map((response: any) => {
				let model = response && response.status === 'success' && response.data;
				this.transformApiActionModel(model);
				return model;
			})
			.catch((error: any) => error);
	}

	private transformApiActionModel(model: any): void {
		model.agentMethod = {id: model.agentMethod};
		model.dateCreated = ((model.dateCreated) ? new Date(model.dateCreated) : '');
		model.lastUpdated = ((model.lastUpdated) ? new Date(model.lastUpdated) : '');
		model.producesData = (model.producesData === 1);
		model.polling = {
			frequency: {
				value: model.pollingInterval,
				interval: INTERVAL.SECONDS
			},
			lapsedAfter: {
				value: DateUtils.convertInterval({ value: model.pollingLapsedAfter, interval: INTERVAL.SECONDS }, INTERVAL.MINUTES),
				interval: INTERVAL.MINUTES
			},
			stalledAfter: {
				value: DateUtils.convertInterval({ value: model.pollingStalledAfter, interval: INTERVAL.SECONDS }, INTERVAL.MINUTES),
				interval: INTERVAL.MINUTES
			}
		};
		model.defaultDataScript = (model.defaultDataScript) ? model.defaultDataScript : {id: 0, name: ''};
		if (model.reactionScripts && model.reactionScripts !== null && model.reactionScripts !== 'null') {
			APIActionModel.createReactions(model, model.reactionScripts);
		} else {
			APIActionModel.createBasicReactions(model);
		}
	}

	getAPIActionEnums(): Observable<any> {
		return this.http.get(`${this.dataApiActionUrl}/enums`)
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	getCredentials(): Observable<CredentialModel[]> {
		return this.http.get(`${this.credentialUrl}`)
			.map((response: any) => {
				let credentialModels = response && response.status === 'success' && response.data;

				credentialModels.forEach((model) => {
					// r.dateCreated = ((r.dateCreated) ? new Date(r.dateCreated) : '');
					// r.environment = ENVIRONMENT[r.environment];
					// r.authMethod = AUTH_METHODS[r.authenticationMethod];
					// r.status = CREDENTIAL_STATUS[r.status];
					this.processCredentialModel(model);
				});
				return credentialModels;
			})
			.catch((error: any) => error);
	}

	/**
	 * GET Credential by id.
	 * @returns {Observable<CredentialModel[]>}
	 */
	getCredential(id: number): Observable<CredentialModel> {
		return this.http.get(`${this.credentialUrl}/${id}`)
			.map((response: any) => {
				let model = response && response.status === 'success' && response.data;
				this.processCredentialModel(model);
				return model;
			})
			.catch((error: any) => error);
	}

	private processCredentialModel(model): void {
		model.dateCreated = ((model.dateCreated) ? new Date(model.dateCreated) : '');
		model.environment = ENVIRONMENT[model.environment];
		model.authMethod = AUTH_METHODS[model.authenticationMethod];
		model.status = CREDENTIAL_STATUS[model.status];
	}

	/**
	 * Get the List of Elements we need to populate in the UI - dropdownlist
	 * @returns {Observable<any>}
	 */
	getCredentialEnumsConfig(): Observable<any> {
		return this.http.get(`${this.credentialUrl}/enums`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Validate and wrap the content of the Parameters inside the Model and creates and observable from it.
	 * @param {APIActionModel} model
	 * @returns {Observable<APIActionParameterModel[]>}
	 */
	getParameters(model: APIActionModel): Observable<APIActionParameterModel[]> {
		return new Observable(observer => {
			if (model.methodParams && model.methodParams !== null && model.methodParams !== '') {
				let parameterList = JSON.parse(model.methodParams);
				parameterList.forEach( (param) => {
					if (param.property && param.property.field) {
						param.property = param.property.field;
					}
					if (param.context === 'ASSET') {
						param.context = DOMAIN.COMMON;
					}
					delete param.sourceFieldList;
					delete param.currentFieldList;
				});

				observer.next(parameterList);
			}
			observer.complete();
		});
	}

	/**
	 * Get Sample Data of a File by passing the FileName to the server
	 * @param {number} id
	 * @param {string} fileName
	 * @param {string} originalFileName
	 * @returns {Observable<SampleDataModel>}
	 */
	getSampleData(id: number, fileName: string, originalFileName = '', rootNode: string): Observable<SampleDataModel> {
		return this.http.get(this.GET_SAMPLE_DATA_URL
			.replace('{0}', id.toString())
			.replace('{1}', fileName)
			.replace('{2}', originalFileName)
			.replace('{3}', rootNode))
			.map((response: any) => {
				let data: any = (response && response.status === 'success' && response.data);
				let columns: any = [];
				let sampleDataModel: SampleDataModel;
				if (response.status === ApiResponseModel.API_ERROR && response.errors) {
					sampleDataModel = {
						errors: response.errors
					};
					return sampleDataModel;
				}
				if (data.config) {
					for (let property in data.config) {
						if (data.config.hasOwnProperty(property)) {
							const label = data.config[property].property;
							let column = {
								label,
								property: label,
								type: data.config[property].type,
								width: 140
							};
							columns.push(column);
						}
					}
					sampleDataModel = new SampleDataModel(columns, data.rows);
					sampleDataModel.gridHeight = 300;
				}
				return sampleDataModel;
			})
			.catch((error: any) => error);
	}

	getActionMethodById(agentId: number): Observable<AgentMethodModel[]> {
		return this.http.get(`${this.dataApiActionUrl}/connector/${agentId}`)
			.map((response: any) => {
				let agentMethodModel = new Array<AgentMethodModel>();
				for (let property in response) {
					if (response.hasOwnProperty(property)) {
						agentMethodModel.push({
							id: response[property].apiMethod,
							name: response[property].name,
							description: response[property].description,
							endpointUrl: response[property].endpointUrl,
							docUrl: response[property].docUrl,
							producesData: (response[property].producesData === 1),
							polling: {
								frequency: {
									value: ((response[property].pollingInterval) ? response[property].pollingInterval : 0),
									interval: INTERVAL.SECONDS
								},
								lapsedAfter: {
									value: DateUtils.convertInterval({
										value: ((response[property].pollingLapsedAfter) ? response[property].pollingLapsedAfter : 0),
										interval: INTERVAL.SECONDS
									}, INTERVAL.MINUTES),
									interval: INTERVAL.MINUTES
								},
								stalledAfter: {
									value: DateUtils.convertInterval({
										value: ((response[property].pollingStalledAfter) ? response[property].pollingStalledAfter : 0),
										interval: INTERVAL.SECONDS
									}, INTERVAL.MINUTES),
									interval: INTERVAL.MINUTES
								}
							},
							methodParams: response[property].params,
							script: response[property].script,
							httpMethod: response[property].httpMethod
						});
					}
				}
				response = agentMethodModel;
				return response;
			})
			.catch((error: any) => error);
	}

	saveDataScript(model: DataScriptModel): Observable<DataScriptModel> {
		let postRequest = {
			name: model.name,
			description: model.description,
			mode: model.mode === DataScriptMode.IMPORT ? 'Import' : 'Export',
			providerId: model.provider.id,
			etlSourceCode: model.etlSourceCode
		};
		if (!model.id) {
			return this.http.post(`${this.dataIngestionUrl}/datascript`, JSON.stringify(postRequest))
				.map((response: any) => {
					let dataItem = (response && response.status === 'success' && response.data);
					dataItem.dataScript.mode = (dataItem.dataScript.mode === 'Import') ? DataScriptMode.IMPORT : DataScriptMode.EXPORT;
					return dataItem;
				})
				.catch((error: any) => error);
		} else {
			return this.http.put(`${this.dataIngestionUrl}/datascript/${model.id}`, JSON.stringify(postRequest))
				.map((response: any) => {
					return response && response.status === 'success' && response.data;
				})
				.catch((error: any) => error);
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
				.map((response: any) => {
					return response && response.status === 'success' && response.data;
				})
				.catch((error: any) => error);
		} else {
			return this.http.put(`${this.dataIngestionUrl}/provider/${model.id}`, JSON.stringify(postRequest))
				.map((response: any) => {
					return response && response.status === 'success' && response.data;
				})
				.catch((error: any) => error);
		}
	}

	saveAPIAction(model: APIActionModel, parameterList: any): Observable<DataScriptModel> {
		let postRequest: any = {
			name: model.name,
			description: model.description,
			provider: { id: model.provider.id },
			apiCatalog: { id: model.dictionary.id },
			connectorMethod: model.agentMethod.id,
			httpMethod: model.httpMethod,
			endpointUrl: model.endpointUrl,
			docUrl: model.docUrl,
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

		postRequest['defaultDataScript'] = { id: ((postRequest.producesData === 1 && model.defaultDataScript.id !== 0) ? model.defaultDataScript.id : null) };

		if (parameterList && parameterList.length > 0) {
			let requestParameterListData = R.clone(parameterList);
			requestParameterListData.forEach( (param) => {
				if (param.fieldName && param.fieldName.field) {
					param.fieldName = param.fieldName.field;
				}
				if (param.context && param.context.assetClass) {
					param.context = param.context.assetClass;
				}
				if (param.context === DOMAIN.COMMON) {
					param.context = 'ASSET';
				}
				delete param.sourceFieldList;
				delete param.currentFieldList;
			});
			postRequest['methodParams'] = JSON.stringify(requestParameterListData);
		}

		if (model.credential && model.credential.id && model.credential.id !== 0) {
			postRequest.credential = { id: model.credential.id };
		}

		if (!model.id) {
			return this.http.post(`${this.dataDefaultUrl}/apiAction`, JSON.stringify(postRequest))
				.map((response: any) => {
					let dataItem = (response && response.status === 'success' && response.data);
					return dataItem;
				})
				.catch((error: any) => error);
		} else {
			postRequest.version = model.version;
			return this.http.put(`${this.dataDefaultUrl}/apiAction/${model.id}`, JSON.stringify(postRequest))
				.map((response: any) => {
					return response && response.status === 'success' && response.data;
				})
				.catch((error: any) => error);
		}
	}

	saveCredential(model: CredentialModel): Observable<CredentialModel> {

		let postRequest: any = {
			name: model.name,
			description: model.description,
			environment: Object.keys(ENVIRONMENT).find((type) => ENVIRONMENT[type] === model.environment),
			provider: {id: model.provider.id},
			status: model.status.toUpperCase(),
			authenticationMethod: Object.keys(AUTH_METHODS).find((type) => AUTH_METHODS[type] === model.authMethod),
			username: model.username,
			authenticationUrl: (model.authenticationUrl) ? model.authenticationUrl : '',
			terminateUrl: (model.terminateUrl) ? model.terminateUrl : '',
			renewTokenUrl: (model.renewTokenUrl) ? model.renewTokenUrl : '',
			requestMode: (model.requestMode === REQUEST_MODE.BASIC_AUTH) ? 'BASIC_AUTH' : 'FORM_VARS',
			httpMethod: model.httpMethod.toUpperCase()
		};

		// Properties only required on this methods.
		if (model.authMethod === AUTH_METHODS.COOKIE || model.authMethod === AUTH_METHODS.HEADER || model.authMethod === AUTH_METHODS.BASIC_AUTH) {
			if (model.authMethod === AUTH_METHODS.COOKIE || model.authMethod === AUTH_METHODS.HEADER ) {
				postRequest.sessionName = (model.sessionName) ? model.sessionName  : '';
			}
			postRequest.validationExpression = (model.validationExpression) ? model.validationExpression  : '';
		}

		// The UI validates if the Password exists however, on edition is not required unless you want to change it
		if (model.password && model.password.length > 0 && model.password !== '') {
			postRequest.password = model.password;
		}

		if (!model.id) {
			return this.http.post(`${this.credentialUrl}`, JSON.stringify(postRequest))
				.map((response: any) => {
					let dataItem = (response && response.status === 'success' && response.data);
					return dataItem;
				})
				.catch((error: any) => error);
		} else {
			postRequest.version = model.version;
			return this.http.put(`${this.credentialUrl}/${model.id}`, JSON.stringify(postRequest))
				.map((response: any) => {
					return response && response.status === 'success' && response.data;
				})
				.catch((error: any) => error);
		}
	}

	validateUniquenessDataScriptByName(model: DataScriptModel): Observable<boolean> {
		let postRequest = {
			providerId: model.provider.id,
			name: model.name
		};
		if (model.id) {
			postRequest['dataScriptId'] = model.id;
		}
		return this.http.post(`${this.dataIngestionUrl}/datascript/validateUnique`, JSON.stringify(postRequest))
			.map((response: any) => {
				return response && response.status === 'success' && response.data && response.data.isUnique;
			})
			.catch((error: any) => error);
	}

	validateUniquenessProviderByName(model: ProviderModel): Observable<ProviderModel> {
		let postRequest = {};
		if (model.id) {
			postRequest['providerId'] = model.id;
		}
		return this.http.post(`${this.dataIngestionUrl}/provider/validateUnique/${model.name}`, JSON.stringify(postRequest))
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	validateCode(scripts: any): Observable<any> {
		return this.http.post(`${this.dataApiActionUrl}/validateSyntax`, JSON.stringify({scripts: scripts}))
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Validate if the Authentication meets the requirements
	 * @param {number} credentialId
	 * @returns {Observable<any>}
	 */
	validateAuthentication(credentialId: number): Observable<any> {
		return this.http.post(`${this.credentialUrl}/test/${credentialId}`, null)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Validate if the expression executed is correct
	 * @param {string} validateExpression
	 * @returns {Observable<any>}
	 */
	validateExpressionCheck(validateExpression: string): Observable<any> {
		let postRequest = {
			expression: validateExpression
		};
		return this.http.post(`${this.credentialUrl}/checkValidExprSyntax`, JSON.stringify(postRequest))
			.map((response: any) => {
				let errorResult = response && response.status === 'success' && response.data;
				let errors = '';
				if (errorResult['errors']) {
					errorResult['errors'].forEach((error: string) => {
						errors += error + '\n';
					});
				}
				return {
					valid: errorResult.valid,
					error: errors
				};
			})
			.catch((error: any) => error);
	}

	/**
	 * Validate if the current DataScript is being or not used somewhere
	 * @param {number} id
	 * @returns {Observable<string>}
	 */
	validateDeleteScript(id: number): Observable<string> {
		return this.http.get(`${this.dataIngestionUrl}/datascript/validateDelete/${id}`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	deleteDataScript(id: number): Observable<string> {
		return this.http.delete(`${this.dataIngestionUrl}/datascript/${id}`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	deleteProvider(id: number): Observable<string> {
		return this.http.delete(`${this.dataIngestionUrl}/provider/${id}`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	deleteAPIAction(id: number): Observable<string> {
		return this.http.delete(`${this.dataDefaultUrl}/apiAction/${id}`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	deleteCredential(id: number): Observable<string> {
		return this.http.delete(`${this.credentialUrl}/${id}`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	saveScript(id: number, script: string): Observable<any> {
		let postRequest = {
			id: id,
			script: script
		};
		return this.http.post(`${this.dataScriptUrl}/saveScript`, JSON.stringify(postRequest))
			.map((response: any) => {
				return response && response.status === 'success';
			})
			.catch((error: any) => error);
	}

	getETLScript(id: number): Observable<ApiResponseModel> {
		return this.http.get(this.GET_ETL_SCRIPT_BY_ID_URL.replace('{0}', id.toString()) )
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * POST - this will create a progress job for the test script process.
	 * @param {string} script
	 * @param {string} filename
	 * @returns {Observable<any>}
	 */
	testScript(script: string, filename: string): Observable<ApiResponseModel> {
		let postRequest = {
			script: script,
			filename: filename
		};
		return this.http.post(`${this.dataScriptUrl}/initiateTestScript`, JSON.stringify(postRequest))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	checkSyntax(script: string, filename: string): Observable<any> {
		let postRequest = {
			script: script,
			filename: filename
		};
		return this.http.post(`${this.dataScriptUrl}/checkSyntax`, JSON.stringify(postRequest))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	uploadText(content: string, extension: string): Observable<any> {
		let postRequest = {
			content: content,
			extension: extension
		};
		return this.http.post(`${this.fileSystemUrl}/uploadText`, JSON.stringify(postRequest))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	uploadETLScriptFileText(content: string, extension: string): Observable<any> {
		let postRequest = {
			content: content,
			extension: extension
		};
		return this.http.post(this.ETLScriptUploadTextURL, JSON.stringify(postRequest))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	uploadFile(formdata: any): Observable<any | HttpResponse<any>> {
		const httpOptions = {
			headers: new HttpHeaders({'Content-Type': 'application/json'})
		};
		return this.http.post(`${this.fileSystemUrl}/uploadFile`, formdata, httpOptions)
			.map((response: any) => {
				return new HttpResponse({status: 200, body: { data : response.data } });
			})
			.catch((error: any) => error);
	}

	uploadETLScriptFile(formdata: any): Observable<any | HttpResponse<any>> {
		const httpOptions = {
			headers: new HttpHeaders({'Content-Type': 'application/json'})
		};
		return this.http.post(this.ETLScriptUploadURL, formdata, httpOptions)
			.map((response: any) => {
				return new HttpResponse({status: 200, body: { data : response.data } });
			})
			.catch((error: any) => error);
	}

	uploadAssetImportFile(formdata: any): Observable<any | HttpResponse<any>> {
		const httpOptions = {
			headers: new HttpHeaders({'Content-Type': 'application/json'})
		};
		return this.http.post(this.assetImportUploadURL, formdata, httpOptions)
			.map((response: any) => {
				return new HttpResponse({status: 200, body: { data : response.data } });
			})
			.catch((error: any) => error);
	}

	deleteFile(filename: string): Observable<any | HttpResponse<any>> {
		const httpOptions = {
			headers: new HttpHeaders({'Content-Type': 'application/json'}), body: JSON.stringify({filename: filename} )
		};

		return this.http.delete(`${this.fileSystemUrl}/delete`, httpOptions)
			.map((response: any) => {
				response.operation = 'delete';
				return new HttpResponse({status: 200, body: { data : response } });
			})
			.catch((error: any) => error);
	}

	private getUserPreference(preferenceName: string): Observable<any> {
		return this.preferenceService.getPreference(preferenceName);
	}

	/**
	 * Based on provided column, update the structure which holds the current selected filters
	 * @param {any} column: Column to filter
	 * @param {any} state: Current filters state
	 * @returns {any} Filter structure updated
	 */
	filterColumn(column: any, state: any): any {
		let root = state.filter || { logic: 'and', filters: [] };

		let [filter] = Flatten(root).filter(item => item.field === column.property);

		if (!column.filter) {
			column.filter = '';
		}

		if (column.type === 'text') {
			if (!filter) {
				root.filters.push({
					field: column.property,
					operator: 'contains',
					value: column.filter,
					ignoreCase: true
				});
			} else {
				filter = root.filters.find((r) => {
					return r['field'] === column.property;
				});
				filter.value = column.filter;
			}
		}

		if (column.type === 'date') {
			const {init, end} = DateUtils.getInitEndFromDate(column.filter);

			if (filter) {
				state.filter.filters = this.getFiltersExcluding(column.property, state);
			}
			root.filters.push({ field: column.property, operator: 'gte', value: init, });
			root.filters.push({ field: column.property, operator: 'lte', value: end });
		}

		if (column.type === 'boolean') {
			if (!filter) {
				root.filters.push({
					field: column.property,
					operator: 'eq',
					value: (column.filter === 'True')
				});
			} else {
				if (column.filter === DefaultBooleanFilterData) {
					this.clearFilter(column, state);
				} else {
					filter = root.filters.find((r) => {
						return r['field'] === column.property;
					});
					filter.value = (column.filter === 'True');
				}
			}
		}

		return root;
	}

	/**
	 * Update the filters state structure removing the column filter provided
	 * @param {any} column: Column to exclude from filters
	 * @param {any} state: Current filters state
	 * @returns void
	 */
	clearFilter(column: any, state: any): void {
		column.filter = '';
		state.filter.filters = this.getFiltersExcluding(column.property, state);
	}

	/**
	 * Get the filters state structure excluding the column filter name provided
	 * @param {string} excludeFilterName:  Name of the filter column to exclude
	 * @param {any} state: Current filters state
	 * @returns void
	 */
	getFiltersExcluding(excludeFilterName: string, state: any): any {
		const filters = (state.filter && state.filter.filters) || [];
		return  filters.filter((r) => r['field'] !== excludeFilterName);
	}

	/**
	 * GET - Gets any job current progresss based on progressKey used as a unique job identifier.
	 * @param {string} progressKey
	 * @returns {Observable<ApiResponseModel>}
	 */
	getJobProgress(progressKey: string): Observable<ApiResponseModel> {
		return this.http.get(`${this.jobProgressUrl}/${progressKey}`)
			.map((response: any) => response)
			.catch((error: any) => error);
	}
}