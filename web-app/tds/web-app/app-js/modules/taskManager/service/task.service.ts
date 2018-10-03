import {Injectable} from '@angular/core';
import {Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Observable} from 'rxjs';
import {SingleCommentModel} from '../../assetExplorer/components/single-comment/model/single-comment.model';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

/**
 * @name TaskService
 */
@Injectable()
export class TaskService {

	// private instance variable to hold base url
	private defaultUrl = '../ws';
	private assetGeneric = '../assetEntity';
	private taskURL = this.defaultUrl + '/task';

	// Resolve HTTP using the constructor
	constructor(private http: HttpInterceptor) {
	}

	/**
	 * Get the task count
	 * @returns {Observable<R>}
	 */
	retrieveUserToDoCount(): Observable<any> {
		return this.http.get('../task/retrieveUserToDoCount')
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
	}

	/**
	 * Get the Comment Categories
	 * @returns {Observable<any>}
	 */
	getCommentCategories(): Observable<any> {
		return this.http.get(`${this.taskURL}/assetCommentCategories`)
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
		return this.http.get(`${this.assetGeneric}/showComment?id=${taskId}`)
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
		return this.http.delete(`${this.taskURL}/comment/${commentId}`)
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
		return this.http.post(`${this.assetGeneric}/updateAssignedToSelect?format=json&forView=&id=${commentId}`, null)
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
		return this.http.get(`${this.defaultUrl}/task/taskCreateDefaults`)
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
			return this.http.post(`${this.taskURL}/comment`, JSON.stringify(request))
				.map((res: Response) => {
					let result = res.json();
					return result && result.status === 'success' && result.data && result.data.dataView;
				})
				.catch((error: any) => error.json());
		} else {
			request['id'] = model.id;
			return this.http.put(`${this.taskURL}/comment/${model.id}`, JSON.stringify(request))
				.map((res: Response) => {
					let result = res.json();
					return result && result.status === 'success' && result.data && result.data.dataView;
				})
				.catch((error: any) => error.json());
		}
	}
}