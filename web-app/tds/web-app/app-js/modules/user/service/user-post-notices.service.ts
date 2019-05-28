/**
 * User Service is a top level service to retrieve information of the current user logged
 * For more specific endpoint, changes should be done on its specific module instead
 */
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';

import {StringUtils} from '../../../shared/utils/string.utils';

@Injectable()
export class UserPostNoticesService {
	private baseURL = '/tdstm/ws/notices';

	constructor(private http: HttpInterceptor) {
	}

	/**
	 * Get the user post notices
	 * @returns boolean
	*/
	getUserPostNotices(): Observable<any> {
		return this.http.get(`${this.baseURL}/fetchPostLoginNotices`)
			.map((res) => {
				let result = res.json();
				let notices = result.data && result.data.notices || [];
				notices.forEach( (notice: any) => {
					notice.htmlText = StringUtils.removeScapeSequences(notice.htmlText);
				});

				return result && result.data || [];
			})
			.catch((error: any) => error.json());
	}

	/**
    * Call the endpoint to se the flag that filter the notices pending
    * @returns any
    */
	notifyContinue(): Observable<any> {
		return this.http.get(`${this.baseURL}/continue`)
			.map((res) =>  res.json())
			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
	}

	/**
	 * Set the Acknowledge state for a notice
	 * @param {number} id:  Id of the notice
	 * @returns NoticeModel
	 */
	setAcknowledge(id: number): Observable<any> {
		return this.http.post(`${this.baseURL}/${id}/acknowledge`, '')
			.map((res) =>  {
				return res.json();
			})
			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
	}
}
