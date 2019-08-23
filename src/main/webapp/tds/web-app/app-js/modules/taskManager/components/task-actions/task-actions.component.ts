/**
 * Created by Jorge Morayta on 3/15/2017.
 */
import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges, OnChanges } from '@angular/core';
import {PermissionService} from '../../../../shared/services/permission.service';
import {Permission} from '../../../../shared/model/permission.model';
import { TaskActionInfoModel } from '../../model/task-action-info.model';
import { TaskStatus } from '../../model/task-edit-create.model';
import { UserContextModel } from '../../../auth/model/user-context.model';
import { UserContextService } from '../../../auth/service/user-context.service';
import { TaskService } from '../../service/task.service';

export interface TaskActionsOptions {
	showDone: boolean;
	showStart: boolean;
	showAssignToMe: boolean;
	showNeighborhood: boolean;
	invokeButton: any;
	showReset: boolean;
}

@Component({
	selector: 'tds-task-actions',
	template: `
        <!-- Main content -->
        <div class="task-actions-component">
            <button *ngIf="showStart()" (click)="onStart()" class="btn btn-default" type="button" title="Start">
                <i class="fa fa-med fa-play-circle"></i> <span>Start</span>
            </button>
            <button  *ngIf="showDone()" (click)="onDone()" class="btn btn-default" type="button" title="Done">
                <i class="fa fa-med fa-check"></i> <span>Done</span>
            </button>
						<button  *ngIf="showReset()" (click)="onReset()" class="btn btn-default" type="button" title="Reset">
							<i class="fa fa-med fa-power-off"></i> <span>Reset Action</span>
						</button>
            <button  *ngIf="showAssignToMe()" (click)="onAssignToMe()" class="btn btn-default" type="button" title="Assign To Me">
                <i class="fa fa-med fa-user"></i> <span>Assign To Me</span>
            </button>
            <button  *ngIf="showNeighborhood()" (click)="onNeighborhood()" class="btn btn-default" type="button" title="Neighborhood">
                <i class="fa fa-med fa-align-left"></i> <span>Neighborhood</span>
            </button>
            <button
							class="btn btn-default"
							*ngIf="showInvoke()"
							[disabled]="!hasInvokePermission || invokeButton.disabled"
							[title]="invokeButton.tooltipText || ''"
							(click)="onInvoke()">
                <i class="fa fa-med fa-cog"></i> <span>{{invokeButton.label || 'Invoke'}}</span>
            </button>
        </div>
     `,
})
export class TaskActionsComponent implements OnInit, OnChanges {
	@Input() task: any;
	@Output() start: EventEmitter<void> = new EventEmitter<void>();
	@Output() done: EventEmitter<void> = new EventEmitter<void>();
	@Output() invoke: EventEmitter<void> = new EventEmitter<void>();
	@Output() assignToMe: EventEmitter<void> = new EventEmitter<void>();
	@Output() neighborhood: EventEmitter<void>  = new EventEmitter<void>();
	@Output() reset: EventEmitter<void> = new EventEmitter<void>();
	invokeButton: any;
	hasInvokePermission: boolean;
	private userContext: UserContextModel;
	private taskActionInfoModel: TaskActionInfoModel;

	constructor(
			private permissionService: PermissionService,
			private userContextService: UserContextService,
			private taskService: TaskService) {
		this.userContext = null;
		this.taskActionInfoModel = null;
		this.hasInvokePermission = this.permissionService.hasPermission(Permission.ActionInvoke);
		this.invokeButton = null;
		this.userContextService.getUserContext().subscribe((userContext: UserContextModel) => {
			this.userContext = userContext;
		});
	}

	ngOnInit(): void {
		this.loadTaskInfoModel();
	}

	/**
	 * Detect any change on the task status or assignedTo
	 * @param changes
	 */
	ngOnChanges(changes: SimpleChanges) {
		if (changes.task && !changes.task.firstChange && (
			changes.task.currentValue['status'] !== changes.task.previousValue['status'] ||
			changes.task.currentValue['assignedTo'] !== changes.task.previousValue['assignedTo']
			)) {
			this.loadTaskInfoModel();
		}
	}

	loadTaskInfoModel(): void {
		this.taskService.getTaskActionInfo(parseInt(this.task.id, 0))
			.subscribe((result: TaskActionInfoModel) => {
				this.taskActionInfoModel = result;
				this.invokeButton = this.taskActionInfoModel.invokeButton;
			})
	}

	showDone(): boolean {
		return [TaskStatus.READY, TaskStatus.STARTED].indexOf(this.task.status) >= 0;
	}

	showStart(): boolean {
		return [TaskStatus.READY].indexOf(this.task.status) >= 0;
	}

	showAssignToMe(): boolean {
		return ( this.taskActionInfoModel
			&&
			(!this.taskActionInfoModel.assignedTo || this.userContext.person.id !== this.taskActionInfoModel.assignedTo)
			&& ([TaskStatus.READY, TaskStatus.PENDING, TaskStatus.STARTED].indexOf(this.task.status) >= 0));
	}

	showNeighborhood(): boolean {
		return this.taskActionInfoModel
			&& (this.taskActionInfoModel.predecessors + this.taskActionInfoModel.successors > 0);
	}

	showReset(): boolean {
		return this.taskActionInfoModel
			&& this.taskActionInfoModel.apiActionId && this.task.status === TaskStatus.HOLD;
	}

	showInvoke(): boolean {
		return this.taskActionInfoModel
			&& this.taskActionInfoModel.invokeButton
			&& this.taskActionInfoModel.invokeButton !== null;
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

	/**
	 * Emit the reset event
	 */
	onReset(): void {
		this.reset.emit();
	}

}
