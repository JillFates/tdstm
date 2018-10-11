/**
 * Created by Jorge Morayta on 3/15/2017.
 */
import {Component, OnInit, Input, Output, EventEmitter} from '@angular/core';

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
	@Output() start: EventEmitter<void> = new EventEmitter<void>();
	@Output() done: EventEmitter<void> = new EventEmitter<void>();
	ngOnInit(): void {
		console.log('Init');
	}

	/**
	 * Emit the start status change event
	 */
	onStart(): void {
		this.start.emit();
	}

	/**
	 * Emit the done status change event
	 */
	onDone(): void {
		this.done.emit();
	}
}