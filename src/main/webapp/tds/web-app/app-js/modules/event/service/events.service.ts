import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {pathOr} from 'ramda'

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {catchError, map} from 'rxjs/operators';

import {EventModel} from '../model/event.model';
import {NewsModel, NewsDetailModel} from '../model/news.model';

/**
 * @name EventsService
 */
@Injectable()
export class EventsService {

	// private instance variable to hold base url
	private readonly baseURL = '/tdstm';
	private readonly APP_EVENT_LISTS_URL = `${this.baseURL}/ws/moveEvent/list`;
	private readonly APP_EVENT_NEWS = `${this.baseURL}/ws/moveEventNews`;
	private readonly APP_EVENT_UPDATE_NEWS = `${this.baseURL}/newsEditor/updateNews`;
	private readonly APP_EVENT_SAVE_NEWS = `${this.baseURL}/newsEditor/saveNews`;
	private readonly APP_EVENT_DELETE_NEWS = `${this.baseURL}/newsEditor/deleteNews`;
	private readonly APP_EVENT_NEWS_DETAIL = `${this.baseURL}/newsEditor/retrieveCommetOrNewsData`;
	private readonly APP_EVENT_LIST_BUNDLES = `${this.baseURL}/ws/event/listBundles`;
	private readonly APP_EVENT_STATUS_DETAILS = `${this.baseURL}/ws/dashboard/bundleData`;
	private readonly APP_EVENT_DETAILS = `${this.baseURL}/ws/moveEvent/dashboardModel`;
	private readonly APP_EVENT_STATUS_UPDATE = `${this.baseURL}/moveEvent/updateEventSumamry`;

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient) {
	}

	/**
	 * Get events list
	 * @returns {Observable<any>}
	 * TODO: @sam please use the previously already implemented getEvents() from task.service.ts.
	 */
	getEvents(): Observable<EventModel[]> {
		return this.http.get(`${this.APP_EVENT_LISTS_URL}`)
			.map((response: any) => {
				return response && response.data || [];
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the news associated to the event
 	 * @param {number} eventId
	 * @returns {Observable<NewsModel[]>}
	 */
	getNewsFromEvent(eventId: number): Observable<NewsModel[]> {
		return this.http.get(`${this.APP_EVENT_NEWS}/${eventId}`)
			.map((response: any) => {
				return response || [];
			})
			.catch((error: any) => error);
	}

	getNewsDetail(newsId: number): Observable<NewsDetailModel> {
		return this.http.get(`${this.APP_EVENT_NEWS_DETAIL}/?id=${newsId}&commentType=N`)
			.map((response: any) => {
				return response && response.shift() || null;
			})
			.catch((error: any) => error);
	}

	getEventDetails(event: number, viewUnpublished: boolean): Observable<any> {
		const url = `${this.APP_EVENT_DETAILS}/?moveEvent=${event}&viewUnpublished=${viewUnpublished ? 1 : 0}`;
		return this.http.get(url)
			.map((response: any) => {
				const model = pathOr(null, ['data', 'model'], response);
				if (model) {
					return {
						remainTaskCount: model.remainTaskCount || 0,
						percTaskDone: model.percTaskDone  || 0,
						percTaskReady: model.percTaskReady  || 0,
						percTaskStarted: model.percTaskStarted || 0,
						countDone: model.countDone || 0,
						countStarted: model.countStarted || 0,
						countReady: model.countReady || 0,
						countHold: model.countHold || 0,
						countPending: model.countPending || 0,
						effortRemainDone: model.effortRemainDone || '',
						effortRemainPending: model.effortRemainPending || '',
						effortRemainReady: model.effortRemainReady || '',
						effortRemainStarted: model.effortRemainStarted || '',
						percDurationDone: model.percDurationDone || '',
						percDurationReady: model.percDurationReady || '',
						percDurationStarted: model.percDurationStarted || '',
						teamTaskMatrix: model.teamTaskMatrix || [],
						moveEvent: model.moveEvent,
						moveBundleSteps: model.moveBundleSteps || []
					}
				}

				return null;
			})
			.catch((error: any) => error);
	}

	updateNews(news: any): Observable<NewsDetailModel> {
		news.mode = 'ajax';
		return this.http.post(`${this.APP_EVENT_UPDATE_NEWS}`, JSON.stringify(news))
			.map((response: any) => {
				return response || null;
			})
			.catch((error: any) => error);
	}

	saveNews(news: any): Observable<NewsDetailModel> {
		news.mode = 'ajax';
		return this.http.post(`${this.APP_EVENT_SAVE_NEWS}`, JSON.stringify(news))
			.map((response: any) => {
				return response || null;
			})
			.catch((error: any) => error);
	}

	deleteNews(news: any): Observable<NewsDetailModel> {
		news.mode = 'ajax';
		return this.http.get(`${this.APP_EVENT_DELETE_NEWS}/?id=${news.id}`)
			.map((response: any) => {
				return response || null;
			})
			.catch((error: any) => error);
	}

	getListBundles(eventId: number): Observable<any[]> {
		return this.http.get(`${this.APP_EVENT_LIST_BUNDLES}/${eventId}`)
			.map((response: any) => pathOr(null, ['data', 'list'], response))
			.catch((error: any) => error);
	}

	getEventStatusDetails(bundleId: number, eventId: number) {
		return this.http.get(`${this.APP_EVENT_STATUS_DETAILS}/${bundleId}?moveEventId=${eventId}`)
			.map((response: any) => pathOr(null, ['snapshot'], response))
			.catch((error: any) => error);
	}

	updateStatusDetails(payload: any): Observable<any> {
		return this.http.post(`${this.APP_EVENT_STATUS_UPDATE}`, JSON.stringify(payload))
			.catch((error: any) => {
				console.log('The error is');
				console.log(error);
				return error
			});
	}

	getBundleSteps(snapshot: any, moveBundleSteps: []): any {
		let headers = [];
		let percents = [];
		let categories = [];
		let colSize = 2;
		let showFrom = 0;
		let elementsToShow = 5;
		let steps = [];

		categories = [
			'',
			'Percents',
			'Tasks',
			'Planned Start',
			'Planned Completion',
			'Actual Start',
			'Actual Completion'
		];

		let headerRow = [];
		moveBundleSteps.forEach((moveBundle: any) => {
			headerRow.push({ id: moveBundle.id, text: moveBundle.label, classes: '' });
		});

		/*
		let headerRow = [
			{ text: 'Ready', classes: '' },
			{ text: 'BackupDone', classes: '' },
			{ text: 'Release', classes: '' },
			{ text: 'Staged', classes: '' },
			{ text: 'Powered On', classes: '' },
			{ text: 'Completed', classes: '' },
			{ text: 'Powered Down', classes: '' },
			{ text: 'Unracking', classes: '' },
			{ text: 'Reracking', classes: '' },
			{ text: 'Powered On', classes: '' },
			{ text: 'App Startup', classes: '' }
		];
		*/

		steps.push(headerRow);
		steps.push([
			{ text: 'p 1', classes: '' },
			{ text: 'p 2', classes: '' },
			{ text: 'p 3', classes: '' },
			{ text: 'p 4', classes: '' },
			{ text: 'p 5', classes: '' },
			{ text: 'p 6', classes: '' },
			{ text: 'p 7', classes: '' },
			{ text: 'p 8', classes: '' },
			{ text: 'p 9', classes: '' },
			{ text: 'p 10', classes: '' },
			{ text: 'p 11', classes: '' }
		]);

		steps.push([
			{ text: 't a', classes: '' },
			{ text: 't b', classes: '' },
			{ text: 't c', classes: '' },
			{ text: 't d', classes: '' },
			{ text: 't e', classes: '' },
			{ text: 't f', classes: '' },
			{ text: 't g', classes: '' },
			{ text: 't h', classes: '' },
			{ text: 't i', classes: '' },
			{ text: 't j', classes: '' },
			{ text: 't k', classes: '' }
		]);

		steps.push([
			{ text: 'ps a', classes: '' },
			{ text: 'ps b', classes: '' },
			{ text: 'ps c', classes: '' },
			{ text: 'ps d', classes: '' },
			{ text: 'ps e', classes: '' },
			{ text: 'ps f', classes: '' },
			{ text: 'ps g', classes: '' },
			{ text: 'ps h', classes: '' },
			{ text: 'ps i', classes: '' },
			{ text: 'ps j', classes: '' },
			{ text: 'ps k', classes: '' }
		]);

		steps.push([
			{ text: 'pc a', classes: '' },
			{ text: 'pc b', classes: '' },
			{ text: 'pc c', classes: '' },
			{ text: 'pc d', classes: '' },
			{ text: 'pc e', classes: '' },
			{ text: 'pc f', classes: '' },
			{ text: 'pc g', classes: '' },
			{ text: 'pc h', classes: '' },
			{ text: 'pc i', classes: '' },
			{ text: 'pc j', classes: '' },
			{ text: 'pc k', classes: '' }
		]);

		steps.push([
			{ text: 'as a', classes: '' },
			{ text: 'as b', classes: '' },
			{ text: 'as c', classes: '' },
			{ text: 'as d', classes: '' },
			{ text: 'as e', classes: '' },
			{ text: 'as f', classes: '' },
			{ text: 'as g', classes: '' },
			{ text: 'as h', classes: '' },
			{ text: 'as i', classes: '' },
			{ text: 'as j', classes: '' },
			{ text: 'as k', classes: '' }
		]);

		steps.push([
			{ text: 'ac a', classes: '' },
			{ text: 'ac b', classes: '' },
			{ text: 'ac c', classes: '' },
			{ text: 'ac d', classes: '' },
			{ text: 'ac e', classes: '' },
			{ text: 'ac f', classes: '' },
			{ text: 'ac g', classes: '' },
			{ text: 'ac h', classes: '' },
			{ text: 'ac i', classes: '' },
			{ text: 'ac j', classes: '' },
			{ text: 'ac k', classes: '' }
		]);

		return {
			categories,
			steps,
			columnsLength: headerRow.length
		};
	}
}
