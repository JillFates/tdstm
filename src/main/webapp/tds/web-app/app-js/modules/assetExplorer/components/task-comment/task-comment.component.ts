// Angular
import {Component, Input, OnInit, Output, EventEmitter, ComponentFactoryResolver} from '@angular/core';
// Model
import {
	TaskColumnsModel,
	CommentColumnsModel,
} from './model/task-comment-columns.model';
import {ModalType} from '../../../../shared/model/constants';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {TaskDetailModel} from '../../../taskManager/model/task-detail.model';
import {TaskEditCreateModelHelper} from '../../../taskManager/components/common/task-edit-create-model.helper';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {Permission} from '../../../../shared/model/permission.model';
import {AssetCommentModel} from '../../../assetComment/model/asset-comment.model';
import {DialogConfirmAction, DialogService, ModalSize} from 'tds-component-library';
// Component
import {TaskDetailComponent} from '../../../taskManager/components/detail/task-detail.component';
import {TaskEditCreateComponent} from '../../../taskManager/components/edit-create/task-edit-create.component';
import {AssetCommentViewEditComponent} from '../../../assetComment/components/view-edit/asset-comment-view-edit.component';
// Service
import {TaskCommentService} from '../../service/task-comment.service';
import {TaskService} from '../../../taskManager/service/task.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {
	PREFERENCES_LIST,
	PreferenceService,
} from '../../../../shared/services/preference.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {UserContextService} from '../../../auth/service/user-context.service';

@Component({
	selector: `task-comment`,
	templateUrl: 'task-comment.component.html',
	styles: [],
})
export class TaskCommentComponent implements OnInit {
	@Input('asset-id') id: number;
	@Input('has-publish-permission') hasPublishPermission ? = false;
	@Input('can-edit-comments') canEdit ? = false;
	@Input('can-edit-tasks') canEditTasks ? = false;
	@Input('asset-name') assetName: string;
	@Input('asset-type') assetType: string;
	@Input('show-task') showTask: boolean;
	@Input('show-comment') showComment: boolean;
	@Input('asset-class') assetClass: string;
	@Input('user-id') currentUserId: string;
	@Output() taskCount = new EventEmitter<number>();
	@Output() commentCount = new EventEmitter<number>();

	// Grid Configuration for Task and Comment
	private dataGridTaskHelper: DataGridOperationsHelper;
	private dataGridCommentHelper: DataGridOperationsHelper;
	protected userTimeZone: string;

	protected taskColumnModel = new TaskColumnsModel();
	protected commentColumnModel = new CommentColumnsModel();
	private modalType = ModalType;
	private viewUnpublished = false;

	private showAllComments: boolean;
	private showAllTasks: boolean;
	private taskCommentsList: any[] = [];
	private userDateFormat;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private taskService: TaskCommentService,
		public taskManagerService: TaskService,
		private userContextService: UserContextService,
		private preferenceService: PreferenceService,
		private translate: TranslatePipe,
		private permissionService: PermissionService
	) {
		this.getPreferences();
	}

	ngOnInit(): void {
		this.userContextService
			.getUserContext()
			.subscribe((userContext: UserContextModel) => {
				this.userTimeZone = userContext.timezone;
				this.userDateFormat = userContext.dateFormat;
			});
		this.showAllComments = false;
		this.showAllTasks = false;
		this.createDataGrids();
	}

	/**
	 * Create the List for the Data Grids for Task and Comments
	 * @returns {any}
	 */
	private createDataGrids(): any {
		this.taskService.searchComments(this.id, '').subscribe(
			res => {
				this.taskCommentsList = res;
				this.dataGridTaskHelper = new DataGridOperationsHelper(
					this.getTaskWithFilter(),
					null,
					null
				);
				this.dataGridCommentHelper = new DataGridOperationsHelper(
					this.getCommentsWithFilter(),
					null,
					null
				);
				this.outputCommentCount();
				this.outputTaskCount();
			},
			err => console.log(err)
		);
	}

	/**
	 * Get the Tasks and change the list based on the Filters being applied
	 * @returns {any}
	 */
	public getTaskWithFilter(): any {
		return this.taskCommentsList
			.filter(
				comment =>
					this.viewUnpublished || comment.commentInstance.isPublished
			)
			.filter(
				comment =>
					comment.commentInstance.commentType === 'issue' ||
					comment.commentInstance.taskNumber
			)
			.filter(
				comment =>
					this.showAllTasks ||
					comment.commentInstance.status !== 'Completed'
			);
	}

	/**
	 * Get the Comments and change the list based on the Filters being applied
	 * @returns {any}
	 */
	public getCommentsWithFilter(): any {
		let filteredList = this.taskCommentsList.filter(
			comment =>
				comment.commentInstance.commentType === 'comment' &&
				comment.commentInstance.dateResolved === null
		);
		if (this.showAllComments) {
			filteredList = this.taskCommentsList.filter(
				comment => comment.commentInstance.commentType === 'comment'
			);
		}
		return filteredList;
	}

	protected outputCommentCount(): void {
		const commentCount = this.taskCommentsList.filter(
			comment => comment.commentInstance.commentType === 'comment'
		).length;
		this.commentCount.emit(commentCount);
	}

	protected outputTaskCount(): void {
		const taskCount = this.taskCommentsList.filter(
			comment =>
				comment.commentInstance.commentType === 'issue' ||
				comment.commentInstance.taskNumber
		).length;
		this.taskCount.emit(taskCount);
	}

	public getAssignedTo(comment): any {
		let assignedToRoleLabel = comment.role ? `/${comment.role}` : '';
		return (
			comment.assignedTo +
			(comment.commentInstance.commentType === 'comment'
				? ''
				: assignedToRoleLabel)
		);
	}

	/**
	 * Create a Comment
	 * @param comment
	 */
	public createComment(): void {
		let assetCommentModel: AssetCommentModel = {
			modal: {
				type: ModalType.CREATE,
			},
			archive: false,
			comment: '',
			category: '',
			assetClass: {
				text: this.assetType,
			},
			asset: {
				id: this.id,
				text: this.assetName,
			},
		};

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: AssetCommentViewEditComponent,
			data: {
				assetCommentModel: assetCommentModel
			},
			modalConfiguration: {
				title: 'Comment',
				draggable: true,
				modalSize: ModalSize.MD
			}
		}).subscribe((data: any) => {
			this.createDataGrids();
		});
	}

	/**
	 * Open the Comment Detail or Task Detail
	 * @param comment
	 */
	public openTaskComment(comment: any, modalType: ModalType): void {
		if (
			comment.commentInstance.taskNumber &&
			comment.commentInstance.taskNumber !== 'null'
		) {
			if (this.canOpenTaskDetail()) {
				this.openTaskDetail(comment);
			}
		} else {
			this.openCommentDetail(comment, modalType);
		}
	}

	/**
	 * Open the Comment Detail
	 * @param comment
	 */
	public openCommentDetail(comment: any, modalType: ModalType): void {
		let assetCommentModel: AssetCommentModel = {
			id: comment.commentInstance.id,
			modal: {
				title:
					modalType === ModalType.EDIT
						? 'Edit Comment'
						: 'Comment Detail',
				type: modalType,
			},
			archive: comment.commentInstance.dateResolved !== null,
			comment: comment.commentInstance.comment,
			category: comment.commentInstance.category,
			assetClass: {
				text: comment.assetType,
			},
			asset: {
				id: this.id,
				text: comment.assetName,
			},
			lastUpdated: comment.commentInstance.lastUpdated,
			dateCreated: comment.commentInstance.dateCreated,
		};

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: AssetCommentViewEditComponent,
			data: {
				assetCommentModel: assetCommentModel
			},
			modalConfiguration: {
				title: 'Comment',
				draggable: true,
				modalSize: ModalSize.MD
			}
		}).subscribe((data: any) => {
			this.createDataGrids();
		});
	}

	/**
	 * Open the Task Create
	 * @param dataItem
	 */
	public openTaskCreate(): void {
		let taskCreateModel: TaskDetailModel = {
			id: this.id.toString(),
			modal: {
				title: 'Create Task',
				type: ModalType.CREATE,
			},
			detail: {
				assetClass: this.assetType,
				assetEntity: this.id,
				assetName: this.assetName,
				currentUserId: this.currentUserId,
			},
		};

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: TaskEditCreateComponent,
			data: {
				taskDetailModel: taskCreateModel
			},
			modalConfiguration: {
				title: 'Task',
				draggable: true,
				modalSize: ModalSize.CUSTOM,
				modalCustomClass: 'custom-task-modal-edit-view-create'
			}
		}).subscribe((data: any) => {
			this.createDataGrids();
		});
	}

	/**
	 * Open the Task Detail
	 * @param dataItem
	 */
	public openTaskDetail(dataItem: any): void {
		let taskDetailModel: TaskDetailModel = {
			id: dataItem.commentInstance.id,
			modal: {
				title: 'Task Detail',
			},
			detail: {
				currentUserId: this.currentUserId,
			},
		};

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: TaskDetailComponent,
			data: {
				taskDetailModel: taskDetailModel
			},
			modalConfiguration: {
				title: 'Task',
				draggable: true,
				modalSize: ModalSize.CUSTOM,
				modalCustomClass: 'custom-task-modal-edit-view-create'
			}
		}).subscribe((data: any) => {
			if (data.isDeleted) {
				this.deleteTaskComment(dataItem).then(() =>
					this.createDataGrids()
				);
			} else if (data.commentInstance) {
				this.openTaskDetail(data);
			} else if (data.shouldEdit) {
				this.openTaskEdit({
					commentInstance: { id: data.id.id },
				});
			}
		});

	}

	/**
	 * Open the Task Edit
	 * @param dataItem
	 */
	public openTaskEdit(dataItem: any): void {
		let taskDetailModel: TaskDetailModel = new TaskDetailModel();

		this.taskManagerService
			.getTaskDetails(dataItem.commentInstance.id)
			.subscribe(res => {
				let modelHelper = new TaskEditCreateModelHelper(
					this.userTimeZone,
					this.userDateFormat,
					this.taskManagerService,
					this.dialogService,
					this.translate,
					this.componentFactoryResolver
				);
				taskDetailModel.detail = res;
				taskDetailModel.modal = {
					title: 'Task Edit',
					type: ModalType.EDIT,
				};

				let model = modelHelper.getModelForDetails(taskDetailModel);
				model.instructionLink = modelHelper.getInstructionsLink(
					taskDetailModel.detail
				);
				model.durationText = DateUtils.formatDuration(
					model.duration,
					model.durationScale
				);
				model.modal = taskDetailModel.modal;

				this.dialogService.open({
					componentFactoryResolver: this.componentFactoryResolver,
					component: TaskEditCreateComponent,
					data: {
						taskDetailModel: model
					},
					modalConfiguration: {
						title: 'Task',
						draggable: true,
						modalSize: ModalSize.CUSTOM,
						modalCustomClass: 'custom-task-modal-edit-view-create'
					}
				}).subscribe((data: any) => {
					if (data.isDeleted) {
						this.deleteTaskComment(dataItem).then(() =>
							this.createDataGrids()
						);
					} else if (data.commentInstance) {
						this.openTaskDetail(data);
					} else {
						this.createDataGrids();
					}
				});
			});
	}

	public reloadTasksGrid(): void {
		this.dataGridTaskHelper.reloadData(this.getTaskWithFilter());
	}

	public reloadCommentsGrid(): void {
		this.dataGridCommentHelper.reloadData(this.getCommentsWithFilter());
	}

	/**
	 * Safe the preference as soon a user change the value
	 */
	public onViewUnpublishedChange(): void {
		this.preferenceService
			.setPreference(
				PREFERENCES_LIST.VIEW_UNPUBLISHED,
				this.viewUnpublished.toString()
			)
			.subscribe();
	}

	/**
	 * Get Preference for the View Unpublished
	 * @returns {Observable<any>}
	 */
	private getPreferences(): void {
		this.preferenceService
			.getPreferences(PREFERENCES_LIST.VIEW_UNPUBLISHED)
			.subscribe((preferences: any) => {
				this.viewUnpublished = preferences[
					PREFERENCES_LIST.VIEW_UNPUBLISHED
					]
					? preferences[
					PREFERENCES_LIST.VIEW_UNPUBLISHED
					].toString() === 'true'
					: false;
			});
	}

	/**
	 * Prompt for delete the Asset Comment
	 */
	protected onDelete(dataItem: any): void {
		this.dialogService.confirm(
			'Confirmation Required',
			'Confirm deletion of this record. There is no undo for this action.'
		)
			.subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					this.deleteTaskComment(dataItem).then(deleted => {
						if (deleted) {
							this.outputCommentCount();
							this.outputTaskCount();
						}
					});
				}
			});
	}

	/**
	 * Delete the Asset Task/Comment
	 */
	private deleteTaskComment(dataItem: any): Promise<boolean> {
		return new Promise((resolve, reject) => {
			const commentId = dataItem.commentInstance.id;

			this.taskManagerService.deleteTaskComment(commentId).subscribe(
				res => {
					// delete the item
					this.dataGridTaskHelper.removeDataItem(dataItem);
					this.dataGridCommentHelper.removeDataItem(dataItem);
					// Reload Grids
					this.dataGridTaskHelper.reloadData(
						this.dataGridTaskHelper.gridData.data
					);
					this.dataGridCommentHelper.reloadData(
						this.dataGridCommentHelper.gridData.data
					);
					// update task and comment collections
					this.taskCommentsList = this.taskCommentsList.filter(
						comment => comment.commentInstance.id !== commentId
					);
					return resolve(true);
				},
				err => reject(false)
			);
		});
	}

	protected isTaskCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.TaskCreate);
	}

	protected isTaskEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.TaskEdit);
	}

	protected isTaskDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.TaskDelete);
	}

	protected isCommentCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.CommentCreate);
	}

	protected isCommentEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.CommentEdit);
	}

	protected isCommentDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.CommentDelete);
	}

	// NOTE TM-15753 - This method and the permissions checked inside are placed here to match
	// the permissions required on the back end to access this screens.
	// However according to the Role Permissions, the user should only need the 'TaskView' permission
	// that is available to all users. To change this in the BE is not trivial, a refactor should be
	// done in the FE (this is also accessed by the Task Manager) to be able to change the permissions on the BE,
	// because the Show and Edit functions on Tasks are not loosely coupled in the FE.
	// I'm adding this method here, that should be removed once this is taken care of in a sepparate ticket.
	protected canOpenTaskDetail(): boolean {
		return this.permissionService.hasPermission(Permission.TaskView);
	}
}