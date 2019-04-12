import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Flatten} from '../../../shared/model/data-list-grid.model';
import {DateUtils} from '../../../shared/utils/date.utils';
import * as moment from 'moment';

@Injectable()
export class LicenseManagerService {

	private readonly licenseUrl = '../ws/manager/license';

	constructor(private http: HttpClient) {
	}

	/**
	 * Get all License from the User
	 */
	getLicenses(): Observable<any[]> {
		return this.http.get(`${this.licenseUrl}`)
			.map((response: any) => {
				let licenseModels = response && response.status === 'success' && response.data;
				licenseModels.forEach((model) => {
					model.activationDate = ((model.activationDate) ? new Date(model.activationDate) : '');
					model.expirationDate = ((model.expirationDate) ? new Date(model.expirationDate) : '');
				});
				return licenseModels;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get a single License instance
	 */
	getLicense(id: number): Observable<any> {
		return this.http.get(`${this.licenseUrl}/${id}`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Import a New License
	 */
	requestImportLicense(licenseKey: string): Observable<any> {
		return this.http.post(`${this.licenseUrl}/request`, JSON.stringify({ data: licenseKey}))
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Does the activation of the current license if this is not active
	 * @param id
	 */
	activateLicense(id: number): Observable<any> {
		return this.http.post(`${this.licenseUrl}/${id}/activate`, null)
			.map((response: any) => response.data)
			.catch((error: any) => error);
	}

	/**
	 * Revoke the License
	 * @param id
	 */
	revokeLicense(id: number): Observable<any> {
		return this.http.delete(`${this.licenseUrl}/${id}`)
			.map((response: any) => response.data)
			.catch((error: any) => error);
	}

	/**
	 * If by some reason the License was not applied at first time, this will do a request for it
	 */
	manuallyRequest(id: number): Observable<any> {
		return this.http.post(`${this.licenseUrl}/${id}/email/send`, null)
			.map((response: any) => response.data)
			.catch((error: any) => error);
	}

	/**
	 * Save the Entire License
	 */
	saveLicense(licenseModel: any): Observable<any> {

		let licenseModified: any = {
			environment: licenseModel.environment,
			method: {
				name: licenseModel.method.name
			},
			activationDate: licenseModel.activationDate ? moment(licenseModel.activationDate).format('YYYY-MM-DD') : '',
			expirationDate: licenseModel.expirationDate ? moment(licenseModel.expirationDate).format('YYYY-MM-DD') : '',
			status: licenseModel.status,
			project: {
				id: (licenseModel.project.id !== 'all') ? parseInt(licenseModel.project.id, 10) : licenseModel.project.id,  // We pass 'all' when is multiproject
				name: licenseModel.project.name
			},
			bannerMessage: licenseModel.bannerMessage,
			gracePeriodDays: licenseModel.gracePeriodDays,
			websitename: licenseModel.websitename,
			hostName: licenseModel.hostName
		};
		if (licenseModel.method.name !== 'CUSTOM') {
			licenseModified.method.max = parseInt(licenseModel.method.max, 10);
		}

		return this.http.put(`${this.licenseUrl}/${licenseModel.id}`, JSON.stringify(licenseModified))
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the list of activity actions associated to the license
	 * @param id
	 */
	getActivityLog(id: number): Observable<any[]> {
		return this.http.get(`${this.licenseUrl}/${id}/activitylog`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the possible Environment Data Source
	 */
	getEnvironments(): Observable<any[]> {
		return this.http.get(`../ws/license/environment`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get Key associated with the License
	 */
	getKeyCode(id: number): Observable<any[]> {
		return this.http.get(`${this.licenseUrl}/${id}/key`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the possible Projects Data Source
	 */
	getProjects(): Observable<any[]> {
		return this.http.get(`${this.licenseUrl}/project`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Delete the selected License
	 * @param id
	 */
	deleteLicense(id: number): Observable<string> {
		return this.http.delete(`${this.licenseUrl}/${id}/delete`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
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