// Angular
import { Component, OnInit, ViewChild } from '@angular/core';
import { Observable, forkJoin } from 'rxjs';
import { pathOr } from 'ramda';
import * as R from  'ramda';
// Store
import {Store} from '@ngxs/store';
// Action
import {SetEvent} from '../../action/event.actions';
// Services
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { UserContextService } from '../../../auth/service/user-context.service';
import { NotifierService } from '../../../../shared/services/notifier.service';
import { PreferenceService, PREFERENCES_LIST } from '../../../../shared/services/preference.service';
import { EventsService } from './../../service/events.service';
import { NewsModel, NewsDetailModel } from './../../model/news.model';
import { EventModel, EventPlanStatus } from './../../model/event.model';
// Components
import { NewsCreateEditComponent } from '../news-create-edit/news-create-edit.component';
import {PlanVersusStatusComponent} from '../plan-versus-status/plan-versus-status.component';
// Model
import { UserContextModel } from '../../../auth/model/user-context.model';

@Component({
	selector: 'event-dashboard',
	templateUrl: 'event-dashboard.component.html'
})

export class EventDashboardComponent implements OnInit {
	@ViewChild('planVersusStatus') public planVersusStatus: PlanVersusStatusComponent;
	public eventList: Array<EventModel> = [];
	public newsList: Array<NewsModel> = [];
	public selectedEvent = null;
	public selectedEventBundle = null;
	public includeUnpublished = true;
	public userTimeZone: string;
	public eventPlanStatus: EventPlanStatus = new EventPlanStatus();
	public eventDetails = null;
	public teamTaskMatrix = [];
	public taskCategories = null;
	public hasBundleSteps = false;

	constructor(
		private eventsService: EventsService,
		private preferenceService: PreferenceService,
		private dialogService: UIDialogService,
		private notifierService: NotifierService,
		private store: Store,
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
					this.onSelectedEvent(this.selectedEvent.id, this.selectedEvent.name);
				}
			});
	}

	/**
	 * Get the news corresponding to the event provided as argument
 	 * @param {number} id  Event id
	*/
	private getNewsFromEvent(id: number): void {
		this.eventsService.getNewsFromEvent(id)
			.subscribe((news: NewsModel[]) => this.newsList = news);
	}

	/**
	 * Whenever an event is selected call the endpoint to get the details to refresh the report
 	 * @param {number} id  Event id
	*/
	public onSelectedEvent(id: number, name: string): void {
		this.store.dispatch(new SetEvent({id: id, name: name}));
		this.getNewsFromEvent(id);

		this.eventsService.getTaskCategoriesStats(id, this.userTimeZone)
			.subscribe((data: any[]) => {
				console.log(data);
				this.taskCategories = data;
			});

		this.eventsService.getEventDetails(id, true)
			.subscribe((eventDetails: any) => {
				this.eventDetails = eventDetails;
				this.teamTaskMatrix = R.flatten(eventDetails && eventDetails.teamTaskMatrix || []);
				const bundles = pathOr([], ['moveEvent', 'moveBundles'], this.eventDetails);
				this.hasBundleSteps = false;
				if (bundles.length) {
					this.selectedEventBundle = bundles[0];
					this.eventPlanStatus = new EventPlanStatus();

					this.eventsService.getEventStatusDetails(this.selectedEventBundle.id, this.selectedEvent.id)
					.subscribe((statusDetails: any) => {
						this.hasBundleSteps = true;
						this.eventPlanStatus.dayTime = pathOr('', ['planSum', 'dayTime'], statusDetails);
						this.eventPlanStatus.dialIndicator = pathOr(0, ['planSum', 'dialInd'], statusDetails);
						this.eventPlanStatus.cssClass = pathOr('', ['planSum', 'confColor'], statusDetails);
						this.eventPlanStatus.description = pathOr('', ['planSum', 'eventDescription'], statusDetails);
						this.eventPlanStatus.eventTitle = pathOr('', ['planSum', 'eventString'], statusDetails);
						this.eventPlanStatus.status = pathOr('', ['planSum', 'eventRunbook'], statusDetails);
						this.eventPlanStatus.startDate = pathOr('', ['eventStartDate'], statusDetails);
					});
				} else {
					this.eventPlanStatus = new EventPlanStatus();
				}
			});
	}

	/**
	 * On click over a news title get the news details and with that show the create/edit news views
 	 * @param {any} news selected
	*/
	public onSelectedNews(selectedNews: any): void {
		const getNewsDetail = selectedNews.id ?
			this.eventsService.getNewsDetail(selectedNews.id, selectedNews.type) : Observable.of(new NewsDetailModel());
		getNewsDetail
			.subscribe((news: NewsDetailModel) => {
				if (news.commentObject && news.commentObject.moveEvent) {
					news.commentObject.moveEvent.id = this.selectedEvent.id;
				}
				news.commentType = selectedNews.type;
				if (news.commentObject.commentType) {
					news.commentObject.message = news.commentObject.comment;
				}
				this.openCreateEditNews(news)
			})
	}

	/**
	 * Passing and event id search for it in the event list, on not found it returns
	 * the first list element whenever the list has elements, otherwise it returns null
 	 * @param {number} id  Event id
	 * @returns {any} Event found otherwhise null
	*/
	private getDefaultEvent(id: string): any {
		if (id) {
			return this.eventList.find((event) => event.id.toString() === id) || null;
		} else if (this.eventList.length)  {
			return this.eventList[0];
		}

		return null;
	}

	/**
	 * Open the view to create/edit news
  	 * @param {NewsDetailModel} model  News info
	*/
	private openCreateEditNews(model: NewsDetailModel): void  {
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
	public onCreateNews(): void {
		const model = new NewsDetailModel();

		model.commentObject.moveEvent.id = this.selectedEvent.id;
		this.openCreateEditNews(model);
	}

	/**
	 * Call the endpoint to update the status value
  	 * @param {number} value Status value
	*/
	public onChangeStatus(value: number): void {
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
	 * On countdown timer timeout, call the on selected method to refresh the report
	*/
	public onTimeout(): void {
		this.onSelectedEvent(this.selectedEvent.id, this.selectedEvent.name);
	}
}
