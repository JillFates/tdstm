import {CredentialModel} from '../model/credential.model';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpHeaders, HttpResponse} from '@angular/common/http';
import {PreferenceService} from '../../../shared/services/preference.service';
import {ProviderModel} from '../../provider/model/provider.model';
import {APIActionModel} from '../../apiAction/model/api-action.model';
import {AUTH_METHODS, ENVIRONMENT, CREDENTIAL_STATUS, REQUEST_MODE} from '../model/credential.model';
import {INTERVAL} from '../../../shared/model/constants';
import {DateUtils} from '../../../shared/utils/date.utils';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Flatten, DefaultBooleanFilterData} from '../../../shared/model/data-list-grid.model';

@Injectable()
export class CredentialService {

	private readonly dataIngestionUrl = '../ws/dataingestion';
	private dataScriptUrl = '../ws/dataScript';
	private credentialUrl = '../ws/credential';
	private fileSystemUrl = '../ws/fileSystem';

	constructor(private http: HttpClient, private preferenceService: PreferenceService) {
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

	deleteCredential(id: number): Observable<string> {
		return this.http.delete(`${this.credentialUrl}/${id}`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
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

	uploadFile(formdata: any): Observable<any | HttpResponse<any>> {
		const httpOptions = {
			headers: new HttpHeaders({})
		};

		return this.http.post(`${this.fileSystemUrl}/uploadFile`, formdata, httpOptions)
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

}