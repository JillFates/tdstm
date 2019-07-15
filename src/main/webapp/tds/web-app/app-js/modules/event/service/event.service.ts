import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {EventModel} from '../model/event.model';
import {DateUtils} from '../../../shared/utils/date.utils';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Flatten, DefaultBooleanFilterData} from '../../../shared/model/data-list-grid.model';
import {ApiResponseModel} from '../../../shared/model/ApiResponseModel';

@Injectable()
export class EventService {

	private jobProgressUrl = '../ws/progress';

	constructor(private http: HttpClient) {
	}

	getEvents(): Observable<EventModel[]> {
		return this.http.get(`../ws/moveEvent/list`)
			.map((response: any) => {
				let eventModels = response && response.status === 'success' && response.data;
				eventModels.forEach((r) => {
					r.estStartTime = ((r.estStartTime) ? new Date(r.estStartTime) : '');
					r.estCompletionTime = ((r.estCompletionTime) ? new Date(r.estCompletionTime) : '');
				});
				return eventModels;
			})
			.catch((error: any) => error);
	}

	getModelForEventCreate(): Observable<EventModel[]> {
		return this.http.get(`../ws/moveEvent/createModel`)
			.map((response: any) => {
				let eventModels = response && response.status === 'success' && response.data;
				return eventModels;
			})
			.catch((error: any) => error);
	}

	getModelForEventViewEdit(id): Observable<any> {
		return this.http.get(`../ws/moveEvent/viewEditModel/${id}`)
			.map((response: any) => {
				let eventModels = response && response.status === 'success' && response.data;
				return eventModels;
			})
			.catch((error: any) => error);
	}

	saveEvent(eventModel: EventModel, id = null): Observable<any> {
		let postObject = JSON.parse(JSON.stringify(eventModel));
		let i = 0;
		for (i = 0; i < postObject.moveBundle.length; i++) {
			postObject.moveBundle[i] = postObject.moveBundle[i].id;
		}
		for (i = 0; i < postObject.tagIds.length; i++) {
			postObject.tagIds[i] = postObject.tagIds[i].id;
		}
		return this.http.post(`../ws/moveEvent/saveEvent/${id}`, JSON.stringify(postObject))
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}

	deleteEvent(id): Observable<any> {
		return this.http.delete(`../ws/moveEvent/deleteEvent/${id}`)
			.map((response: any) => {
				let eventModels = response && response.status === 'success' && response.data;
				return eventModels;
			})
			.catch((error: any) => error);
	}

	markAssetsMoved(id): Observable<any> {
		return this.http.put(`../ws/moveEvent/markAssetsMoved/${id}`,null)
			.map((response: any) => {
				let eventModels = response && response.status === 'success' && response.data;
				return eventModels;
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
		let root = state.filter || {lsogic: 'and', filters: []};

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