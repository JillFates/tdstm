import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Flatten} from '../../../shared/model/data-list-grid.model';
import {DateUtils} from '../../../shared/utils/date.utils';
import * as moment from 'moment';

@Injectable()
export class LicenseManagerService {

	private readonly licenseUrl = '../ws/manager/license';

	constructor(private http: HttpInterceptor) {
	}

	/**
	 * Get all License from the User
	 */
	getLicenses(): Observable<any[]> {
		return this.http.get(`${this.licenseUrl}`)
			.map((res: Response) => {
				let result = res.json();
				let licenseModels = result && result.status === 'success' && result.data;
				licenseModels.forEach((model) => {
					model.activationDate = ((model.activationDate) ? new Date(model.activationDate) : '');
					model.expirationDate = ((model.expirationDate) ? new Date(model.expirationDate) : '');
				});
				return licenseModels;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Get a single License instance
	 */
	getLicense(id: number): Observable<any> {
		return this.http.get(`${this.licenseUrl}/${id}`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Import a New License
	 */
	requestImportLicense(licenseKey: string): Observable<any> {
		return this.http.post(`${this.licenseUrl}/request`, JSON.stringify({ data: licenseKey}))
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Does the activation of the current license if this is not active
	 * @param id
	 */
	activateLicense(id: number): Observable<any> {
		return this.http.post(`${this.licenseUrl}/${id}/activate`, null)
			.map((res: Response) => {
				let result = res.json();
				return result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Revoke the License
	 * @param id
	 */
	revokeLicense(id: number): Observable<any> {
		return this.http.delete(`${this.licenseUrl}/${id}`, null)
			.map((res: Response) => {
				let result = res.json();
				return result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * If by some reason the License was not applied at first time, this will do a request for it
	 */
	manuallyRequest(id: number): Observable<any> {
		return this.http.post(`${this.licenseUrl}/${id}/email/send`, null)
			.map((res: Response) => {
				let result = res.json();
				return result.data;
			})
			.catch((error: any) => error.json());
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
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Get the list of activity actions associated to the license
	 * @param id
	 */
	getActivityLog(id: number): Observable<any[]> {
		return this.http.get(`${this.licenseUrl}/${id}/activitylog`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Get the possible Environment Data Source
	 */
	getEnvironments(): Observable<any[]> {
		return this.http.get(`../ws/license/environment`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Get Key associated with the License
	 */
	getKeyCode(id: number): Observable<any[]> {
		return this.http.get(`${this.licenseUrl}/${id}/key`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Get the possible Projects Data Source
	 */
	getProjects(): Observable<any[]> {
		return this.http.get(`${this.licenseUrl}/project`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Delete the selected License
	 * @param id
	 */
	deleteLicense(id: number): Observable<string> {
		return this.http.delete(`${this.licenseUrl}/${id}/delete`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
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