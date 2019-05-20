import {Injectable} from '@angular/core';
import {Response, Headers, RequestOptions} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {StringUtils} from '../../../shared/utils/string.utils';
import {Observable} from 'rxjs/Observable';

import {NoticeModel, PostNoticeResponse} from '../model/notice.model';

import 'rxjs/add/operator/map';

import 'rxjs/add/operator/catch';
import {Flatten} from '../../../shared/model/data-list-grid.model';
import {DateUtils} from '../../../shared/utils/date.utils';

/**
 * @name NoticeService
 */
@Injectable()
export class NoticeService {

	// private instance variable to hold base url
	private noticeListUrl = '../ws/notices';
	private singleNoticeUrl = '/tdstm/ws/notices';

	// Resolve HTTP using the constructor
	constructor(private http: HttpInterceptor) {
	}

	/**
	 * Get the Notice List
	 * @returns {Observable<R>}
	 */
	getNoticesList(): Observable<NoticeModel[]> {
		return this.http.get(this.noticeListUrl)
			.map((res: Response) => {
				let result = res.json();
				result.notices.forEach( (notice: any) => {
					notice = this.cleanNotice(notice);
				});
				return result && result.notices;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Get a specific notice
	 * @returns {Observable<R>}
	 * @param {number} id: Notice id to fetch
	 */
	getNotice(id: number): Observable<NoticeModel> {
		return this.http.get(`${this.noticeListUrl}/${id}`)
			.map((res: Response) => {
				return this.cleanNotice(res.json());
			})
			.catch((error: any) => error.json());
	}

	createNotice(notice: NoticeModel): Observable<NoticeModel[]> {
		return this.http.post(this.noticeListUrl, JSON.stringify(notice))
			.map((res: Response) => this.handleJSONError(res))
			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
	}

	editNotice(notice: NoticeModel): Observable<NoticeModel[]> {
		return this.http.put(`${this.noticeListUrl}/${notice.id}`, JSON.stringify(notice))
			.map((res: Response) => this.handleJSONError(res))
			.catch((error: any) => Observable.throw(error || 'Server error'));
	}

	deleteNotice(id: string): Observable<NoticeModel[]> {
		return this.http.delete(`${this.noticeListUrl}/${id}`)
			.map((res: Response) => this.handleJSONError(res))
			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
	}

	/**
	 * Update the filters state structure removing the column filter provided
	 * @param {any} column: Column to exclude from filters
	 * @param {any} state: Current filters state
	 * @returns void
	 */
	clearFilter(column: any, state: any): void {
		column.filter = '';
		state.filter.filters = this.getFiltersExcluding(column.property, state);
	}

	/**
	 * Get the filters state structure excluding the column filter name provided
	 * @param {string} excludeFilterName:  Name of the filter column to exclude
	 * @param {any} state: Current filters state
	 * @returns void
	 */
	getFiltersExcluding(excludeFilterName: string, state: any): any {
		const filters = (state.filter && state.filter.filters) || [];
		return  filters.filter((r) => r['field'] !== excludeFilterName);
	}

	/**
	 * Get the post Notices to process
	 * @returns any
	 */
	getPostNotices(): Observable<PostNoticeResponse> {
		return this.http.get(`${this.singleNoticeUrl}/fetchPostLoginNotices`)
			.map((res: Response) => {
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
	 * Set the Acknowledge state for a notice
	 * @param {number} id:  Id of the notice
	 * @returns NoticeModel
	 */
	setAcknowledge(id: number): Observable<NoticeModel> {
		return this.http.post(`${this.singleNoticeUrl}/${id}/acknowledge`, '')
			.map((res: Response) =>  {
				return res.json();
			})
			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
	}

	/**
	 * Check the response, in case this is an JSON error coming from the server, respond appripately
	 * @param {Respone} res:  Service Response
	 * @returns any : Response in JSON format / or error
	 */
	private handleJSONError(res: Response): any {
		const result = res.json();

		if (result && result.status === 'error') {
			throw new Error(result);
		} else {
			return result
		}
	}

	/**
	 * Set the default values for empty fields, and clean html coming with escape sequences
	 * @param {NoticeModel} notice:  Notice received
	 * @returns any : Notice with the default values and html content in place
	 */
	private cleanNotice(notice: NoticeModel): any {
		notice.typeId = notice.typeId.toString();
		notice.expirationDate = notice.expirationDate ? new Date(notice.expirationDate) : '';
		notice.activationDate = notice.activationDate ? new Date(notice.activationDate) : '';
		notice.htmlText = StringUtils.removeScapeSequences(notice.htmlText);

		return notice;
	}

}