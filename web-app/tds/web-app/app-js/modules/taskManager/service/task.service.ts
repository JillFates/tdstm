import {Injectable} from '@angular/core';
import {Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Observable} from 'rxjs/Observable';
import {SingleCommentModel} from '../../assetExplorer/components/single-comment/model/single-comment.model';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {ComboBoxSearchModel} from '../../../shared/components/combo-box/model/combobox-search-param.model';
import {ComboBoxSearchResultModel} from '../../../shared/components/combo-box/model/combobox-search-result.model';

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
		return this.http.get(`${this.baseURL}/task/assetCommentCategories`)
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
		return this.http.delete(`${this.baseURL}/task/comment/${commentId}`)
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
		return this.http.get(`${this.baseURL}/task/taskCreateDefaults`)
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
			return this.http.post(`${this.baseURL}/task/comment`, JSON.stringify(request))
				.map((res: Response) => {
					let result = res.json();
					return result && result.status === 'success' && result.data && result.data.dataView;
				})
				.catch((error: any) => error.json());
		} else {
			request['id'] = model.id;
			return this.http.put(`${this.baseURL}/task/comment/${model.id}`, JSON.stringify(request))
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
		const params = [];
		const filters = [];

		params.push({name: 'category', value: 'general'});
		params.push({name: 'commentId', value: '233347'});
		params.push({name: 'take', value: searchParams.maxPage});
		params.push({name: 'skip', value: 0});
		params.push({name: 'page', value: searchParams.currentPage});
		params.push({name: 'pageSize', value: searchParams.maxPage});

		filters.push({name: 'value', value: searchParams.query });
		filters.push({name: 'field', value: 'desc'});
		filters.push({name: 'operator', value: 'contains'});
		filters.push({name: 'ignoreCase', value: true});
		filters.push({name: 'login', value: 'and'});

		const queryString = params
			.map((param) =>  `${param.name}=${param.value}&`)
			.concat(filters.map((filter) => `filter[filters][0][${filter.name}]=${filter.value}&`));


		// return this.http.get(`../${this.baseURL}/assetEntity/tasksSearch?q=${searchParams.query}&value=${searchParams.value}&max=${searchParams.maxPage}&page=${searchParams.currentPage}&assetClassOption=${searchParams.metaParam}`)
		return this.http.get(`../${this.baseURL}/assetEntity/tasksSearch?${queryString}`)
			.map((res: Response) => {
				let response = res.json();
				let comboBoxSearchResultModel: ComboBoxSearchResultModel = {
					result: response.data && response.data.list,
					total: response.data && response.total,
					page: response.page || searchParams.currentPage
				};
				return comboBoxSearchResultModel;
			})
			.catch((error: any) => error.json());
	}
}