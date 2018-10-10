/**
 * Created by Jorge Morayta on 3/15/2017.
 */
import {Component, OnInit, Input} from '@angular/core';

export interface TaskActionsOptions {
	showDone: boolean;
	showStart: boolean;
	showAssignToMe: boolean;
	showNeighborhood: boolean;
}

@Component({
	selector: 'tds-task-actions',
	templateUrl: '../tds/web-app/app-js/modules/taskManager/components/task-actions/task-actions.component.html'
})

export class TaskActionsComponent implements OnInit {
	@Input() options: TaskActionsOptions;
	/**
	 * Initiates the Notice Module
	 */
	ngOnInit(): void {
		console.log('Init');
	}
}