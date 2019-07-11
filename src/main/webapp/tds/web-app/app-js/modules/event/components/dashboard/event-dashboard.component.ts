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

	/**
	 * Call the endpoints required to populate the initial data
	*/
	private populateData(): void {
		this.userContextService.getUserContext()
		.subscribe((userContext: UserContextModel) => {
			this.userTimeZone = userContext.timezone;
		})

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

	/**
	 * Get the news corresponding to the event provided as argument
 	 * @param {number} id  Event id
	*/
	getNewsFromEvent(id: number): void {
		this.eventsService.getNewsFromEvent(id)
			.subscribe((news: NewsModel[]) => this.newsList = news);
	}

	/**
	 * Whenever an event is selected call the endpoint to get the details to refresh the report
 	 * @param {number} id  Event id
	*/
	onSelectedEvent(id: number): void {
		this.bundleSteps = this.eventsService.getEmptyBundleSteps();
		this.getNewsFromEvent(id);

		this.eventsService.getEventDetails(id, true)
			.subscribe((eventDetails: any) => {
				this.eventDetails = eventDetails;
				this.teamTaskMatrix = R.flatten(eventDetails && eventDetails.teamTaskMatrix || []);
				const bundles = pathOr([], ['moveEvent', 'moveBundles'], this.eventDetails);
				if (bundles.length) {
					this.selectedEventBundle = bundles[0];
					this.eventPlanStatus = new EventPlanStatus();
					this.eventsService.getEventStatusDetails(this.selectedEventBundle.id, this.selectedEvent.id)
					.subscribe((statusDetails: any) => {
						this.bundleSteps = this.eventsService.getBundleSteps(
							statusDetails,
							this.eventDetails.moveBundleSteps,
							this.userTimeZone,
							this.eventDetails.moveBundleList,
							this.selectedEventBundle && this.selectedEventBundle.id
						);

						this.eventPlanStatus.dayTime = pathOr('', ['planSum', 'dayTime'], statusDetails);
						this.eventPlanStatus.dialIndicator = pathOr(0, ['planSum', 'dialInd'], statusDetails);
						this.eventPlanStatus.cssClass = pathOr('', ['planSum', 'confColor'], statusDetails);
						this.eventPlanStatus.description = pathOr('', ['planSum', 'eventDescription'], statusDetails);
						this.eventPlanStatus.eventTitle = pathOr('', ['planSum', 'eventString'], statusDetails);
						this.eventPlanStatus.status = pathOr('', ['planSum', 'eventRunbook'], statusDetails);
						this.eventPlanStatus.startDate = pathOr('', ['eventStartDate'], statusDetails);
					});
				}
			});
	}

	/**
	 * On click over a news title get the news details and with that show the create/edit news views
 	 * @param {number} id  News id
	*/
	onSelectedNews(id: number): void {
		const getNewsDetail = id ? this.eventsService.getNewsDetail(id) : Observable.of(new NewsDetailModel());
		getNewsDetail
			.subscribe((news: NewsDetailModel) => {
				if (news.commentObject) {
					news.commentObject.moveEvent.id = this.selectedEvent.id;
				}
				this.openCreateEdiceNews(news)
			})
	}

	/**
	 * Passing and event id search for it in the event list
 	 * @param {number} id  Event id
	 * @returns {any} Event found otherwhise null
	*/
	getDefaultEvent(id: string): any {
		if (id) {
			return this.eventList.find((event) => event.id.toString() === id) || null;
		}
		return null;
	}

	/**
	 * Open the view to create/edit news
  	 * @param {NewsDetailModel} model  News info
	*/
	openCreateEdiceNews(model: NewsDetailModel): void  {
		this.dialogService.open(NewsCreateEditComponent, [
			{ provide: NewsDetailModel, useValue: model },
		]).then(result => {
			this.getNewsFromEvent(this.selectedEvent.id);
		}, error => {
			console.log(error);
		});
	}

	/**
	 * Create an empty news model and call the component to show the edit/create news view
	*/
	onCreateNews(): void {
		const model = new NewsDetailModel();

		model.commentObject.moveEvent.id = this.selectedEvent.id;
		this.openCreateEdiceNews(model);
	}

	/**
	 * Call the endpoint to update the status value
  	 * @param {number} value Status value
	*/
	onChangeStatus(value: number): void {
		this.eventsService.updateStatusDetails({
			moveEventId: this.selectedEvent.id,
			value: value,
			checkbox: true
		})
			.subscribe((result) => {
				console.log(result);
			}, error => {
				console.log(error);
			});
	}

	/**
	 * On changing the bundle steps tab, call the endpoint to refresh the status details
  	 * @param {number} selectedBundleId Current step bundle tab selected
	*/
	onChangeStepsTab(selectedBundleId: number): void {
		this.eventsService.getEventStatusDetails(selectedBundleId, this.selectedEvent.id)
		.subscribe((statusDetails: any) => {
			this.bundleSteps = this.eventsService.getBundleSteps(
				statusDetails,
				this.eventDetails.moveBundleSteps,
				this.userTimeZone,
				this.eventDetails.moveBundleList,
				selectedBundleId
			);
		});
	}

	/**
	 * On countdown timer timeout, call the on selected method to refresh the report
	*/
	onTimeout(): void {
		this.onSelectedEvent(this.selectedEvent.id);
	}
}
