// Angular
import {Component, OnInit, ViewChild} from '@angular/core';
// Services
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UserContextService} from '../../../security/services/user-context.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
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
	constructor(
		private dialogService: UIDialogService,
		private notifierService: NotifierService,
		private userContextService: UserContextService) {
	}

	ngOnInit() {
		console.log('Init Event dashboard');
	}

}
