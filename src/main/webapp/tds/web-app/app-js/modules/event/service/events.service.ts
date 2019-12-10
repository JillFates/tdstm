import {Injectable, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {pathOr} from 'ramda'

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {catchError, map} from 'rxjs/operators';

import {EventModel, EventRowType, CatagoryRowType, CategoryTask, TaskCategoryCell} from '../model/event.model';
import {NewsModel, NewsDetailModel} from '../model/news.model';
import {DateUtils} from '../../../shared/utils/date.utils';
import move from 'ramda/es/move';
import {DefaultBooleanFilterData, Flatten} from '../../../shared/model/data-list-grid.model';
import {ApiResponseModel} from '../../../shared/model/ApiResponseModel';
import {PREFERENCES_LIST, PreferenceService} from '../../../shared/services/preference.service';

/**
 * @name EventsService
 */
@Injectable()
export class EventsService {
	// private instance variable to hold base url
	private jobProgressUrl = '../ws/progress';
	private readonly baseURL = '/tdstm';
	private readonly APP_EVENT_LISTS_URL = `${this.baseURL}/ws/moveEvent/list`;
	private readonly APP_EVENT_NEWS = `${this.baseURL}/ws/moveEventNews`;
	private readonly APP_EVENT_UPDATE_NEWS = `${this.baseURL}/newsEditor/updateNews`;
	private readonly APP_EVENT_SAVE_NEWS = `${this.baseURL}/newsEditor/saveNews`;
	private readonly APP_EVENT_DELETE_NEWS = `${this.baseURL}/newsEditor/deleteNews`;
	private readonly APP_EVENT_NEWS_DETAIL = `${this.baseURL}/newsEditor/retrieveCommetOrNewsData`;
	private readonly APP_EVENT_LIST_BUNDLES = `${this.baseURL}/ws/event/listBundles`;
	private readonly APP_EVENT_STATUS_DETAILS = `${this.baseURL}/ws/dashboard/eventData`;
	private readonly APP_EVENT_DETAILS = `${this.baseURL}/ws/moveEvent/dashboardModel`;
	private readonly APP_EVENT_STATUS_UPDATE = `${this.baseURL}/ws/event/updateEventSummary`;
	private readonly APP_EVENT_TASK_CATEGORY = `${this.baseURL}/ws/event/taskCategoriesStats`;
	private readonly categories = [
		'Step',
		'',
		'Tasks',
		'Planned Start',
		'Planned Completion',
		'Actual Start',
		'Actual Completion'
	];

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient, private preferenceService: PreferenceService) {
		this.preferenceService.getPreference(PREFERENCES_LIST.CURR_TZ).subscribe();
	}

	/**
	 * Get events list
	 * @returns {Observable<any>}
	 * TODO: @sam please use the previously already implemented getEvents() from task.service.ts.
	 */
	getEvents(): Observable<EventModel[]> {
		return this.http.get(`${this.APP_EVENT_LISTS_URL}`)
			.map((response: any) => {
				return response && response.data || [];
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the news associated to the event
 	 * @param {number} eventId
	 * @returns {Observable<NewsModel[]>}
	 */
	getNewsFromEvent(eventId: number): Observable<NewsModel[]> {
		return this.http.get(`${this.APP_EVENT_NEWS}/${eventId}`)
			.map((response: any) => {
				return response || [];
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the news details
 	 * @param {number} newsid News Id
	 * @returns {Observable<NewsDetailModel>}
	 */
	getNewsDetail(newsId: number, commentType: string): Observable<NewsDetailModel> {
		return this.http.get(`${this.APP_EVENT_NEWS_DETAIL}/?id=${newsId}&commentType=${commentType}`)
			.map((response: any) => {
				return response && response.shift() || null;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get event details
 	 * @param {number} event Event id
 	 * @param {boolean} viewUnpublished Flag to filter unpublished events
	 * @returns {Observable<any>} Event details
	*/
	getEventDetails(event: number, viewUnpublished: boolean): Observable<any> {
		const url = `${this.APP_EVENT_DETAILS}/?moveEvent=${event}&viewUnpublished=${viewUnpublished ? 1 : 0}`;
		return this.http.get(url)
			.map((response: any) => {
				const model = pathOr(null, ['data', 'model'], response);
				if (model) {
					return {
						remainTaskCount: model.remainTaskCount || 0,
						percTaskDone: model.percTaskDone  || 0,
						percTaskReady: model.percTaskReady  || 0,
						percTaskStarted: model.percTaskStarted || 0,
						countDone: model.countDone || 0,
						countStarted: model.countStarted || 0,
						countReady: model.countReady || 0,
						countHold: model.countHold || 0,
						countPending: model.countPending || 0,
						effortRemainDone: model.effortRemainDone || '',
						effortRemainPending: model.effortRemainPending || '',
						effortRemainReady: model.effortRemainReady || '',
						effortRemainStarted: model.effortRemainStarted || '',
						effortRemainHold: model.effortRemainHold || '',
						percDurationDone: model.percDurationDone || '',
						percDurationReady: model.percDurationReady || '',
						percDurationStarted: model.percDurationStarted || '',
						teamTaskMatrix: model.teamTaskMatrix || [],
						moveEvent: model.moveEvent,
						moveBundleList: model.moveBundleList || []
					}
				}

				return null;
			})
			.catch((error: any) => error);
	}

	/**
	 * Call the endpoint to update the news
 	 * @param {any} news News info
	 * @returns {Observable<NewsDetailModel>} News details updated
	*/
	updateNews(news: any): Observable<NewsDetailModel> {
		news.mode = 'ajax';
		return this.http.post(`${this.APP_EVENT_UPDATE_NEWS}`, JSON.stringify(news))
			.map((response: any) => {
				return response || null;
			})
			.catch((error: any) => error);
	}

	/**
	 * Call the endpoint to create a news
 	 * @param {any} news News info
	 * @returns {Observable<NewsDetailModel>} News details created
	*/
	saveNews(news: any): Observable<NewsDetailModel> {
		news.mode = 'ajax';
		return this.http.post(`${this.APP_EVENT_SAVE_NEWS}`, JSON.stringify(news))
			.map((response: any) => {
				return response || null;
			})
			.catch((error: any) => error);
	}

	/**
	 * Call the endpoint to delete a news
 	 * @param {any} news News info
	 * @returns {Observable<NewsDetailModel>} Delete operation results
	*/
	deleteNews(news: any): Observable<NewsDetailModel> {
		news.mode = 'ajax';
		return this.http.get(`${this.APP_EVENT_DELETE_NEWS}/?id=${news.id}`)
			.map((response: any) => {
				return response || null;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the bundles list corresponding to an specific event
 	 * @param {number} eventId Event id
	 * @returns {Observable<any[]>} Event Bundle list
	*/
	getListBundles(eventId: number): Observable<any[]> {
		return this.http.get(`${this.APP_EVENT_LIST_BUNDLES}/${eventId}`)
			.map((response: any) => pathOr(null, ['data', 'list'], response))
			.catch((error: any) => error);
	}

	/**
	 * Get the event status details
	 * @param {string} userTimeZone User Time Zone
 	 * @param {number} eventId Event id
	 * @returns {Observable<any>} Event status details
	*/
	getEventStatusDetails(userTimeZone: string, eventId: number): Observable<any> {
		return this.http.get(`${this.APP_EVENT_STATUS_DETAILS}/${eventId}`)
			.map((response: any) => {
				const result = pathOr(null, ['snapshot'], response);
				if (result) {
					result.eventStartDate = DateUtils.formatUserDateTime(userTimeZone, result.eventStartDate);

					if (result.planSum) {
						result.planSum.compTime = DateUtils.formatUserDateTime(userTimeZone, result.planSum.compTime);
					}
				}

				return result;
			})
			.catch((error: any) => error);
	}

	/**
	 * Call the endpoint to update the status details
 	 * @param {any} payload Status details
	 * @returns {Observable<any>}
	*/
	updateStatusDetails(payload: any): Observable<any> {
		return this.http.post(`${this.APP_EVENT_STATUS_UPDATE}`, JSON.stringify(payload))
			.catch((error: any) => {
				console.log(error);
				return error
			});
	}

	/**
	 * Retrive the full event list
	 * @returns {EventModel[]} list of events
	*/
	getEventsForList(): Observable<EventModel[]> {
		return this.http.get(`../ws/moveEvent/list`)
			.map((response: any) => {
				let eventModels = response && response.status === 'success' && response.data;
				let userTimeZone = this.preferenceService.getUserTimeZone();
				eventModels.forEach((r) => {
					r.estStartTime =  r.estStartTime ?
						DateUtils.toDateUsingFormat(DateUtils.convertFromGMT(r.estStartTime, userTimeZone), DateUtils.SERVER_FORMAT_DATE) : '';
					r.estCompletionTime =  r.estCompletionTime ?
						DateUtils.toDateUsingFormat(DateUtils.convertFromGMT(r.estCompletionTime, userTimeZone), DateUtils.SERVER_FORMAT_DATE) : '';
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
		return this.http.post(`../ws/moveEvent/save/${id}`, JSON.stringify(postObject))
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}

	deleteEvent(id): Observable<any> {
		return this.http.delete(`../ws/moveEvent/delete/${id}`)
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

	/**
	 * Get the relationship among categories and tasks
 	 * @param {number} eventId Event id
	 * @param {string} userTimeZone User Time Zone
	 * @param {string} plannedStart The Event Estimated Start
	 * @param {string} plannedCompletion The Event Estimated Completion
	  *@param {boolean} viewUnpublished Flag to filter unpublished events
	 * @returns {Observable<any>} Category status details
	*/
	getTaskCategoriesStats(eventId: number, userTimeZone: string, plannedStart: any, plannedCompletion: any, viewUnpublished: boolean): Observable<any> {
		return this.http.get(`${this.APP_EVENT_TASK_CATEGORY}/${eventId}?viewUnpublished=${viewUnpublished ? 1 : 0}`)
			.map((response: any) => this.formatTaskCategoryResults(response && response.data || [], userTimeZone, plannedStart, plannedCompletion))
			.catch((error: any) => error);
	}

	/**
	 * Take the raws category results and produce the structure that contains
	 * the relationship task category
 	 * @param {CategoryTask[]} data  Raw task category results
	 * @returns {any} Array of task category cells
	*/
	formatTaskCategoryResults(data: CategoryTask[], userTimeZone: string, plannedStart: any, plannedCompletion: any): any {
		const results: Array<Array<TaskCategoryCell>> = [];

		const headerRow: TaskCategoryCell[] = [];
		data.forEach((item: CategoryTask) => {
			headerRow.push({text: item.category, classes: 'empty-column'});
		});
		results.push(headerRow);

		const columnsLength = headerRow.length;
		results.push(this.getInitialTaskCategoriesCells(columnsLength, 'column-percents'));
		results.push(this.getInitialTaskCategoriesCells(columnsLength, 'secondary'));
		results.push(this.getInitialTaskCategoriesCells(columnsLength, 'primary estimated-start'));
		results.push(this.getInitialTaskCategoriesCells(columnsLength, 'primary estimated-completion'));
		results.push(this.getInitialTaskCategoriesCells(columnsLength, 'secondary actual-start'));
		results.push(this.getInitialTaskCategoriesCells(columnsLength, 'secondary actual-completion'));

		data.forEach((item: CategoryTask, index: number) => {
			results[CatagoryRowType.Percent][index].text =   item.percComp + '%';
			results[CatagoryRowType.Percent][index].compose =   item;
			results[CatagoryRowType.TaskCompleted][index].compose =   item;

			item.minEstStart = item.minEstStart ? item.minEstStart : plannedStart;
			item.maxEstFinish = item.maxEstFinish ? item.maxEstFinish : plannedCompletion;

			results[CatagoryRowType.PlannedStart][index].text = DateUtils.formatUserDateTime(userTimeZone, item.minEstStart);

			results[CatagoryRowType.PlannedCompletion][index].text = DateUtils.formatUserDateTime(userTimeZone, item.maxEstFinish);

			results[CatagoryRowType.ActualStart][index].text = DateUtils.formatUserDateTime(userTimeZone, item.minActStart);

			results[CatagoryRowType.ActualCompletion][index].text = DateUtils.formatUserDateTime(userTimeZone, item.maxActFinish);

			if (item.maxEstFinish && (DateUtils.stringDateToDate(item.maxActFinish) > DateUtils.stringDateToDate(item.maxEstFinish))) {
				results[CatagoryRowType.ActualStart][index].classes += ' task-overdue ';
				results[CatagoryRowType.ActualCompletion][index].classes += ' task-overdue ';
			}
		});

		const hasInfo = data.find((item: CategoryTask) => {
			return Boolean(item.minEstStart || item.maxEstFinish || item.minActStart || item.maxActFinish);
		});

		return {tasks: results, columns: columnsLength, hasInfo};
	}

	/**
	 * Get the initial task category cells
 	 * @param {number[]} lenght Number of categories
 	 * @param {string} classes Comma separated css classes
	 * @returns {any} Array of task category cells
	*/
	private getInitialTaskCategoriesCells(lenght: number, classes: string): TaskCategoryCell[] {
		const items: TaskCategoryCell[] = [];

		for (let i = 0; i < lenght; i++) {
			items.push({text: '', classes: classes})
		}

		return items;
	}
}
