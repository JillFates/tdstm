/**
 * User Service is a top level service to retrieve information of the current user logged
 * For more specific endpoint, changes should be done on its specific module instead
 */
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';

import {StringUtils} from '../../../shared/utils/string.utils';
import {NoticeModel} from '../../noticeManager/model/notice.model';

@Injectable()
export class PostNoticesService {
	private baseURL = '/tdstm/ws/notices';

	constructor(private http: HttpClient) {
	}

	/**
	 * Get the user post notices
	 * @returns boolean
	*/
	public getPostNotices(): Observable<any> {
		return this.http.get(`${this.baseURL}/fetchPostLoginNotices`)
			.map((result: any) => {
				let notices = result.data && result.data.notices || [];
				// Process Notices and Clean it
				notices = notices.map((notice: NoticeModel) => {
					notice.htmlText = StringUtils.removeScapeSequences(notice.htmlText);
					notice.sequence = notice.sequence || 0;
					return notice;
				});
				return notices;
			})
			.catch((error: any) => error);
	}

	/**
    * Call the endpoint to se the flag that filter the notices pending
    * @returns any
    */
	notifyContinue(): Observable<any> {
		return this.http.get(`${this.baseURL}/continue`)
			.map((result: any) =>  result);
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
