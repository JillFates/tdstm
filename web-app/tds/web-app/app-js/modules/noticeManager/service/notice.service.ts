import {Injectable} from '@angular/core';
import {Response, Headers, RequestOptions} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {NotifierService} from '../../../shared/services/notifier.service';
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
					notice.typeId = notice.typeId.toString();
					notice.active = notice.active ? 'Yes' : 'No';
				});
				return result && result.notices;
			})
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

	deleteNotice(noticeId: NoticeModel): Observable<NoticeModel[]> {
		return this.http.delete(`${this.noticeListUrl}/${noticeId}`)
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

		if (column.type === 'text' || column.type === 'boolean') {
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

	/**
	 * Get the post Notices to process
	 * @returns any
	 */
	getPostNotices(): Observable<PostNoticeResponse> {
		const postNoticeUrl = '/tdstm/ws/notice/fetchPostNotices';

		return this.http.get(postNoticeUrl)
			.map((res: Response) => {
				let result = res.json();

				return result && result.data || [];
			})
			.catch((error: any) => error.json());

		/*
		const mockResponse = {
			redirectUri: '/tdstm/notices',
			notices: [
				{
					id: 5,
					title: 'EULA Aggreement 2018-19 version 1.0',
					htmlText: `
					<p>Last updated: (add date)</p>
					<p>
					Please read this End­User License Agreement ("Agreement") carefully before clicking the "I Agree"
					button, downloading or using My Application (change this) ("Application").
					By clicking the "I Agree" button, downloading or using the Application, you are agreeing to be bound
					by the terms and conditions of this Agreement.
					If you do not agree to the terms of this Agreement, do not click on the "I Agree" button and do not
					download or use the Application.
					</p>
					<h5>License</h5>
					<p>
					My Company (change this) grants you a revocable, non­exclusive, non­transferable, limited license
					to download, install and use the Application solely for your personal, non­commercial purposes
					strictly in accordance with the terms of this Agreement.
					</p>
					<h5>Restrictions</h5>
					<p>
					You agree not to, and you will not permit others to:
					a) license, sell, rent, lease, assign, distribute, transmit, host, outsource, disclose or otherwise
					commercially exploit the Application or make the Application available to any third party.
					</p>
					<h5>License</h5>
					<p>
					My Company (change this) grants you a revocable, non­exclusive, non­transferable, limited license
					to download, install and use the Application solely for your personal, non­commercial purposes
					strictly in accordance with the terms of this Agreement.
					</p>
					<h5>Restrictions</h5>
					<p>
					You agree not to, and you will not permit others to:
					a) license, sell, rent, lease, assign, distribute, transmit, host, outsource, disclose or otherwise
					commercially exploit the Application or make the Application available to any third party.
					</p>
					<h5>License</h5>
					<p>
					My Company (change this) grants you a revocable, non­exclusive, non­transferable, limited license
					to download, install and use the Application solely for your personal, non­commercial purposes
					strictly in accordance with the terms of this Agreement.
					</p>
					<h5>Restrictions</h5>
					<p>
					You agree not to, and you will not permit others to:
					a) license, sell, rent, lease, assign, distribute, transmit, host, outsource, disclose or otherwise
					commercially exploit the Application or make the Application available to any third party.
					</p>
					`,
					acknowledgeable: true,
					active: true,
					createdBy: null,
					lastModified: null,
					rawText: null,
					typeId: 2,
					activationDate: null,
					expirationDate: null,
					sequence: 2,
					locked: false,
					postMessageText: '',
					dateCreated: ''
				},
				{
					id: 7,
					title: 'Pizza to night in lobby',
					htmlText: 'Come one come all for some greasy pizza 1',
					acknowledgeable: false,
					active: true,
					createdBy: null,
					lastModified: null,
					rawText: null,
					typeId: 1,
					activationDate: null,
					expirationDate: null,
					sequence: 2,
					locked: false,
					postMessageText: '',
					dateCreated: ''
				},
				{
					id: 8,
					title: 'Another message',
					htmlText: 'Come one come all for some greasy pizza 2',
					acknowledgeable: false,
					active: true,
					createdBy: null,
					lastModified: null,
					rawText: null,
					typeId: 1,
					activationDate: null,
					expirationDate: null,
					sequence: 4,
					locked: false,
					postMessageText: '',
					dateCreated: ''
				},
				{
					id: 12,
					title: 'Maintenance Outage',
					htmlText: 'We will be updating the system at 11:00pm EST this evening - you have been warned!',
					acknowledgeable: false,
					active: true,
					createdBy: null,
					lastModified: null,
					rawText: null,
					typeId: 1,
					activationDate: null,
					expirationDate: null,
					sequence: 3,
					locked: false,
					postMessageText: '',
					dateCreated: ''
				}
			]
		};

		return Observable.of(mockResponse);
		*/
	}

}