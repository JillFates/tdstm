// Angular
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
// Service
import {DateUtils} from '../../../shared/utils/date.utils';
import {TranslatePipe} from '../../../shared/pipes/translate.pipe';
// Model
import {Flatten} from '../../../shared/model/data-list-grid.model';
import {LicenseEnvironment, LicenseStatus, LicenseType} from '../model/license.model';
import {FilterType} from 'tds-component-library';
// Other
import {Observable} from 'rxjs';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class LicenseAdminService {

	private readonly licenseUrl = '../ws/license';

	constructor(
		private http: HttpClient,
		private translateService: TranslatePipe) {
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
					model.licenseType = ((model.type === LicenseType.MULTI_PROJECT) ? this.translateService.transform('LICENSE.GLOBAL') : this.translateService.transform('LICENSE.SINGLE'));
					model.licenseEnvironment = ((model.environment === LicenseEnvironment.ENGINEERING) ? this.translateService.transform('LICENSE.ENGINEERING') : this.translateService.transform('LICENSE.TRAINING'));
					model.licenseStatus = ((model.status === LicenseStatus.PENDING) ? this.translateService.transform('GLOBAL.PENDING') : this.translateService.transform('GLOBAL.ACTIVE'));
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
	 * Apply the key for an specific license
	 */
	applyKey(id: number, hash: string): Observable<any> {
		return this.http.post(`${this.licenseUrl}/${id}/load`, JSON.stringify({hash: hash}))
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Resubmit the License
	 */
	resubmitLicenseRequest(id: number): Observable<any> {
		return this.http.post(`${this.licenseUrl}/${id}/email/request`, null)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the Email Content for this request
	 */
	getEmailContent(id: number): Observable<any> {
		return this.http.get(`${this.licenseUrl}/${id}/email/request`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the possible Environment Data Source
	 */
	getEnvironments(): Observable<any[]> {
		return this.http.get(`${this.licenseUrl}/environment`)
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
		return this.http.delete(`${this.licenseUrl}/${id}`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the possible Projects Data Source
	 */
	createRequestLicense(requestLicense: any): Observable<any[]> {
		let postRequest = {
			clientName: requestLicense.clientName,
			email: requestLicense.email,
			environment: requestLicense.environment,
			projectId: requestLicense.project.id,
			requestNote: requestLicense.specialInstruction,
		};

		return this.http.post(`${this.licenseUrl}/request`, JSON.stringify(postRequest))
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

		if (column.filterType === 'text' || column.filterType === FilterType.dropdown) {
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

		if (column.filterType === 'date') {
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
