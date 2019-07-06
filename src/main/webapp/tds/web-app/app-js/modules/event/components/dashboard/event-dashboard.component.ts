// Angular
import { Component, OnInit, ViewChild } from '@angular/core';
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';
import { pathOr } from 'ramda';

import * as R from  'ramda';

// Services
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { UserContextService } from '../../../security/services/user-context.service';
import { NotifierService } from '../../../../shared/services/notifier.service';
import { PreferenceService, PREFERENCES_LIST } from '../../../../shared/services/preference.service';
import { ActionType } from '../../../../shared/model/action-type.enum';
import { EventsService } from './../../service/events.service';
import { NewsModel, NewsDetailModel } from './../../model/news.model';
import { EventModel, EventPlanStatus } from './../../model/event.model';
// Components
import { NewsCreateEditComponent } from '../news-create-edit/news-create-edit.component';
// Model

import { COLUMN_MIN_WIDTH } from '../../../dataScript/model/data-script.model';
import { DIALOG_SIZE } from '../../../../shared/model/constants';
import { GridComponent } from '@progress/kendo-angular-grid';
import { UserContextModel } from '../../../security/model/user-context.model';
import { ContextMenuComponent } from '@progress/kendo-angular-menu';
// import any from 'ramda/es/any';

@Component({
	selector: 'event-dashboard',
	templateUrl: 'event-dashboard.component.html'
})

export class EventDashboardComponent implements OnInit {
	public eventList: Array<EventModel> = [];
	public newsList: Array<NewsModel> = [];
	public selectedEvent = null;
	public selectedEventBundle = null;
	public includeUnpublished = true;
	public userTimeZone: string;
	public eventPlanStatus: EventPlanStatus = new EventPlanStatus();
	public eventDetails = null;
	public teamTaskMatrix = [];
	public bundleSteps = null;

	constructor(
		private eventsService: EventsService,
		private preferenceService: PreferenceService,
		private dialogService: UIDialogService,
		private notifierService: NotifierService,
		private userContextService: UserContextService) {
	}

	ngOnInit() {
		this.populateData();
	}

	private populateData(): void {
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
					this.onSelectedEvent(this.selectedEvent.id);

				}
			});
	}

	onSelectedEvent(id: number): void {
		this.eventsService.getNewsFromEvent(id)
			.subscribe((news: NewsModel[]) => this.newsList = news);

		this.eventsService.getEventDetails(id, true)
			.subscribe((eventDetails: any) => {
				this.eventDetails = eventDetails;
				this.teamTaskMatrix = R.flatten(eventDetails && eventDetails.teamTaskMatrix || []);
				const bundles = pathOr([], ['moveEvent', 'moveBundles'], this.eventDetails);
				if (bundles.length) {
					this.selectedEventBundle =  {id: 3239}; // bundles[0];
					this.eventPlanStatus = new EventPlanStatus();
					this.eventsService.getEventStatusDetails(this.selectedEventBundle.id, this.selectedEvent.id)
					.subscribe((statusDetails: any) => {
						console.log('The event status details are');
						this.bundleSteps = this.eventsService.getBundleSteps(statusDetails, this.eventDetails.moveBundleSteps)
						this.eventPlanStatus.dayTime = pathOr('', ['planSum', 'dayTime'], statusDetails);
						this.eventPlanStatus.dialIndicator = pathOr(0, ['planSum', 'dialInd'], statusDetails);
						this.eventPlanStatus.cssClass = pathOr('', ['planSum', 'confColor'], statusDetails);
						this.eventPlanStatus.description = pathOr('', ['planSum', 'eventDescription'], statusDetails);
						this.eventPlanStatus.eventTitle = pathOr('', ['planSum', 'eventString'], statusDetails);
						this.eventPlanStatus.status = pathOr('', ['planSum', 'eventRunbook'], statusDetails);
					});
				}

			});

		/*
		this.eventsService.getListBundles(id)
			.subscribe((results: any[]) => {
				this.selectedEventBundle = results.length > 0 ? results.shift() : null;
				this.eventPlanStatus = new EventPlanStatus();
				if (this.selectedEventBundle) {
					this.eventsService.getEventStatusDetails(this.selectedEventBundle.id, this.selectedEvent.id)
						.subscribe((statusDetails: any) => {
							console.log('The event status details are');
							this.eventPlanStatus.dayTime = pathOr('', ['planSum', 'dayTime'], statusDetails);
							this.eventPlanStatus.dialIndicator = pathOr(0, ['planSum', 'dialInd'], statusDetails);
							this.eventPlanStatus.cssClass = pathOr('', ['planSum', 'confColor'], statusDetails);
							this.eventPlanStatus.description = pathOr('', ['planSum', 'eventDescription'], statusDetails);
							this.eventPlanStatus.eventTitle = pathOr('', ['planSum', 'eventString'], statusDetails);
							this.eventPlanStatus.status = pathOr('', ['planSum', 'eventRunbook'], statusDetails);
						});
				}
			});
			*/
	}

	onSelectedNews(id: number): void {
		const getNewsDetail = id ? this.eventsService.getNewsDetail(id) : Observable.of(new NewsDetailModel());

		getNewsDetail
			.subscribe((news: NewsDetailModel) => {
				news.commentObject.moveEvent.id = this.selectedEvent.id;
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
			{ provide: NewsDetailModel, useValue: model },
		]).then(result => {
			console.log('reloading data');
		}, error => {
			console.log(error);
		});
	}

	getEmptyNews(): NewsModel {
		return {
			type: 'N',
			text: '',
			state: 'L'
		};
	}

	onCreate(): void {
		const model = new NewsDetailModel();

		model.commentObject.moveEvent.id = this.selectedEvent.id;
		this.openCreateEdiceNews(model);
	}

	onChangeStatus(value: number) {
		this.eventsService.updateStatusDetails({
			moveEventId: this.selectedEvent.id,
			value: value,
			checkbox: true
		})
			.subscribe((result) => {
				console.log('The resulting of update is:');
				console.log(result);
			}, error => {
				console.log('here error is');
				console.log(error);
			});
	}
}
