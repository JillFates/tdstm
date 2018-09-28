import {Injectable} from '@angular/core';
import {Response, RequestOptions, Headers} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Observable} from 'rxjs/Observable';
import {SingleCommentModel} from '../../assetExplorer/components/single-comment/model/single-comment.model';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {ComboBoxSearchModel} from '../../../shared/components/combo-box/model/combobox-search-param.model';
import {ComboBoxSearchResultModel} from '../../../shared/components/combo-box/model/combobox-search-result.model';
import {DateUtils} from '../../../shared/utils/date.utils';

/**
 * @name TaskService
 */
@Injectable()
export class TaskService {

	// private instance variable to hold base url
	private baseURL = '/tdstm';

	// Resolve HTTP using the constructor
	constructor(private http: HttpInterceptor) {
	}

	/**
	 * Get the task count
	 * @returns {Observable<R>}
	 */
	retrieveUserToDoCount(): Observable<any> {
		return this.http.get(`${this.baseURL}/task/retrieveUserToDoCount`)
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
	}

	/**
	 * Get the Comment Categories
	 * @returns {Observable<any>}
	 */
	getCommentCategories(): Observable<any> {
		return this.http.get(`${this.baseURL}/ws/task/assetCommentCategories`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Get the Task Details
	 * @returns {Observable<any>}
	 */
	getTaskDetails(taskId: string): Observable<any> {
		return this.http.get(`${this.baseURL}/assetEntity/showComment?id=${taskId}`)
			.map((res: Response) => {
				let result = res.json();
				return result && result[0];
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Delete a Comment from a Task
	 * @returns {Observable<any>}
	 */
	deleteTaskComment(commentId: any): Observable<any> {
		return this.http.delete(`${this.baseURL}/ws/task/comment/${commentId}`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Get the Current Team Assigned for the Comment
	 * @returns {Observable<any>}
	 */
	getAssignedTeam(commentId: any): Observable<any> {
		return this.http.post(`${this.baseURL}/assetEntity/updateAssignedToSelect?format=json&forView=&id=${commentId}`, null)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Get the staff roles
	 * @returns {Observable<any>}
	 */
	getStaffRoles(): Observable<any> {
		return this.http.get(`${this.baseURL}/task/retrieveStaffRoles`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Get the status list for the asset id provided
	 * @returns {Observable<any>}
	 */
	getStatusList(commentId: any): Observable<any> {
		return this.http.post(`${this.baseURL}/assetEntity/updateStatusSelect?format=json&id=${commentId}`, null)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Get Laste Created Task Params from Session
	 */
	getLastCreatedTaskSessionParams(): Observable<any> {
		return this.http.get(`${this.baseURL}/ws/task/taskCreateDefaults`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
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
			return this.http.post(`${this.baseURL}/ws/task/comment`, JSON.stringify(request))
				.map((res: Response) => {
					let result = res.json();
					return result && result.status === 'success' && result.data && result.data.dataView;
				})
				.catch((error: any) => error.json());
		} else {
			request['id'] = model.id;
			return this.http.put(`${this.baseURL}/ws/task/comment/${model.id}`, JSON.stringify(request))
				.map((res: Response) => {
					let result = res.json();
					return result && result.status === 'success' && result.data && result.data.dataView;
				})
				.catch((error: any) => error.json());
		}
	}
	/**
	 *
	 * @param searchParams
	 * @returns {Observable<any>}
	 */
	getAssetListForComboBox(searchParams: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		return this.http.get(`${this.baseURL}/assetEntity/assetListForSelect2?q=${searchParams.query}&value=${searchParams.value || ''}&max=${searchParams.maxPage}&page=${searchParams.currentPage}&assetClassOption=${searchParams.metaParam}`)
			.map((res: Response) => {
				let response = res.json();
				let comboBoxSearchResultModel: ComboBoxSearchResultModel = {
					result: response.results,
					total: response.total,
					page: response.page
				};
				return comboBoxSearchResultModel;
			})
			.catch((error: any) => error.json());
	}

	/**
	 *
	 * @param searchParams
	 * @returns {Observable<any>}
	 */
	getTasksForComboBox(searchParams: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		const {metaParam, currentPage, maxPage, query} = searchParams;
		const params = [
			{name: 'commentId', value: metaParam},
			{name: 'page', value: currentPage},
			{name: 'pageSize', value: maxPage},
			{name: 'filter[filters][0][value]', value: query }
		];
		const queryString = params
			.map((param) =>  `${param.name}=${param.value}`)
			.join('&');

		return this.http.get(`${this.baseURL}/assetEntity/tasksSearch?${queryString}`)
			.map((res: Response) => {
				let response = res.json();
				let comboBoxSearchResultModel: ComboBoxSearchResultModel = {
					result: (response.data && response.data.list || []).map((item) => ({id: item.id, text: item.desc})),
					total: response.data && response.total,
					page: response.page || currentPage
				};
				return comboBoxSearchResultModel;
			})
			.catch((error: any) => error.json());
	}
	/**
	 * Update a task
	 * @param model
	 * @returns {Observable<any>}
	 */
	updateTask(payload: any): Observable<any> {
		// const params = [
		// 	{name: 'assetClass', value: model.assetClass.id},
		// 	{name: 'assetEntity', value: model.asset.id},
		// 	{name: 'assetType', value: 'Application'}, /* ? */
		// 	{name: 'assignedTo', value: model.assignedTo.id},
		// 	{name: 'category', value: model.category},
		// 	{name: 'apiAction', value: model.apiAction.id},
		// 	{name: 'actionInvocable', value: ''},
		// 	{name: 'actionMode', value: ''},
		// 	{name: 'comment', value: model.comment},
		// 	{name: 'commentFromId', value: ''},
		// 	{name: 'commentId', value: model.id},
		// 	{name: 'commentType', value: 'issue'},
		// 	{name: 'deletePredId', value: ''},  /* ? */
		// 	{name: 'dueDate', value: model.dueDate ? model.dueDate.toISOString() : ''},
		// 	{name: 'duration', value: DateUtils.convertDurationPartsToMinutes(model.durationParts)},
		// 	{name: 'durationScale', value: model.durationScale},
		// 	{name: 'estFinish', value: model.estimatedFinish ? model.estimatedFinish.toISOString() : ''},
		// 	{name: 'estStart', value: model.estimatedStart ? model.estimatedStart.toISOString() : ''},
		// 	{name: 'forWhom', value: ''},
		// 	{name: 'hardAssigned', value: model.hardAssigned === 'No' ? 0 : 1},
		// 	{name: 'sendNotification', value: model.sendNotification === 'No' ? 0 : 1},
		// 	{name: 'isResolved', value: 0}, /* ? */
		// 	{name: 'instructionsLink', value: model.instructionLink},
		// 	{name: 'manageDependency', value: 1}, /* ? */
		// 	{name: 'moveEvent', value: model.event.id},
		// 	{name: 'mustVerify', value: 0}, /* ? */
		// 	{name: 'override', value: 0}, /* ? */
		// 	{name: 'predCount', value: -1}, /* ? */
		// 	{name: 'predecessorCategory', value: ''}, /* ? */
		// 	{name: 'prevAsset', value: ''}, /* ? */
		// 	{name: 'priority', value: model.priority}, /* ? */
		// 	{name: 'resolution', value: ''}, /* ? */
		// 	{name: 'role', value: model.assignedTeam.id},
		// 	{name: 'status', value: model.status},
		// 	{name: 'taskDependendy[]', value: '3002_233386'}, /* ? */
		// 	{name: 'taskSuccessor[]', value: '3012_233398'}, /* ? */
		// 	{name: 'workflowTransition', value: ''}, /* ? */
		// 	{name: 'canEdit', value: true}, /* ? */
		// 	{name: 'durationLocked', value: model.durationLocked},
		// 	{name: 'durationText', value: `${model.durationParts.days} days ${model.durationParts.hours} hrs ${model.durationParts.minutes} mins`},
		// 	{name: 'taskNumber', value: model.taskNumber},
		// 	{name: 'note', value: model.note},
		// 	{name: 'id', value: model.id},
		// 	{name: 'apiActionId', value: model.apiAction.id || 0},
		// 	{name: 'deletedPreds', value: ''}
		// ];

		/*
		const body = params
			.map((item) => `${item.name}=${item.value}`)
			.join('&');
		*/

		const url = `${this.baseURL}/assetEntity/updateComment`;
		return this.http.post(url, JSON.stringify(payload))
			.map(res => res.ok)
			.catch((error: any) => error);
	}

}