/**
 * Created by Jorge Morayta on 3/15/2017.
 */
import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges, OnChanges } from '@angular/core';
import { PermissionService } from '../../../../shared/services/permission.service';
import { Permission } from '../../../../shared/model/permission.model';
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
			<button *ngIf="showStart()" (click)="onStart()"
							class="btn" [ngClass]="buttonClass" type="button" title="Start">
				<i class="fa fa-med fa-play-circle"></i> <span>Start</span>
			</button>
			<button *ngIf="showDone()" (click)="onDone()"
							class="btn" [ngClass]="buttonClass" type="button" title="Done">
				<i class="fa fa-med fa-check"></i> <span>Done</span>
			</button>
			<button
				*ngIf="showInvoke()"
				class="btn" [ngClass]="buttonClass"
				[disabled]="!hasInvokePermission || invokeButton.disabled"
				[title]="invokeButton.tooltipText || ''"
				(click)="onInvoke()">
				<i class="fa fa-med fa-cog"></i> <span>{{invokeButton.label || 'Invoke'}}</span>
			</button>
			<button *ngIf="showReset()" (click)="onReset()"
							class="btn" [ngClass]="buttonClass" type="button" title="Reset">
				<i class="fa fa-med fa-power-off"></i> <span>Reset Action</span>
			</button>
			<button
				*ngIf="showDetails" (click)="onShowDetails()"
				class="btn" [ngClass]="buttonClass" type="button" title="Open Task Details">
				<i class="glyphicon glyphicon-zoom-in"></i>Details
			</button>
			<button *ngIf="showAssignToMe()" (click)="onAssignToMe()"
							class="btn" [ngClass]="buttonClass" type="button" title="Assign To Me">
				<i class="fa fa-med fa-user"></i> <span>Assign To Me</span>
			</button>
			<button *ngIf="showNeighborhood()" (click)="onNeighborhood()"
							class="btn" [ngClass]="buttonClass" type="button" title="Neighborhood">
				<i class="fa fa-med fa-align-left"></i> <span>Neighborhood</span>
			</button>
			<div *ngIf="showDelayActions && showDelay()"
					 class="task-action-buttons">
				<span style="margin-right:16px">Delay for:</span>
				<button
					class="btn" [ngClass]="buttonClass"
					(click)="onDelay(1)">
					<i class="fa fa-forward"></i>1 day
				</button>
				<button
					class="btn" [ngClass]="buttonClass"
					(click)="onDelay(2)">
					<i class="fa fa-forward"></i>2 day
				</button>
				<button
					class="btn" [ngClass]="buttonClass"
					(click)="onDelay(7)">
					<i class="fa fa-forward"></i>7 day
				</button>
			</div>
		</div>
	`,
})
export class TaskActionsComponent implements OnInit, OnChanges {
	@Input() taskStatus: string;
	@Input() showDelayActions: boolean;
	@Input() showDetails: boolean;
	@Input() buttonClass: string;
	@Input() taskActionInfoModel: TaskActionInfoModel;
	@Output() start: EventEmitter<void> = new EventEmitter<void>();
	@Output() done: EventEmitter<void> = new EventEmitter<void>();
	@Output() invoke: EventEmitter<void> = new EventEmitter<void>();
	@Output() assignToMe: EventEmitter<void> = new EventEmitter<void>();
	@Output() neighborhood: EventEmitter<void> = new EventEmitter<void>();
	@Output() reset: EventEmitter<void> = new EventEmitter<void>();
	@Output() delay: EventEmitter<any> = new EventEmitter<any>();
	@Output() details: EventEmitter<any> = new EventEmitter<any>();
	invokeButton: any;
	hasInvokePermission: boolean;
	private userContext: UserContextModel;

	constructor(
		private permissionService: PermissionService,
		private userContextService: UserContextService,
		private taskService: TaskService) {
		this.userContext = null;
		this.hasInvokePermission = this.permissionService.hasPermission(Permission.ActionInvoke);
		this.invokeButton = null;
		this.userContextService.getUserContext().subscribe((userContext: UserContextModel) => {
			this.userContext = userContext;
		});
	}

	ngOnInit(): void {
		if (!this.buttonClass) {
			this.buttonClass = 'btn-default';
		}
	}

	/**
	 * Detect any change on the task status or assignedTo
	 * @param changes
	 */
	ngOnChanges(changes: SimpleChanges): void {
		if (changes.task && !changes.task.firstChange && (
			changes.task.currentValue['status'] !== changes.task.previousValue['status'] ||
			changes.task.currentValue['assignedTo'] !== changes.task.previousValue['assignedTo']
		)) {
			// on parent task model change
		}
	}

	/**
	 * Determines if Done button can be shown.
	 */
	showDone(): boolean {
		return this.taskActionInfoModel && [TaskStatus.READY, TaskStatus.STARTED].indexOf(this.taskActionInfoModel.status) >= 0;
	}

	/**
	 * Determines if Start button can be shown.
	 */
	showStart(): boolean {
		return this.taskActionInfoModel && [TaskStatus.READY].indexOf(this.taskActionInfoModel.status) >= 0;
	}

	/**
	 * Determines if Assign to me button can be shown.
	 */
	showAssignToMe(): boolean {
		return (this.taskActionInfoModel
			&&
			(	( !this.taskActionInfoModel.assignedTo
				||
				this.userContext.person.id !== this.taskActionInfoModel.assignedTo )
				&&
				[TaskStatus.READY, TaskStatus.PENDING, TaskStatus.STARTED].indexOf(this.taskActionInfoModel.status) >= 0
		) );
	}

	/**
	 * Determines if Neighborhood button can be shown.
	 */
	showNeighborhood(): boolean {
		return this.taskActionInfoModel
			&& (this.taskActionInfoModel.predecessors + this.taskActionInfoModel.successors > 0);
	}

	/**
	 * Determines if Reset button can be shown.
	 */
	showReset(): boolean {
		return this.taskActionInfoModel
			&& this.taskActionInfoModel.apiActionId && this.taskActionInfoModel.status === TaskStatus.HOLD;
	}

	/**
	 * Determines if Invoke button can be shown.
	 */
	showInvoke(): boolean {
		if (this.taskActionInfoModel
			&& this.taskActionInfoModel.invokeButton
			&& this.taskActionInfoModel.invokeButton !== null) {
			this.invokeButton = this.taskActionInfoModel.invokeButton;
			return true;
		}
		return false;
	}

	/**
	 * Determines if Delay button can be shown.
	 */
	showDelay(): boolean {
		return this.taskActionInfoModel
			&& this.taskActionInfoModel.category && this.taskActionInfoModel.category !== 'moveday'
			&& this.taskActionInfoModel.status === TaskStatus.READY;
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

	/**
	 * Emit the reset event
	 */
	onDelay(delayNumber: number): void {
		this.delay.emit(delayNumber);
	}

	/**
	 * Emit the show details event
	 */
	onShowDetails(): void {
		this.details.emit();
	}
}
