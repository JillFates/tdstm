import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {pathOr} from 'ramda'

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {catchError, map} from 'rxjs/operators';

import {EventModel, EventRowType} from '../model/event.model';
import {NewsModel, NewsDetailModel} from '../model/news.model';
import {DateUtils} from '../../../shared/utils/date.utils';
import move from 'ramda/es/move';

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
						moveBundleSteps: model.moveBundleSteps || [],
						moveBundleList: model.moveBundleList || []
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

	private getInitialValues(lenght: number, classes: string): any {
		const items = [];
		for (let i = 0; i < lenght; i++) {
			items.push({text: '', classes})
		}
		return items;
	}

	getBundleSteps(snapshot: any, moveBundleSteps: [], userTimeZone: string, moveBundleList: any, selectedBundleId: number): any {
		let headers = [];
		let percents = [];
		let categories = [];
		let colSize = 2;
		let showFrom = 0;
		let elementsToShow = 5;
		let steps = [];

		categories = [
			'Step',
			'',
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

		steps.push(headerRow);
		steps.push(this.getInitialValues(headerRow.length, 'empty-column'));
		steps.push(this.getInitialValues(headerRow.length, 'primary'));
		steps.push(this.getInitialValues(headerRow.length, 'secondary'));
		steps.push(this.getInitialValues(headerRow.length, 'secondary'));
		steps.push(this.getInitialValues(headerRow.length, 'primary'));
		steps.push(this.getInitialValues(headerRow.length, 'primary'));

		snapshot.steps.forEach((step: any) => {
			const bundle: any = moveBundleSteps
				.find((currentBundle: any) => {
					return currentBundle.moveBundle.id === parseInt(snapshot.moveBundleId, 10) && step.tid === currentBundle.transitionId;
				});

			if (bundle) {
				console.log(bundle);
			}
			let colIndex = headerRow.findIndex((item: any) => item.id === bundle.id);
			console.log(colIndex);
			console.log('----------');

			const percent = isNaN(step.tskComp / step.tskTot) ? 0 + '%' : parseInt(((step.tskComp / step.tskTot) * 100).toString(), 10) + '%';
			steps[EventRowType.Percents][colIndex].text = percent;
			steps[EventRowType.Percents][colIndex].classes = step.percentageStyle;
			steps[EventRowType.PlannedStart][colIndex].text = DateUtils.formatUserDateTime(userTimeZone, step.planStart);
			steps[EventRowType.PlannedCompletion][colIndex].text = DateUtils.formatUserDateTime(userTimeZone, step.planComp);
			steps[EventRowType.ActualStart][colIndex].text = DateUtils.formatUserDateTime(userTimeZone, step.actStart);
			steps[EventRowType.ActualCompletion][colIndex].text = DateUtils.formatUserDateTime(userTimeZone, step.actComp);

			let remainingTasksNumber = 0
			let totalTasksNumber = 0
			if (!isNaN(step.tskComp / step.tskTot)) {
				remainingTasksNumber = step.tskComp
				totalTasksNumber = step.tskTot
			}

			let taskManagerUrl =  '/assetEntity/listTasks?bundle=' + 10 + '&justRemaining=';
			let firstUrl = taskManagerUrl + '1&step=' + step.wfTranId
			let secondUrl = taskManagerUrl + '0&step=' + step.wfTranId
			let linksHtml = '<a href=\'' + firstUrl + '\'>' + remainingTasksNumber + '</a> (of <a href=\'' + secondUrl + '\'>' + totalTasksNumber + '</a>)';
			// set the task value
			steps[EventRowType.Tasks][colIndex].text = linksHtml;

			if (snapshot.runbookOn === 1) {
				console.log('print');
			}

		});

		return {
			categories,
			steps,
			columnsLength: headerRow.length,
			moveBundleList: moveBundleList,
			selectedBundleId: selectedBundleId
		};
	}
}
