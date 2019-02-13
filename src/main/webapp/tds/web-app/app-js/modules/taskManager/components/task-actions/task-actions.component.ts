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
	template: `
        <!-- Main content -->
        <div class="task-actions-component">
            <button *ngIf="options && options.showStart" (click)="onStart()" class="btn btn-default" type="button" title="Start">
                <i class="fa fa-med fa-play-circle"></i> <span>Start</span>
            </button>
            <button  *ngIf="options && options.showDone" (click)="onDone()" class="btn btn-default" type="button" title="Done">
                <i class="fa fa-med fa-check"></i> <span>Done</span>
            </button>
            <button  *ngIf="options && options.showAssignToMe" (click)="onAssignToMe()" class="btn btn-default" type="button" title="Assign To Me">
                <i class="fa fa-med fa-user"></i> <span>Assign To Me</span>
            </button>
            <button  *ngIf="options && options.showNeighborhood" (click)="onNeighborhood()" class="btn btn-default" type="button" title="Neighborhood">
                <i class="fa fa-med fa-align-left"></i> <span>Neighborhood</span>
            </button>
            <button  *ngIf="options && options.invoke" [disabled]="!hasInvokePermission"
                     (click)="onInvoke()" class="btn btn-default" type="button" title="Invoke">
                <i class="fa fa-med fa-cogs"></i> <span>Invoke</span>
            </button>
        </div>
     `,
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