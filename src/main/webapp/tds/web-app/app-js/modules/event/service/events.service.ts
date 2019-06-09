import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';

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
	private readonly baseURL = '/tdstm/ws';
	private readonly APP_EVENT_LISTS_URL = `${this.baseURL}/moveEvent/list`;
	private readonly APP_EVENT_NEWS = `${this.baseURL}/moveEventNews`;
	private readonly APP_EVENT_UPDATE_NEWS = `${this.baseURL}/../newsEditor/updateNews`;
	private readonly APP_EVENT_NEWS_DETAIL = `${this.baseURL}/../newsEditor/retrieveCommetOrNewsData`;

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

	/*
	http://localhost:8080/tdstm/newsEditor/updateNews
	&mode=ajax
	&displayOption=U

		id=2
		&message=Running+20+minutes+ahead+of+schedule
		&isArchived=1
		&resolution=.asd
	*/

	updateNews(news: any): Observable<NewsDetailModel> {
		news.mode = 'ajax';
		return this.http.post(`${this.APP_EVENT_UPDATE_NEWS}`, JSON.stringify(news))
			.map((response: any) => {
				console.log(response);
				return response || null;
			})
			.catch((error: any) => error);
	}

}
