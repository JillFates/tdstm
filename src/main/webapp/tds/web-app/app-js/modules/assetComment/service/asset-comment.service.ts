// Angular
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AssetCommentModel} from '../model/asset-comment.model';
import {DateUtils} from '../../../shared/utils/date.utils';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Flatten, DefaultBooleanFilterData} from '../../../shared/model/data-list-grid.model';
import {ApiResponseModel} from '../../../shared/model/ApiResponseModel';

@Injectable()
export class AssetCommentService {

	private jobProgressUrl = '../ws/progress';
	private readonly listCommentUrl = '../ws/asset/comment';

	constructor(private http: HttpClient) {
	}

	getAssetComments(): Observable<AssetCommentModel[]> {
		return this.http.get(`${this.listCommentUrl}`)
			.map((response: any) => {
				response.forEach((r) => {
					r.lastUpdated = ((r.lastUpdated) ? new Date(r.lastUpdated) : '');
					r.dateCreated = ((r.dateCreated) ? new Date(r.dateCreated) : '');
				});
				return response;
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
		let root = state.filter || {logic: 'and', filters: []};

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
			root.filters.push({field: column.property, operator: 'gte', value: init});
			root.filters.push({field: column.property, operator: 'lte', value: end});
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
		return filters.filter((r) => r['field'] !== excludeFilterName);
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