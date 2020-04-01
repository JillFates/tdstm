// Angular
import {Component, ComponentFactoryResolver, Input, OnInit} from '@angular/core';
// Model
import { ModalType} from '../../../../shared/model/constants';
import {TaskDetailModel} from '../../model/task-detail.model';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {TaskSuccessorPredecessorColumnsModel} from '../../model/task-successor-predecessor-columns.model';
import {TaskNotesColumnsModel} from '../../../../shared/components/task-notes/model/task-notes-columns.model';
import {Permission} from '../../../../shared/model/permission.model';
import {SHARED_TASK_SETTINGS} from '../../model/shared-task-settings';
import {AlertType} from '../../../../shared/model/alert.model';
import { UserContextModel } from '../../../auth/model/user-context.model';
import { TaskActionInfoModel } from '../../model/task-action-info.model';
import {
	Dialog,
	DialogButtonType,
	DialogConfirmAction,
	DialogExit,
	DialogService,
	ModalSize
} from 'tds-component-library';
// Component
import {AssetShowComponent} from '../../../assetExplorer/components/asset/asset-show.component';
import { TaskActionSummaryComponent } from '../task-actions/task-action-summary.component';
import {TaskEditCreateComponent} from '../edit-create/task-edit-create.component';
// Service
import {TaskService} from '../../service/task.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TaskEditCreateModelHelper} from '../common/task-edit-create-model.helper';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {WindowService} from '../../../../shared/services/window.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import { UserContextService } from '../../../auth/service/user-context.service';
import * as R from 'ramda';
import {TaskStatus} from '../../model/task-edit-create.model';
import {ValidationUtils} from '../../../../shared/utils/validation.utils';

@Component({
	selector: `task-detail`,
	templateUrl: 'task-detail.component.html',
	styles: []
})
export class TaskDetailComponent extends Dialog implements OnInit {
	@Input() data: any;

	protected modalType = ModalType;
	protected dateFormat: string;
	protected dateFormatTime: string;
	protected userTimeZone: string;
	protected currentUserId: number;
	public modelHelper: TaskEditCreateModelHelper;
	public dataGridTaskPredecessorsHelper: DataGridOperationsHelper;
	public dataGridTaskSuccessorsHelper: DataGridOperationsHelper;
	public dataGridTaskNotesHelper: DataGridOperationsHelper;
	public taskSuccessorPredecessorColumnsModel = new TaskSuccessorPredecessorColumnsModel();
	protected taskNotesColumnsModel = new TaskNotesColumnsModel();
	public collapsedTaskDetail = false;
	protected hasCookbookPermission = false;
	public hasEditTaskPermission = false;
	public hasDeleteTaskPermission = false;
	public hasCreateTaskPermission = false;
	public model: any = {};
	public SHARED_TASK_SETTINGS = SHARED_TASK_SETTINGS;
	private hasChanges: boolean;
	private userContext: UserContextModel;
	private taskActionInfoModel: TaskActionInfoModel = null;
	private taskDetailModel: TaskDetailModel;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private taskManagerService: TaskService,
		private dialogService: DialogService,
		private userPreferenceService: PreferenceService,
		private permissionService: PermissionService,
		private translatePipe: TranslatePipe,
		private windowService: WindowService,
		private notifierService: NotifierService,
		private userContextService: UserContextService) {
		super();

		this.userContextService.getUserContext().subscribe((userContext: UserContextModel) => {
			this.userContext = userContext;
		});
	}

	ngOnInit() {
		this.taskDetailModel = R.clone(this.data.taskDetailModel);

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			show: () => this.hasEditTaskPermission,
			type: DialogButtonType.ACTION,
			action: this.editTaskDetail.bind(this)
		});

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			show: () => this.hasDeleteTaskPermission,
			type: DialogButtonType.ACTION,
			action: this.deleteTask.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.hasChanges = false;
		this.dateFormat = this.userPreferenceService.getUserDateFormat();
		this.userTimeZone = this.userPreferenceService.getUserTimeZone();

		if (this.taskDetailModel.detail && this.taskDetailModel.detail.currentUserId) {
			this.currentUserId = parseInt(this.taskDetailModel.detail.currentUserId, 10);
		} else {
			this.currentUserId = this.userContext.user.id;
		}
		this.loadTaskDetail();
		this.hasCookbookPermission = this.permissionService.hasPermission(Permission.CookbookView) || this.permissionService.hasPermission(Permission.CookbookEdit);
		this.hasEditTaskPermission = this.permissionService.hasPermission(Permission.TaskEdit);
		this.hasDeleteTaskPermission = this.permissionService.hasPermission(Permission.TaskDelete);
		this.hasCreateTaskPermission = this.permissionService.hasPermission(Permission.TaskCreate);
		setTimeout(() => {
			this.setTitle(this.taskDetailModel.modal.title);
		});
	}

	private prepareTaskActionButtons(): void {
		this.buttons = this.buttons.filter((button: any) => {
			return (['toPlay', 'toDone', 'toInvoke', 'toResetAction', 'toAssignTo', 'toNeighborhood'].indexOf(button.name) === -1)
		});

		this.buttons.push({
			name: 'toPlay',
			icon: 'play',
			text: 'Start',
			show: () => this.taskActionInfoModel && [TaskStatus.READY].indexOf(this.taskActionInfoModel.status) >= 0,
			type: DialogButtonType.CONTEXT,
			action: this.onStartTask.bind(this)
		});

		this.buttons.push({
			name: 'toDone',
			icon: 'check',
			text: 'Done',
			show: () => this.taskActionInfoModel && [TaskStatus.READY, TaskStatus.STARTED].indexOf(this.taskActionInfoModel.status) >= 0,
			type: DialogButtonType.CONTEXT,
			action: this.onDoneTask.bind(this)
		});

		this.buttons.push({
			name: 'toInvoke',
			icon: 'cog',
			text: ((this.taskActionInfoModel.invokeButton && this.taskActionInfoModel.invokeButton.label) ? this.taskActionInfoModel.invokeButton.label : 'Invoke'),
			show: () => this.showInvoke(),
			disabled: () => !this.permissionService.hasPermission(Permission.ActionInvoke) || (this.taskActionInfoModel.invokeButton && this.taskActionInfoModel.invokeButton.disabled),
			type: DialogButtonType.CONTEXT,
			action: this.onInvoke.bind(this)
		});

		this.buttons.push({
			name: 'toResetAction',
			icon: 'power',
			text: 'Reset Action',
			show: () => this.taskActionInfoModel && this.taskActionInfoModel.apiActionId && this.taskActionInfoModel.status === TaskStatus.HOLD,
			type: DialogButtonType.CONTEXT,
			action: this.onReset.bind(this)
		});

		this.buttons.push({
			name: 'toAssignTo',
			icon: 'user',
			text: 'Assign To Me',
			show: () => this.showAssignToMe(),
			type: DialogButtonType.CONTEXT,
			action: this.onAssignToMe.bind(this)
		});

		this.buttons.push({
			name: 'toNeighborhood',
			icon: 'power',
			text: 'Neighborhood',
			show: () => this.taskActionInfoModel && (this.taskActionInfoModel.predecessors + this.taskActionInfoModel.successors > 0),
			type: DialogButtonType.CONTEXT,
			action: this.onNeighborhood.bind(this)
		});
	}

	/**
	 * Load All Asset Class and Retrieve
	 */
	private loadTaskDetail(): void {
		this.taskManagerService.getTaskDetails(this.taskDetailModel.id)
			.subscribe((res) => {
				if (!res) {
					super.onCancelClose();
					return;
				}
				this.dateFormatTime = this.userPreferenceService.getUserDateTimeFormat();
				this.taskDetailModel.detail = res;

				this.modelHelper = new TaskEditCreateModelHelper(
					this.userTimeZone,
					this.userPreferenceService.getUserDateFormat(),
					this.taskManagerService,
					this.dialogService,
					this.translatePipe,
					this.componentFactoryResolver);
				this.model = this.modelHelper.getModelForDetails(this.taskDetailModel);
				this.model.instructionLink = this.modelHelper.getInstructionsLink(this.taskDetailModel.detail);

				this.dataGridTaskPredecessorsHelper = new DataGridOperationsHelper(this.model.predecessorList, null, null, null, 2000);
				this.dataGridTaskSuccessorsHelper = new DataGridOperationsHelper(this.model.successorList, null, null, null, 2000);
				// Notes are coming into an Array of Arrays...
				this.dataGridTaskNotesHelper = new DataGridOperationsHelper(this.modelHelper.generateNotes(this.model.notesList), null, null, null, 2000);
				// Convert the Duration into a Human Readable form
				this.model.durationText = DateUtils.formatDuration(this.model.duration, this.model.durationScale);

				// get the class corresponding to this asset
				this.taskManagerService.getClassForAsset(this.model.asset.id)
					.subscribe((result: any) => {
						if (result) {
							const assetClass = this.model.assetClasses.find((asset: any) => asset.id === result.assetClass)
							if (assetClass) {
								this.model.assetClass = assetClass;
							}
						}
					});
			});
		const taskId = parseInt(this.taskDetailModel.id, 0);
		this.taskManagerService.getBulkTaskActionInfo([taskId])
			.subscribe((result: TaskActionInfoModel[]) => {
				if (result && result[taskId]) {
					this.taskActionInfoModel = result[taskId];
					this.prepareTaskActionButtons();
				}
			});
	}

	/**
	 * Open the Task Detail
	 * @param task
	 */
	public openTaskDetail(task: any, modalType: ModalType): void {
		super.onCancelClose({commentInstance: {...task, id: task.taskId}, shouldOpenTask: true});
	}

	public onCollapseTaskDetail(): void {
		this.collapsedTaskDetail = !this.collapsedTaskDetail;
	}

	/**
	 * Close Dialog
	 */
	public cancelCloseDialog(): void {
		super.onCancelClose({hasChanges: this.hasChanges});
	}
	/**
	 * Prompt confirm delete a task
	 * delegate operation to host component
	 */
	deleteTask(): void {
		this.dialogService.confirm(
			this.translatePipe.transform(
				'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'
			),
			this.translatePipe.transform(
				'TASK_MANAGER.DELETE_TASK'
			)
		)
			.subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					super.onCancelClose({id: this.taskDetailModel, isDeleted: true})
				}
			});
	}
	/**
	 * Open view to edit task details
	 */
	public editTaskDetail(): void {
		super.onCancelClose({id: this.taskDetailModel, shouldEdit: true});
	}

	/**
	 * Change the task status to started
	 */
	onStartTask(): void {
		const payload = {
			id: this.model.id.toString(),
			status: this.modelHelper.STATUS.STARTED,
			currentStatus: this.model.status
		};

		this.taskManagerService.updateTaskStatus(payload)
			.subscribe((result) => {
				this.hasChanges = true;
				this.loadTaskDetail();
			});
	}
	/**
	 * Change the task status to done
	 */
	onDoneTask(): void {
		const payload = {
			id: this.model.id.toString(),
			status: this.modelHelper.STATUS.COMPLETED,
			currentStatus: this.model.status
		};

		this.taskManagerService.updateTaskStatus(payload)
			.subscribe((result) => {
				this.hasChanges = true;
				this.loadTaskDetail();
			});
	}
	/**
	 * Open the neighborhood window
	 */
	onNeighborhood(): void {
		this.windowService.getWindow().open(`../task/taskGraph?neighborhoodTaskId=${this.model.id}`, '_blank')
	}

	/**
	 * Assign the task to the current user
	 */
	onAssignToMe(): void {
		const payload = {
			id: this.model.id.toString(),
			status: this.model.status
		};

		this.taskManagerService.assignToMe(payload)
			.subscribe((result) => {
				this.hasChanges = true;
				this.loadTaskDetail();
			});
	}

	/**
	 * Invoke an api action
	 */
	onInvoke(): void {
		this.taskManagerService.invokeAction(this.model.id)
			.subscribe((result) => {
				this.hasChanges = true;
				this.loadTaskDetail();
			});
	}

	/**
	 * Reset the current task action
	 */
	onReset(): void {
		this.taskManagerService.resetTaskAction(this.model.id)
			.subscribe((result) => {
				this.hasChanges = true;
				this.loadTaskDetail();
			});
	}

	/**
	 * Show the asset using the link asset
	 */
	protected onOpenLinkAsset() {
		const id = this.model.asset.id;
		const assetClass = this.taskManagerService.getAssetCategory(this.model.assetClass.id);

		if (assetClass) {
			super.onCancelClose();
			this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: AssetShowComponent,
				data: {
					assetId: id,
					assetClass: assetClass
				},
				modalConfiguration: {
					title: 'Asset',
					draggable: true,
					modalSize: ModalSize.CUSTOM,
					modalCustomClass: 'custom-asset-modal-dialog'
				}
			}).subscribe();
		} else {
			this.notifierService.broadcast({
				name: AlertType.DANGER,
				message: 'Invalid asset type'
			});
		}
	}

	/**
	 * Open the Task Create Dialog
	 * on Success event adding a task it will reload the model
	 * @param {any[]} taskList contains the model array with the task list
	 * @param {any} gridHelper point out  to the corresponding grid which is using the list
	 */
	public onAddTaskDependency(taskList: any[], gridHelper: any): void {
		let taskCreateModel: TaskDetailModel = {
			id: this.model.id,
			modal: {
				title: 'Create Task',
				type: ModalType.CREATE
			},
			detail: {
				assetClass: this.model.assetClass,
				assetEntity: this.model.asset.id,
				assetName:  this.model.assetName,
				currentUserId: this.model.assignedTo.id,
				event: this.model.event
			}
		};

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: TaskEditCreateComponent,
			data: {
				taskDetailModel: taskCreateModel
			},
			modalConfiguration: {
				title: 'Create Task',
				draggable: true,
				modalSize: ModalSize.CUSTOM,
				modalCustomClass: 'custom-task-modal-edit-view-create'
			}
		}).subscribe((data: any) => {
			if (data.status === DialogExit.ACCEPT) {
				const task = {
					category: data.assetComment.category,
					desc: data.assetComment.comment,
					id: data.assetComment.id,
					model: {
						id: data.assetComment.id,
						text: data.assetComment.comment
					},
					originalId: '',
					status: data.assetComment.status,
					taskId: data.assetComment.id,
					taskNumber: data.assetComment.taskNumber
				};

				taskList.unshift(task);
				gridHelper.addDataItem(task);
				console.log('Reloading');
				const payload = this.modelHelper.getPayloadForUpdate();

				this.taskManagerService.updateTask(payload)
					.subscribe((result) => this.loadTaskDetail());
			}
		});
	}

	/**
	 * Create a note and update the datagrid task notes component
	 */
	public createNote() {
		this.modelHelper.onCreateNote()
			.subscribe((result) => {
				if (result) {
					this.model.notesList = result && result.data || [];
					this.dataGridTaskNotesHelper =
						new DataGridOperationsHelper(
							this.modelHelper.generateNotes(this.model.notesList), null, null);
				}
			});
	}

	/**
	 * Opens the action summary modal.
	 */
	openTaskActionSummaryDetailHandler(): void {
		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: TaskActionSummaryComponent,
			data: {
				taskDetailModel: this.taskDetailModel
			},
			modalConfiguration: {
				title: 'Task',
				draggable: true,
				modalSize: ModalSize.LG
			}
		}).subscribe();
	}

	showInvoke(): boolean {
		if (this.taskActionInfoModel
			&& this.taskActionInfoModel.invokeButton
			&& this.taskActionInfoModel.invokeButton !== null) {
			this.taskActionInfoModel.invokeButton = this.taskActionInfoModel.invokeButton;
			return true;
		}
		return false;
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
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}

	/**
	 * On double click
	 */
	public onDoubleClick(event: MouseEvent): void {
		this.changeToEditViewOnDoubleClick(event);
		super.onDoubleClick(event);
	}

	/**
	 * Change the view to edit view if the click was made over a not banned css class
	 * @param event MouseEvent info where the double click was made
	 */
	private changeToEditViewOnDoubleClick(event: MouseEvent): void {
		const bannedClasses = ['btn', 'actionable-link', 'k-grid'];
		if (!ValidationUtils.isBannedClass(bannedClasses, event)) {
			// move to edit mode
			this.editTaskDetail();
		}
	}

}
