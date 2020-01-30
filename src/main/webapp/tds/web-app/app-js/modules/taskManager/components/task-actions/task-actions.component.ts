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
		<section class="task-actions-component " [ngClass]="{'btn-link': !displayAsButtons, 'btn-group': !displayAsButtons, 'btn-sm': !displayAsButtons}">
            <tds-button *ngIf="showStart()" (click)="onStart()" theme="success" [icon]="displayAsButtons ? 'play' : ''">
				Start
			</tds-button>
            <tds-button *ngIf="showDone()" (click)="onDone()"  [icon]="displayAsButtons ? 'check' : ''">
				Done
			</tds-button>
            <tds-button *ngIf="showInvoke()" [disabled]="!hasInvokePermission || invokeButton?.disabled" (click)="onInvoke()"  [icon]="displayAsButtons ? 'cog' : ''">
				{{invokeButton?.label || 'Invoke'}}
			</tds-button>
            <tds-button *ngIf="showReset()" (click)="onReset()"  [icon]="displayAsButtons ? 'power' : ''">
				Reset Action
            </tds-button>
            <tds-button *ngIf="showDetails" (click)="onShowDetails()" [icon]="displayAsButtons ? 'zoom-in' : ''">
                Details
            </tds-button>
            <tds-button *ngIf="showAssignToMe()" (click)="onAssignToMe()" [icon]="displayAsButtons ? 'user' : ''">
                Assign To Me
            </tds-button>
            <tds-button *ngIf="showNeighborhood()" (click)="onNeighborhood()" [icon]="displayAsButtons ? 'align-left' : ''">
                Neighborhood
            </tds-button>
            <div *ngIf="showDelayActions && showDelay()"
                 class="task-action-buttons">
                <label class="delay">Delay for:</label>
                <tds-button (click)="onDelay(1)">
                    1 day
                </tds-button>
                <tds-button (click)="onDelay(2)">
                    2 days
                </tds-button>
                <tds-button (click)="onDelay(7)">
                    7 days
                </tds-button>
            </div>
		</section>
	`,
})
export class TaskActionsComponent implements OnInit, OnChanges {
	@Input() taskStatus: string;
	@Input() displayAsButtons: boolean;
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
