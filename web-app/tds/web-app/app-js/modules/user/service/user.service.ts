import {Injectable} from '@angular/core';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

/**
 * @name UserService
 */
@Injectable()
export class UserService {

	// private instance variable to hold base url
	private baseURL = '/tdstm';

	// Resolve HTTP using the constructor
	constructor(private http: HttpInterceptor) {
	}
}