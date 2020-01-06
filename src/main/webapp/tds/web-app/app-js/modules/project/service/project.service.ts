// Angular
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Store} from '@ngxs/store';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Observable} from 'rxjs';
import {DefaultBooleanFilterData, Flatten} from '../../../shared/model/data-list-grid.model';
import {DateUtils} from '../../../shared/utils/date.utils';
import {ApiResponseModel} from '../../../shared/model/ApiResponseModel';
import {PREFERENCES_LIST, PreferenceService} from '../../../shared/services/preference.service';
import {SetDefaultProject} from '../actions/project.actions';

@Injectable()
export class ProjectService {

	private jobProgressUrl = '../ws/progress';

	constructor(private http: HttpClient, private preferenceService: PreferenceService, private store: Store) {
		this.preferenceService.getPreference(PREFERENCES_LIST.CURR_TZ).subscribe();
	}

	getDefaultProject(): Observable<any> {
		return this.http.get(`../ws/project/default`)
			.map((response: any) => {
				this.store.dispatch(new SetDefaultProject(response.data));
				return response.data;
			})
			.catch((error: any) => {
				this.store.dispatch(new SetDefaultProject(null));
				return error
			});
	}

	getProjects(): Observable<any> {
		return this.http.get(`../ws/project/lists`)
			.map((response: any) => {
				response.data.activeProjects.forEach((item: any) => {
					item.completionDate = item.completionDate ?
						DateUtils.toDateUsingFormat(DateUtils.getDateFromGMT(item.completionDate), DateUtils.SERVER_FORMAT_DATE) : '';
					item.startDate = item.startDate ?
						DateUtils.toDateUsingFormat(DateUtils.getDateFromGMT(item.startDate), DateUtils.SERVER_FORMAT_DATE) : '';
				});
				response.data.completedProjects.forEach((item: any) => {
					item.completionDate = item.completionDate ?
						DateUtils.toDateUsingFormat(DateUtils.getDateFromGMT(item.completionDate), DateUtils.SERVER_FORMAT_DATE) : '';
					item.startDate = item.startDate ?
						DateUtils.toDateUsingFormat(DateUtils.getDateFromGMT(item.startDate), DateUtils.SERVER_FORMAT_DATE) : '';
				});
				return response.data;
			})
			.catch((error: any) => error);

	}

	getModelForProjectViewEdit(id) {
		return this.http.get(`../ws/project/viewEditProject/${id}`)
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}

	getModelForProjectCreate() {
		return this.http.get(`../ws/project/createProjectModel`)
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);

	}

	deleteProject(id) {
		return this.http.delete(`../ws/project/${id}`)
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}

	saveProject(model, originalFilename = '', id = ''): Observable<any> {
		let postModel = JSON.parse(JSON.stringify(model));
		postModel['originalFilename'] = originalFilename;
		postModel['planMethodology'] = postModel.planMethodology.field;
		postModel['partnerIds'] = postModel.partners.map(p => p.id);
		postModel['collectMetrics'] = postModel.collectMetrics ? true : false;
		return this.http.post(`../ws/project/saveProject/${id}`, postModel)
				.map((response: any) => {
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

		if (!column.filter == undefined) {
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

		if (column.type === 'number') {
			if (!filter) {
				root.filters.push({
					field: column.property,
					operator: 'eq',
					value: column.filter,
				});
			} else {
				filter = root.filters.find((r) => {
					return r['field'] === column.property;
				});
				filter.value = column.filter;
				if (filter.value == undefined) {
					this.clearFilter(column, state);
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