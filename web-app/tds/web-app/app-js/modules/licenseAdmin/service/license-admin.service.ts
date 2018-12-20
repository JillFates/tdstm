import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Flatten} from '../../../shared/model/data-list-grid.model';
import {DateUtils} from '../../../shared/utils/date.utils';

@Injectable()
export class LicenseAdminService {

	private readonly licenseUrl = '../ws/license';

	constructor(private http: HttpInterceptor) {
	}

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