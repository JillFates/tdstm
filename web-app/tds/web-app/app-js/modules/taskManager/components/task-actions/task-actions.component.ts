/**
 * Created by Jorge Morayta on 3/15/2017.
 */
import {Component, OnInit} from '@angular/core';

@Component({
	selector: 'tds-task-actions',
	templateUrl: '../tds/web-app/app-js/modules/taskManager/components/task-actions/task-actions.component.html'
})

export class TaskActionsComponent implements OnInit {
	constructor() {}

	/**
	 * Initiates the Notice Module
	 */
	ngOnInit(): void {
		console.log('Init');
	}
}