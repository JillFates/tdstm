import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {PREFERENCES_LIST, PreferenceService} from '../../../shared/services/preference.service';
import {NewsModel} from '../../event/model/news.model';
import {EventNewsModel} from '../model/event-news.model';

/**
 * @name EventsService
 */
@Injectable()
export class EventNewsService {
	// private instance variable to hold base url
	private readonly baseURL = '/tdstm';
	private readonly APP_EVENT_NEWS = `${this.baseURL}/newsEditor/getEventNewsList`;
	private readonly categories = [
		'Step',
		'',
		'Tasks',
		'Planned Start',
		'Planned Completion',
		'Actual Start',
		'Actual Completion'
	];

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient, private preferenceService: PreferenceService) {
		this.preferenceService.getPreference(PREFERENCES_LIST.CURR_TZ).subscribe();
	}

	/**
	 * Get the news associated to the event
	 * @param {number} eventId
	 * @param bundleId
	 * @returns {Observable<NewsModel[]>}
	 */
	getNewsFromEvent(eventId: number, bundleId = null, viewFilter = null): Observable<EventNewsModel[]> {
		let url = `${this.APP_EVENT_NEWS}?moveEvent=${eventId}`;
		url += (viewFilter && viewFilter !== '0: undefined')
			? `&viewFilter=${viewFilter}`
			: `&viewFilter=`;
		url += (bundleId && bundleId !== '0: undefined')
			? `&moveBundle=${bundleId}`
			: `&moveBundle=`;

		return this.http.get(url)
			.map((response: any) => {
				const eventNewsModels = response || [];
				eventNewsModels.forEach(item => {
					item.createdAt = ((item.createdAt) ? new Date(item.createdAt) : '');
					item.resolvedAt = ((item.resolvedAt) ? new Date(item.resolvedAt) : '');
				});

				return eventNewsModels;
			})
			.catch((error: any) => error);
	}
}
