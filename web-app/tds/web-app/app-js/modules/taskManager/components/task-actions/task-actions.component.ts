/**
 * Created by Jorge Morayta on 3/15/2017.
 */
import {Component, OnInit, Input, Output, EventEmitter} from '@angular/core';
import {PermissionService} from '../../../../shared/services/permission.service';
import {Permission} from '../../../../shared/model/permission.model';

export interface TaskActionsOptions {
	showDone: boolean;
	showStart: boolean;
	showAssignToMe: boolean;
	showNeighborhood: boolean;
	invoke: boolean;
}

@Component({
	selector: 'tds-task-actions',
	templateUrl: '../tds/web-app/app-js/modules/taskManager/components/task-actions/task-actions.component.html'
})
export class TaskActionsComponent implements OnInit {
	@Input() options: TaskActionsOptions;
	@Output() start: EventEmitter<void> = new EventEmitter<void>();
	@Output() done: EventEmitter<void> = new EventEmitter<void>();
	@Output() invoke: EventEmitter<void> = new EventEmitter<void>();
	@Output() assignToMe: EventEmitter<void> = new EventEmitter<void>();
	@Output() neighborhood: EventEmitter<void>  = new EventEmitter<void>();

	hasInvokePermission = false;
	constructor(private permissionService: PermissionService) {
		this.hasInvokePermission = this.permissionService.hasPermission(Permission.ActionInvoke)
	}

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

	/**
	 * Emit the Neighborhood event
	 */
	onNeighborhood(): void {
		this.neighborhood.emit();
	}

	/**
	 * Emit the assign to me event
	 */
	onAssignToMe(): void {
		this.assignToMe.emit();
	}

	/**
	 * Emit the invoke event
	 */
	onInvoke(): void {
		this.invoke.emit();
	}

}