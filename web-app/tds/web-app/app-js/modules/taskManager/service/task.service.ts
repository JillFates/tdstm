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
	 * Search assets
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
	 * Get the asset classes list
	 * @returns {Observable<any>}
	 */
	getAssetClasses(): Observable<any> {
		return this.http.get(`${this.baseURL}/assetEntity/assetClasses`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 *
	 * Returns a set of filtered tasks
	 * @param searchParams
	 * @returns {Observable<any>}
	 */
	getTasksForComboBox(searchParams: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		return this.searchTasks(searchParams)
			.map(res => {
				return {
					result: res.result.map((item) =>
						({id: item.id, text: item.desc, metaFields: {category: item.category, status: item.status, taskNumber: item.taskNumber}})),
					total: res.total,
					page: res.page
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
					result: (response.data && response.data.list || []),
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
		const url = `${this.baseURL}/assetEntity/updateComment`;
		return this.http.post(url, JSON.stringify(payload))
			.map(res =>  res && res.json())
			.catch((error: any) => error);
	}

	/**
	 * Create a task
	 * @param payload model to create
	 * @returns {Observable<any>}
	 */
	createTask(payload: any): Observable<any> {
		const url = `${this.baseURL}/assetEntity/saveComment`;
		return this.http.post(url, JSON.stringify(payload))
			.map(res =>  res && res.json())
			.catch((error: any) => error);
	}

	/**
	 *
	 * Get categories
	 * @returns {Observable<any>}
	 */
	getCategories(): Observable<any[]> {
		return this.http.get(`${this.baseURL}/ws/task/assetCommentCategories`)
			.map((res: Response) => {
				let response = res.json();
				return response && response.data || [];

			})
			.catch((error: any) => error.json());
	}

	/**
	 *
	 * Get events list
	 * @returns {Observable<any>}
	 */
	getEvents(): Observable<any[]> {
		return this.http.get(`${this.baseURL}/ws/moveEvent/list`)
			.map((res: Response) => {
				let response = res.json();
				return response && response.data || [];

			})
			.catch((error: any) => error.json());
	}

	/**
	 * Update a task
	 * @param payload model to update
	 * @returns {Observable<any>}
	 */
	updateTaskStatus(payload: any): Observable<any> {
		return this.http.post(`${this.baseURL}/task/update`, JSON.stringify(payload))
			.map((res: Response) => {
				let result = res.json();
				return result;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Assign the task to the current user
	 * @param payload
	 * @returns {Observable<any>}
	 */
	assignToMe(payload: any): Observable<any> {
		return this.http.post(`${this.baseURL}/task/assignToMe`, JSON.stringify(payload))
			.map((res: Response) => {
				let result = res.json();
				return result;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Invoke an api action
	 * @param taskId
	 * @returns {Observable<any>}
	 */
	invokeAction(taskId: string): Observable<any> {
		return this.http.post(`${this.baseURL}/ws/task/${taskId}/invokeAction`, '')
			.map((res: Response) => {
				let result = res.json();
				return result;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Get the action list
	 * @returns {Observable<any>}
	 */
	getActionList(): Observable<any> {
		return this.http.get(`${this.baseURL}/ws/apiAction`)
			.map((res: Response) => {
				let result = res.json();
				return result.data || [];
			})
			.catch((error: any) => error.json());
	}

}