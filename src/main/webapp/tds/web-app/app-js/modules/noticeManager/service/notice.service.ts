import {Injectable} from '@angular/core';
import {Response, Headers, RequestOptions} from '@angular/http';
import {HttpClient} from '@angular/common/http';
import {StringUtils} from '../../../shared/utils/string.utils';
import {Observable} from 'rxjs/Observable';

import {NoticeModel, PostNoticeResponse, NOTICE_TYPE_POST_LOGIN, NOTICE_TYPE_MANDATORY} from '../model/notice.model';

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

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient) {
	}

	/**
	 * Get the Notice List
	 * @returns {Observable<R>}
	 */
	getNoticesList(): Observable<NoticeModel[]> {
		return this.http.get(this.noticeListUrl)
			.map((result: any) => {
				result.notices.forEach( (notice: any) => {
					notice = this.cleanNotice(notice);
					if (notice.typeId === NOTICE_TYPE_POST_LOGIN && notice.needAcknowledgement) {
						notice.typeId = NOTICE_TYPE_MANDATORY;
					}
				});
				return result && result.notices;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get a specific notice
	 * @returns {Observable<R>}
	 * @param {number} id: Notice id to fetch
	 */
	getNotice(id: number): Observable<NoticeModel> {
		return this.http.get(`${this.noticeListUrl}/${id}`)
			.map((res: any) => {
				return this.cleanNotice(res);
			})
			.catch((error: any) => error);
	}

	createNotice(notice: NoticeModel): Observable<any> {
		return this.http.post(this.noticeListUrl, JSON.stringify(notice))
			.map((res: any) => res)
			.catch((error: any) => error);
	}

	editNotice(notice: NoticeModel): Observable<any> {
		return this.http.put(`${this.noticeListUrl}/${notice.id}`, JSON.stringify(notice))
			.map((res: any) => res)
			.catch((error: any) => error);
	}

	deleteNotice(id: string): Observable<any> {
		return this.http.delete(`${this.noticeListUrl}/${id}`)
			.map((res: any) => res)
			.catch((error: any) => error);
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
	 * Set the default values for empty fields, and clean html coming with escape sequences
	 * @param {NoticeModel} notice:  Notice received
	 * @returns any : Notice with the default values and html content in place
	 */
	private cleanNotice(notice: NoticeModel): any {
		notice.typeId = notice.typeId.toString();
		notice.htmlText = StringUtils.removeScapeSequences(notice.htmlText);
		notice.activationDate = notice.activationDate
			? DateUtils.toDateUsingFormat(DateUtils.getDateFromGMT(notice.activationDate), DateUtils.SERVER_FORMAT_DATE) : '';

		notice.expirationDate = notice.expirationDate
			? DateUtils.toDateUsingFormat(DateUtils.getDateFromGMT(notice.expirationDate), DateUtils.SERVER_FORMAT_DATE) : '';

		return notice;
	}
}