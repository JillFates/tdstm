import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SingleCommentModel } from '../../assetExplorer/components/single-comment/model/single-comment.model';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import { ComboBoxSearchModel } from '../../../shared/components/combo-box/model/combobox-search-param.model';
import { ComboBoxSearchResultModel } from '../../../shared/components/combo-box/model/combobox-search-result.model';
import { catchError, map } from 'rxjs/operators';
import { TaskActionInfoModel } from '../model/task-action-info.model';

/**
 * @name TaskService
 */
@Injectable()
export class TaskService {
	// private instance variable to hold base url
	private baseURL = '/tdstm';
	private readonly TASK_LIST_URL = `${ this.baseURL }/ws/task/listTasks`;
	private readonly CUSTOM_COLUMNS_URL = `${ this.baseURL }/ws/task/customColumns`;
	private readonly TASK_ACTION_INFO_URL = `${ this.baseURL }/ws/task/getInfoForActionBar/{taskId}`;
	private readonly RESET_TASK_URL = `${ this.baseURL }/ws/task/{taskId}/resetAction`;
	private readonly TASK_ACTION_SUMMARY = `${ this.baseURL }/ws/task/{taskId}/actionLookUp`;

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient) {
	}

	/**
	 * Get the task count
	 * @returns {Observable<R>}
	 */
	retrieveUserToDoCount(): Observable<any> {
		return this.http.get(`${ this.baseURL }/task/retrieveUserToDoCount`)
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * Get the Task Details
	 * @returns {Observable<any>}
	 */
	getTaskDetails(taskId: string): Observable<any> {
		return this.http.get(`${ this.baseURL }/assetEntity/showComment?id=${ taskId }`)
			.map((response: any) => {
				return response && response[0];
			})
			.catch((error: any) => error);
	}

	/**
	 * Delete a Comment from a Task
	 * @returns {Observable<any>}
	 */
	deleteTaskComment(commentId: any): Observable<any> {
		return this.http.delete(`${ this.baseURL }/ws/asset/comment/${ commentId }`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the Current Team Assigned for the Comment
	 * @returns {Observable<any>}
	 */
	getAssignedTeam(commentId: any): Observable<any> {
		if ( commentId == null ) {
			return Observable.of([]);
		}

		return this.http.post(`${ this.baseURL }/assetEntity/updateAssignedToSelect?format=json&forView=&id=${ commentId }`, null)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the staff roles
	 * @returns {Observable<any>}
	 */
	getStaffRoles(): Observable<any> {
		return this.http.get(`${ this.baseURL }/task/retrieveStaffRoles`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the status list for the asset id provided
	 * @returns {Observable<any>}
	 */
	getStatusList(commentId: any): Observable<any> {
		if ( commentId == null ) {
			return Observable.of([]);
		}

		return this.http.post(`${ this.baseURL }/assetEntity/updateStatusSelect?format=json&id=${ commentId }`, null)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get Laste Created Task Params from Session
	 */
	getLastCreatedTaskSessionParams(): Observable<any> {
		return this.http.get(`${ this.baseURL }/ws/task/taskCreateDefaults`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Save the Task Cooment
	 * @param model
	 * @returns {Observable<any>}
	 */
	saveComment(model: SingleCommentModel): Observable<any> {
		const request: any = {
			comment: model.comment,
			category: model.category,
			isResolved: model.archive,
			assetEntityId: model.asset.id,
			status: 'Ready'
		};
		if (!model.id) {
			return this.http.post(`${ this.baseURL }/ws/asset/comment`, JSON.stringify(request))
				.map((response: any) => {
					return response && response.status === 'success' && response.data && response.data.dataView;
				})
				.catch((error: any) => error);
		} else {
			request['id'] = model.id;
			return this.http.put(`${ this.baseURL }/ws/asset/comment/${ model.id }`, JSON.stringify(request))
				.map((response: any) => {
					return response && response.status === 'success' && response.data && response.data.dataView;
				})
				.catch((error: any) => error);
		}
	}

	/**
	 *
	 * Search assets
	 * @param searchParams
	 * @returns {Observable<any>}
	 */
	getAssetListForComboBox(searchParams: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		return this.http.get(`${ this.baseURL }/assetEntity/assetListForSelect2?q=${ searchParams.query }
		&value=${ searchParams.value || '' }
		&max=${ searchParams.maxPage }
		&page=${ searchParams.currentPage }
		&assetClassOption=${ searchParams.metaParam }`)
			.map((response: any) => {
				let comboBoxSearchResultModel: ComboBoxSearchResultModel = {
					result: response.results,
					total: response.total,
					page: response.page
				};
				return comboBoxSearchResultModel;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the asset classes list
	 * @returns {Observable<any>}
	 */
	getAssetClasses(): Observable<any> {
		return this.http.get(`${ this.baseURL }/assetEntity/assetClasses`)
			.map((response: any) => {
				return response && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 *
	 * Returns a set of filtered tasks
	 * @param searchParams
	 * @returns {Observable<any>}
	 */
	getTasksForComboBox(searchParams: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		return this.searchTasks(searchParams)
			.map(response => {
				const result = response.result.filter((item) => item.id);
				return {
					result: result.map((item) =>
						({
							id: item.id,
							text: item.desc,
							metaFields: {
								category: item.category,
								status: item.status,
								taskNumber: item.taskNumber
							}
						})),
					total: response.total,
					page: response.page
				}
			});
	}

	/**
	 *
	 * Filter tasks
	 * @param searchParams
	 * @returns {Observable<any>}
	 */
	searchTasks(searchParams: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		const { metaParam, currentPage, maxPage, query } = searchParams;
		const params = [
			{ name: 'commentId', value: metaParam.commentId },
			{ name: 'page', value: currentPage },
			{ name: 'pageSize', value: maxPage },
			{ name: 'query', value: query }
		];
		if (metaParam.eventId) {
			params.unshift({ name: 'moveEvent', value: metaParam.eventId });
		}
		const queryString = params
			.map((param) => `${ param.name }=${ param.value }`)
			.join('&');
		return this.http.get(`${ this.baseURL }/assetEntity/tasksSearch?${ queryString }`)
			.map((response: any) => {
				let comboBoxSearchResultModel: ComboBoxSearchResultModel = {
					result: (response.data && response.data.list || []),
					total: response.data && response.data.total,
					page: response.page || currentPage
				};
				return comboBoxSearchResultModel;
			})
			.catch((error: any) => error);
	}

	/**
	 * Update a task
	 * @param payload
	 * @returns {Observable<any>}
	 */
	updateTask(payload: any): Observable<any> {
		const url = `${ this.baseURL }/assetEntity/updateComment`;
		return this.http.post(url, JSON.stringify(payload))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	changeTimeEst(id: string, days: string) {
		const url = `${ this.baseURL }/ws/task/${ id }/changeTime`;
		return this.http.post(url, JSON.stringify({ days }))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * Add note
	 * @param {string} id task
	 * @param {string} note
	 * @returns {Observable<any>}
	 */
	addNote(id: string, note: string): Observable<any> {
		const url = `${ this.baseURL }/ws/task/${ id }/addNote`;
		return this.http.post(url, JSON.stringify({ note }))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * Create a task
	 * @param payload model to create
	 * @returns {Observable<any>}
	 */
	createTask(payload: any): Observable<any> {
		const url = `${ this.baseURL }/assetEntity/saveComment`;
		return this.http.post(url, JSON.stringify(payload))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 *
	 * Get Asset Comment Categories
	 * @returns {Observable<any>}
	 */
	getAssetCommentCategories(): Observable<any[]> {
		return this.http.get(`${ this.baseURL }/ws/task/assetCommentCategories`)
			.map((response: any) => {
				return response && response.data || [];
			})
			.catch((error: any) => error);
	}

	/**
	 *
	 * Get events list
	 * @returns {Observable<any>}
	 */
	getEvents(): Observable<any[]> {
		return this.http.get(`${ this.baseURL }/ws/moveEvent/list`)
			.map((response: any) => {
				return response && response.data || [];
			})
			.catch((error: any) => error);
	}

	/**
	 * Update a task
	 * @param payload model to update
	 * @returns {Observable<any>}
	 */
	updateTaskStatus(payload: any): Observable<any> {
		return this.http.post(`${ this.baseURL }/task/update`, JSON.stringify(payload))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	updateStatus(id: string, status: string): Observable<any> {
		return this.http.post(`${ this.baseURL }/ws/task/${ id }/updateStatus`, JSON.stringify({ status }))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * Assign the task to the current user
	 * @param payload
	 * @returns {Observable<any>}
	 */
	assignToMe(payload: any): Observable<any> {
		return this.http.post(`${ this.baseURL }/task/assignToMe`, JSON.stringify(payload))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * Invoke an api action
	 * @param taskId
	 * @returns {Observable<any>}
	 */
	invokeAction(taskId: string): Observable<any> {
		return this.http.post(`${ this.baseURL }/ws/task/${ taskId }/invokeLocalAction`, '')
			.map((response: any) => response)
			.catch((error: any) => error.json());
	}

	/**
	 * Get the action list
	 * @returns {Observable<any>}
	 */
	getActionList(): Observable<any> {
		return this.http.get(`${ this.baseURL }/ws/apiAction`)
			.map((response: any) => response.data || [])
			.catch((error: any) => error);
	}

	/**
	 * Get the asset class that correspond to the provided asset
	 * @param assetId  Id of the provided asset
	 * @returns {Observable<any>}
	 */
	getClassForAsset(assetId: string): Observable<any> {
		if (assetId && assetId !== '0') {
			return this.http.get(`${ this.baseURL }/assetEntity/classForAsset?id=${ assetId }`)
				.map((response: any) => response.data || null)
				.catch((error: any) => error);
		}
		return Observable.of({});
	}

	/**
	 * Based on the asset name, get the corresponding category
	 * @param {string asstClass Full asset name
	 * @return {string} Corresponding category
	 */
	getAssetCategory(assetClass: string): string {
		const assetTypes = {
			'APPLICATION': 'APPLICATION',
			'DATABASE': 'DATABASE',
			'SERVER-DEVICE': 'DEVICE',
			'NETWORK-DEVICE': 'DEVICE',
			'STORAGE-DEVICE': 'DEVICE',
			'STORAGE-LOGICAL': 'STORAGE',
			'OTHER-DEVICE': 'DEVICE'
		};
		return assetTypes[assetClass];
	}

	/**
	 * POST - Get the List of Task presented on Task Management list.
	 * @param {any} filters  Object containing the filters to apply for
	 */
	getTaskList(filters: any): Observable<any> {
		return this.http.post(this.TASK_LIST_URL, filters).pipe(
			map((response: any) => {
				if (!response.rows || response.rows === null) {
					return {rows: [], totalCount: 0};
				}
				return response;
			}),
			catchError(error => {
				console.error(error);
				return error;
			})
		);
	}

	getBulkTaskActionInfo(taskIds: Array<number>): Observable<any> {
		return this.http.post(this.baseURL + '/ws/task/getBulkActionInfo', taskIds)
			.map((response: any) => {
				let data = response.data;
				let returnObj = {};
				if (!data) {
					return returnObj;
				}
				data.forEach(task => {
					if (task.taskId) {
						returnObj[task.taskId] = this.convertToTaskActionInfoModel(task);
					}
				});
				return returnObj;
			})
			.catch((error: any) => error);
	}

	/**
	 * GET - Get the custom columns tasks.
	 */
	getCustomColumns(): Observable<any> {
		return this.http.get(this.CUSTOM_COLUMNS_URL).pipe(
			map(response => response),
			catchError(error => {
				console.error(error);
				return error;
			})
		);
	}

	/**
	 * POST - Set the new custom columns configuration.
	 */
	setCustomColumn(oldColumn: string, newColumn: string, index: number): Observable<any> {
		const request = {
			columnValue: newColumn,
			from: index.toString(),
			previousValue: oldColumn,
			type: 'Task_Columns'
		};
		return this.http.post(this.CUSTOM_COLUMNS_URL, request).pipe(
			map(response => response),
			catchError(error => {
				console.error(error);
				return error;
			})
		);
	}

	/**
	 * GET - Get Task Information for the action bar grid.
	 * @param taskId: number
	 */
	getTaskActionInfo(taskId: number): Observable<any> {
		return this.http.get(this.TASK_ACTION_INFO_URL.replace('{taskId}', taskId.toString()))
			.pipe(map((response: any) => {
				return this.convertToTaskActionInfoModel(response.data || response);
				}),
				catchError(error => {
					console.error(error);
					return error;
				})
			);
	}

	/**
	 * Converts raw response data from getTaskActionInfo/getTaskList into TaskActionInfoModel object
	 * @param actionBarInfo - The object that contains all the data for the TaskActionInfoModel
	 */
	convertToTaskActionInfoModel(actionBarInfo: any): TaskActionInfoModel {
		let result: TaskActionInfoModel = {
			predecessors: actionBarInfo.predecessorsCount || 0,
			successors: actionBarInfo.successorsCount || 0,
			assignedTo: actionBarInfo.assignedTo,
			assignedToName:  actionBarInfo.assignedToName,
			apiActionId: actionBarInfo.apiActionId,
			apiActionCompletedAt: actionBarInfo.apiActionCompletedAt,
			apiActionInvokedAt: actionBarInfo.apiActionInvokedAt,
			category: actionBarInfo.category,
			status: actionBarInfo.status,
			instructionsLinkURL: actionBarInfo.instructionsLinkURL,
			instructionsLinkLabel: actionBarInfo.instructionsLinkLabel
		};
		if (actionBarInfo.invokeActionDetails) {
			result.invokeButton = { ...actionBarInfo.invokeActionDetails };
		}
		return result;
	}

	/**
	 * POST - Reset Task Action
	 * @param taskId: number
	 */
	resetTaskAction(taskId: number): Observable<any> {
		return this.http.post(this.RESET_TASK_URL.replace('{taskId}', taskId.toString()), null)
			.pipe(
				map(response => response),
				catchError(error => {
					console.error(error);
					return error;
				})
			);
	}

	/**
	 * GET - Returns the task api action summary.
	 * @param taskId
	 */
	getTaskActionSummary(taskId: string): Observable<any> {
		return this.http.get(this.TASK_ACTION_SUMMARY.replace('{taskId}', taskId.toString()))
			.pipe(
				map(response => response),
				catchError(error => {
					console.error(error);
					return error;
				})
			);
	}
}
