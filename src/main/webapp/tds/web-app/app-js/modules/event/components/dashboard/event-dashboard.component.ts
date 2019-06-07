// Angular
import {Component, OnInit, ViewChild} from '@angular/core';
import {forkJoin} from 'rxjs';
// Services
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UserContextService} from '../../../security/services/user-context.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {PreferenceService, PREFERENCES_LIST} from '../../../../shared/services/preference.service';
import {EventsService} from './../../service/events.service';
import {News} from '../news/model/news.model';
// Components
// Model

import {COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
import {DIALOG_SIZE} from '../../../../shared/model/constants';
import {GridComponent} from '@progress/kendo-angular-grid';
import {UserContextModel} from '../../../security/model/user-context.model';
import {ContextMenuComponent} from '@progress/kendo-angular-menu';

@Component({
	selector: 'event-dashboard',
	templateUrl: 'event-dashboard.component.html'
})

export class EventDashboardComponent implements OnInit {
	public eventList = [];
	public newsList = [];
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
						.subscribe((news: News[]) => this.newsList = news);
				}
			});
	}

	onChangeEvent(event): void {
		console.log(event);
	}

	getDefaultEvent(defaultEventId: string): any {
		if (defaultEventId) {
			return this.eventList.find((event) => event.id.toString() === defaultEventId) || null;
		}
		return null;
	}
}
