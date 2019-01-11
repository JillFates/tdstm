import {Injectable} from '@angular/core';
import {Response, Headers, RequestOptions} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {NotifierService} from '../../../shared/services/notifier.service';
import {NoticeModel} from '../model/notice.model';
import {Observable} from 'rxjs/Observable';

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
	constructor(private http: HttpInterceptor) {
	}

	/**
	 * Get the Notice List
	 * @returns {Observable<R>}
	 */
	getNoticesList(): Observable<NoticeModel[]> {
		return this.http.get(this.noticeListUrl)
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
	}

	createNotice(notice: NoticeModel): Observable<NoticeModel[]> {
		return this.http.post(this.noticeListUrl, JSON.stringify(notice))
			.map((res: Response) => res.json())
			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
	}

	editNotice(notice: NoticeModel): Observable<NoticeModel[]> {
		return this.http.put(`${this.noticeListUrl}/${notice.id}`, JSON.stringify(notice))
			.map((res: Response) => res.json())
			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
	}

	deleteNotice(notice: NoticeModel): Observable<NoticeModel[]> {
		return this.http.delete(`${this.noticeListUrl}/${notice.id}`)
			.map((res: Response) => res.json())
			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
	}

	/**
	 * Based on provided column, update the structure which holds the current selected filters
	 * @param {any} column: Column to filter
	 * @param {any} state: Current filters state
	 * @returns {any} Filter structure updated
	 */
	filterColumn(column: any, state: any): any {
		let root = state.filter || { logic: 'and', filters: [] };

		let [filter] = Flatten(root).filter(item => item.field === column.property);

		if (!column.filter) {
			column.filter = '';
		}

		if (column.type === 'text') {
			if (!filter) {
				root.filters.push({
					field: column.property,
					operator: 'contains',
					value: column.filter,
					ignoreCase: true
				});
			} else {
				filter = root.filters.find((r) => {
					return r['field'] === column.property;
				});
				filter.value = column.filter;
			}
		}

		return root;
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
}