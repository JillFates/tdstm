// Angular
import {Component, OnInit, ViewChild} from '@angular/core';
import {forkJoin} from 'rxjs';
// Services
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UserContextService} from '../../../security/services/user-context.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {EventsService} from './../../service/events.service';
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
	public includeUnpublished = true;
	public refreshEverySeconds = 0;

	constructor(
		private eventsService: EventsService,
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
		];
		forkJoin(services)
			.subscribe((results: any[]) => {
				const [eventList] = results;
				this.eventList = eventList;
			});
	}

	onChangeEvent(event): void {
		console.log(event);
	}

}
