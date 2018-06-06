import {Injectable} from '@angular/core';
import {Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Observable} from 'rxjs/Observable';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

/**
 * @name TaskService
 */
@Injectable()
export class TaskService {

	// private instance variable to hold base url
	private defaultUrl = '../ws';
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
}