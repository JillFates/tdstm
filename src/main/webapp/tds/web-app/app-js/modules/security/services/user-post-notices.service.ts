/**
 * User Service is a top level service to retrieve information of the current user logged
 * For more specific endpoint, changes should be done on its specific module instead
 */
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';

import {StringUtils} from '../../../shared/utils/string.utils';

@Injectable()
export class UserPostNoticesService {
	private baseURL = '/tdstm/ws/notices';

	constructor(private http: HttpClient) {
	}

	/**
	 * Get the user post notices
	 * @returns boolean
	*/
	getUserPostNotices(): Observable<any> {
		return this.http.get(`${this.baseURL}/fetchPostLoginNotices`)
			.map((result: any) => {
				let notices = result.data && result.data.notices || [];
				notices.forEach( (notice: any) => {
					notice.htmlText = StringUtils.removeScapeSequences(notice.htmlText);
				});

				return result && result.data || [];
			})
			.catch((error: any) => error);
	}

	/**
    * Call the endpoint to se the flag that filter the notices pending
    * @returns any
    */
	notifyContinue(): Observable<any> {
		return this.http.get(`${this.baseURL}/continue`)
			.map((result: any) =>  result)
			.catch((error: any) => error);
	}

	/**
	 * Set the Acknowledge state for a notice
	 * @param {number} id:  Id of the notice
	 * @returns NoticeModel
	 */
	setAcknowledge(id: number): Observable<any> {
		return this.http.post(`${this.baseURL}/${id}/acknowledge`, '')
			.map((result: any) =>  {
				return result;
			})
			.catch((error: any) => error);
	}
}
