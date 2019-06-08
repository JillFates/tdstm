// Angular
import {Component, OnInit, ViewChild} from '@angular/core';
import {Observable, forkJoin} from 'rxjs';
import {map} from 'rxjs/operators';

// Services
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UserContextService} from '../../../security/services/user-context.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {PreferenceService, PREFERENCES_LIST} from '../../../../shared/services/preference.service';
import {ActionType} from '../../../../shared/model/action-type.enum';
import {EventsService} from './../../service/events.service';
import {NewsModel, NewsDetailModel} from './../../model/news.model';
import {EventModel} from './../../model/event.model';
// Components
import {NewsCreateEditComponent} from '../news-create-edit/news-create-edit.component';
// Model

import {COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
import {DIALOG_SIZE} from '../../../../shared/model/constants';
import {GridComponent} from '@progress/kendo-angular-grid';
import {UserContextModel} from '../../../security/model/user-context.model';
import {ContextMenuComponent} from '@progress/kendo-angular-menu';
import any from 'ramda/es/any';

@Component({
	selector: 'event-dashboard',
	templateUrl: 'event-dashboard.component.html'
})

export class EventDashboardComponent implements OnInit {
	public eventList: Array<EventModel> = [];
	public newsList: Array<NewsModel> = [];
	public selectedEvent = null;
	public includeUnpublished = true;
	public userTimeZone: string;

	constructor(
		private eventsService: EventsService,
		private preferenceService: PreferenceService,
		private dialogService: UIDialogService,
		private notifierService: NotifierService,
		private userContextService: UserContextService) {
	}

	ngOnInit() {
		this.populateDate();
	}

	private populateDate(): void {
		const services = [
			this.eventsService.getEvents(),
			this.preferenceService.getPreference(PREFERENCES_LIST.MOVE_EVENT)
		];

		forkJoin(services)
			.subscribe((results: any[]) => {
				const [eventList, preference] = results;
				this.eventList = eventList;
				this.selectedEvent = this.getDefaultEvent(preference && preference[PREFERENCES_LIST.MOVE_EVENT] || '')
				if (this.selectedEvent) {
					this.eventsService.getNewsFromEvent(this.selectedEvent.id)
						.subscribe((news: NewsModel[]) => this.newsList = news);
				}
			});
	}

	onSelectedEvent(id: number): void {
		console.log(id);
	}

	onSelectedNews(id: number): void {
		this.eventsService.getNewsDetail(id)
			.subscribe((news: NewsDetailModel) => {
				this.openCreateEdiceNews(news)
			})
		console.log(id);
	}

	getDefaultEvent(defaultEventId: string): any {
		if (defaultEventId) {
			return this.eventList.find((event) => event.id.toString() === defaultEventId) || null;
		}
		return null;
	}

	openCreateEdiceNews(model: NewsDetailModel) {
		this.dialogService.open(NewsCreateEditComponent, [
			{provide: NewsDetailModel, useValue: model},
		]).then(result => {
			console.log('this.reloadData()');
		}, error => {
			console.log(error);
		});
	}

	getEmptyNews(): NewsModel {
		return {
			type: 'N',
			text: '',
			state: 'L'
		} ;
	}
}
