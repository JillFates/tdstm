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

}