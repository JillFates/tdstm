// Angular
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

// Others
import {Observable} from 'rxjs';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {NoticeModel} from '../../noticeManager/model/notice.model';
import {StringUtils} from '../../../shared/utils/string.utils';

/**
 * @name LoginService
 */
@Injectable()
export class LoginService {

	// private instance variable to hold base url
	private authUrl = '../auth/';

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient) {
	}

	/**
	 * Get the Login Basic Information
	 * @returns {Observable<R>}
	 */
	public getLoginInfo(): Observable<any[]> {
		return this.http.get(this.authUrl + 'loginInfo')
			.map((response: any) => {
				if (response.data && response.data.notices) {
					response.data.notices = response.data.notices.map((notice: any) => {
						return this.cleanNotice(notice);
					});
				}
				return response && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Send an email to restore password
	 * @returns {Observable<R>}
	 */
	public forgotPassword(userEmail: string): Observable<any[]> {
		return this.http.get(`${this.authUrl}sendResetPasswordEmail?email=${userEmail}`)
			.map((response: any) => {
				return response && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Clean the html content coming in the notice(s) removing the scape sequences
	 * @returns {any}
	 */
	private cleanNotice(notice: NoticeModel): any {
		notice.typeId = notice.typeId.toString();
		notice.htmlText = StringUtils.removeScapeSequences(notice.htmlText);
		return notice;
	}
}
