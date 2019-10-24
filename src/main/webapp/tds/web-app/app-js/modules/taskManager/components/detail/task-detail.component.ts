import {Component, HostListener, OnInit} from '@angular/core';
import {DIALOG_SIZE, KEYSTROKE, ModalType} from '../../../../shared/model/constants';
import {UIDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {TaskDetailModel} from '../../model/task-detail.model';
import {TaskService} from '../../service/task.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {TaskSuccessorPredecessorColumnsModel} from '../../model/task-successor-predecessor-columns.model';
import {TaskNotesColumnsModel} from '../../../../shared/components/task-notes/model/task-notes-columns.model';
import {Permission} from '../../../../shared/model/permission.model';
import {PermissionService} from '../../../../shared/services/permission.service';
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';
import {TaskEditComponent} from '../edit/task-edit.component';
import {clone} from 'ramda';
import {TaskEditCreateModelHelper} from '../common/task-edit-create-model.helper';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {WindowService} from '../../../../shared/services/window.service';
import {SHARED_TASK_SETTINGS} from '../../model/shared-task-settings';
import {AssetShowComponent} from '../../../assetExplorer/components/asset/asset-show.component';
import {AlertType} from '../../../../shared/model/alert.model';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {TaskCreateComponent} from '../create/task-create.component';
import { UserContextService } from '../../../auth/service/user-context.service';
import { UserContextModel } from '../../../auth/model/user-context.model';
import { TaskActionSummaryComponent } from '../task-actions/task-action-summary.component';
import { TaskActionInfoModel } from '../../model/task-action-info.model';

@Component({
	selector: `task-detail`,
	templateUrl: 'task-detail.component.html',
	styles: []
})
export class TaskDetailComponent extends UIExtraDialog  implements OnInit {

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
	public modalOptions: DecoratorOptions;
	public model: any = {};
	public SHARED_TASK_SETTINGS = SHARED_TASK_SETTINGS;
	private hasChanges: boolean;
	private userContext: UserContextModel;
	taskActionInfoModel: TaskActionInfoModel;

	constructor(
		private taskDetailModel: TaskDetailModel,
		private taskManagerService: TaskService,
		private dialogService: UIDialogService,
		private promptService: UIPromptService,
		private userPreferenceService: PreferenceService,
		private permissionService: PermissionService,
		private translatePipe: TranslatePipe,
		private windowService: WindowService,
		private notifierService: NotifierService,
		private userContextService: UserContextService) {

		super('#task-detail-component');
		this.modalOptions = { isResizable: true, isCentered: true, isDraggable: false };
		this.userContextService.getUserContext().subscribe((userContext: UserContextModel) => {
			this.userContext = userContext;
		});
	}

	ngOnInit() {
		this.hasChanges = false;
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
	}

	/**
	 * Load All Asset Class and Retrieve
	 */
	private loadTaskDetail(): void {
		this.taskManagerService.getTaskDetails(this.taskDetailModel.id)
			.subscribe((res) => {
				if (!res) {
					this.dismiss();
					return;
				}
				this.dateFormat = this.userPreferenceService.getUserDateFormat();
				this.dateFormatTime = this.userPreferenceService.getUserDateTimeFormat();
				this.taskDetailModel.detail = res;

				this.modelHelper = new TaskEditCreateModelHelper(
					this.userTimeZone,
					this.userPreferenceService.getUserCurrentDateFormatOrDefault(),
					this.taskManagerService,
					this.dialogService,
					this.translatePipe);
				this.model = this.modelHelper.getModelForDetails(this.taskDetailModel);
				this.model.instructionLink = this.modelHelper.getInstructionsLink(this.taskDetailModel.detail);

				this.dataGridTaskPredecessorsHelper = new DataGridOperationsHelper(this.model.predecessorList, null, null);
				this.dataGridTaskSuccessorsHelper = new DataGridOperationsHelper(this.model.successorList, null, null);
				// Notes are coming into an Array of Arrays...
				this.dataGridTaskNotesHelper = new DataGridOperationsHelper(this.modelHelper.generateNotes(this.model.notesList), null, null);
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
		this.taskManagerService.getTaskActionInfo(parseInt(this.taskDetailModel.id, 0))
			.subscribe((result: TaskActionInfoModel) => {
			this.taskActionInfoModel = result;
		});
	}

	/**
	 * Open the Task Detail
	 * @param task
	 */
	public openTaskDetail(task: any, modalType: ModalType): void {
		this.close({commentInstance: {...task, id: task.taskId}, shouldOpenTask: true});
	}

	public onCollapseTaskDetail(): void {
		this.collapsedTaskDetail = !this.collapsedTaskDetail;
	}

	/**
	 * Close Dialog
	 */
	public cancelCloseDialog(): void {
		this.dismiss(this.hasChanges);
	}
	/**
	 * Prompt confirm delete a task
	 * delegate operation to host component
	 */
	deleteTask(): void {
		this.promptService.open(
			this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED')	,
			this.translatePipe.transform('TASK_MANAGER.DELETE_TASK')	,
			this.translatePipe.transform('GLOBAL.CONFIRM'),
			this.translatePipe.transform('GLOBAL.CANCEL'))
			.then(result => {
				if (result) {
					this.close({id: this.taskDetailModel, isDeleted: true})
				}
			});
	}
	/**
	 * Open view to edit task details
	 */
	public editTaskDetail(): void {
		this.model.modal = {
			title: 'Edit Task',
			type: ModalType.EDIT
		};

		this.dialogService.extra(TaskEditComponent,
			[
				{provide: TaskDetailModel, useValue: clone(this.model)}
			], false, false)
			.then(result => {
				if (result) {
					if (result.isDeleted) {
						this.close({id: this.taskDetailModel, isDeleted: true})
						return;
					}

					this.hasChanges = true;
					this.loadTaskDetail();
				}
			}).catch(result => {
			this.dismiss(this.hasChanges);
		});
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
			this.dialogService.replace(AssetShowComponent,
				[UIDialogService,
					{ provide: 'ID', useValue: id },
					{ provide: 'ASSET', useValue: assetClass }
				], DIALOG_SIZE.LG);

			this.close();
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
			id: this.model.asset.id, // dataItem.common_id,
			modal: {
				title: 'Create Task',
				type: ModalType.CREATE
			},
			detail: {
				assetClass: this.model.assetClass,
				assetEntity: this.model.asset.id,
				assetName:  this.model.assetName,
				currentUserId: this.model.assignedTo.id
			}
		};

		this.dialogService.extra(TaskCreateComponent, [
			{provide: TaskDetailModel, useValue: taskCreateModel}
		], false, false)
			.then(result => {
				if (result) {
					const task = {
						category: result.assetComment.category,
						desc: result.assetComment.comment,
						id: result.assetComment.id,
						model: {
							id: result.assetComment.id,
							text: result.assetComment.comment
						},
						originalId: '',
						status: result.assetComment.status,
						taskId: result.assetComment.id,
						taskNumber: result.assetComment.taskNumber
					};

					taskList.unshift(task);
					gridHelper.addDataItem(task);
					console.log('Reloading');
					const payload = this.modelHelper.getPayloadForUpdate();

					this.taskManagerService.updateTask(payload)
						.subscribe((result) => this.loadTaskDetail());
				}

			}).catch(result => {
			console.log('Cancel:', result);
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
		this.dialogService.extra(TaskActionSummaryComponent, [
			{ provide: TaskDetailModel, useValue: this.taskDetailModel }
		]).then((result) => {
			// do nothing, modal was closed;
		}).catch(result => {
			// do nothing, modal was closed
		});
	}
}
