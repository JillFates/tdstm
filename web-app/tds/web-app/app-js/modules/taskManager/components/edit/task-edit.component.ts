import {Component, HostListener, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {KEYSTROKE, ModalType} from '../../../../shared/model/constants';
import {UIDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {TaskDetailModel} from './../model/task-detail.model';
import {TaskService} from '../../service/task.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {TaskSuccessorPredecessorColumnsModel} from './../model/task-successor-predecessor-columns.model';
import {TaskNotesColumnsModel} from './../model/task-notes-columns.model';
import {RowClassArgs} from '@progress/kendo-angular-grid';
import {Permission} from '../../../../shared/model/permission.model';
import {PermissionService} from '../../../../shared/services/permission.service';
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';
import {ComboBoxSearchModel} from '../../../../shared/components/combo-box/model/combobox-search-param.model';

@Component({
	selector: `task-edit`,
	templateUrl: '../tds/web-app/app-js/modules/taskManager/components/edit/task-edit.component.html',
	styles: []
})
export class TaskEditComponent extends UIExtraDialog  implements OnInit {

	public modalType = ModalType;
	public dateFormat: string;
	public dateFormatTime: string;
	public dataGridTaskPredecessorsHelper: DataGridOperationsHelper;
	public dataGridTaskSuccessorsHelper: DataGridOperationsHelper;
	public dataGridTaskNotesHelper: DataGridOperationsHelper;
	public taskSuccessorPredecessorColumnsModel = new TaskSuccessorPredecessorColumnsModel();
	public taskNotesColumnsModel = new TaskNotesColumnsModel();
	public collapsedTaskDetail = false;
	public hasCookbookPermission = false;
	public modalOptions: DecoratorOptions;
	public model: any = null;
	public getAssetList: Function;

	constructor(
		public taskDetailModel: TaskDetailModel,
		public taskManagerService: TaskService,
		private dialogService: UIDialogService,
		public promptService: UIPromptService,
		public userPreferenceService: PreferenceService,
		private permissionService: PermissionService) {

		super('#task-edit-component');
		this.modalOptions = { isResizable: true, isCentered: true };
	}

	ngOnInit() {
		this.model = this.extractModel();
		this.getAssetList = this.taskManagerService.getAssetListForComboBox.bind(this.taskManagerService);

		this.taskManagerService.getStatusList(this.model.id)
			.subscribe((data: string[]) => this.model.statusList = data);

		this.taskManagerService.getAssignedTeam(this.model.id)
			.subscribe((data: any[]) => {
				this.model.personList = data.map((item) => ({id: item.id, text: item.nameRole}))
			});

		this.hasCookbookPermission = this.permissionService.hasPermission(Permission.CookbookView) || this.permissionService.hasPermission(Permission.CookbookEdit);
	}

	/**
	 * Extract only the model fields used by the view
	 */
	extractModel(): any {
		const detail = this.taskDetailModel.detail;
		const asset = detail['assetComment'] || {};

		return  {
			id: asset.id,
			assetName: detail.assetName,
			comment:  asset.comment || '',
			sendNotification: asset.sendNotification,
			assetClass: {id: detail.assetClass, text: ''},
			assetClasses: Object.keys(detail.assetClasses || {}).map((key: string) => ({id: key, text: detail.assetClasses[key]}) ),
			status: asset.status,
			statusList: [],
			personList: [],
			asset: {id: detail.assetId, text: detail.assetName},
			assignedTo: {id : asset.assignedTo.id, text: detail.assignedTo}
		}
	}

	/**
	 * Pass Service as Reference
	 * @param {ComboBoxSearchModel} searchParam
	 * @returns {Observable<any>}
	 */
	public getAssetListForComboBox(searchParam: ComboBoxSearchModel): Observable<any> {
		return this.taskManagerService.getAssetListForComboBox(searchParam);
	}

	onAssetClassChange(asset): void {
	}

	onStatusChange(asset): void {
	}

	onAssetNameChange(asset): void {
	}

	onPersonChange(asset): void {
	}

	/**
	 * Open the Task Detail
	 * @param task
	 */
	public openTaskDetail(task: any, modalType: ModalType): void {
		this.close({commentInstance: {id: task.taskId}});
	}

	/**
	 * Create the structure of the notes from the simple array returned in the API
	 * @param notes
	 * @returns {Array<any>}
	 */
	private generateNotes(notes: any): Array<any> {
		let noteList = new Array<any>();
		notes.forEach((note) => {
			noteList.push({
				dateCreated: note[0],
				createdBy: note[1],
				note: note[2],
			});
		});

		return noteList;
	}

	protected onSave(): void {
		/* this.taskManagerService.saveComment(this.singleCommentModel).subscribe((res) => {
			this.close();
		}); */
		this.close();
	}

	/**
	 * Delete the Asset Comment
	 */
	protected onDelete(): void {
		this.promptService.open(
			'Confirmation Required',
			'Confirm deletion of this record. There is no undo for this action?',
			'Confirm', 'Cancel')
			.then(confirm => {
				if (confirm) {
					// this.taskManagerService.deleteTaskComment(this.singleCommentModel.id).subscribe((res) => {
					this.close();
					// });
				}
			})
			.catch((error) => console.log(error));
	}

	protected resizeWindow(resize: any): void {
		console.log(resize);
	}

	/**
	 * Get the assigned team name
	 * @param commentId
	 * @param assignedToId
	 * @returns {any}
	 */
	public getAssignedTeam(commentId: any, assignedToId: any): void {
		this.taskManagerService.getAssignedTeam(commentId).subscribe((res: any) => {
			let team = res.filter((team) => team.id === assignedToId);
			if (team) {
				// is this a real case in the legacy view?
				if (team.length > 1) {
					team = team[0];
				}
				this.taskDetailModel.detail.assignedTeam = team.nameRole.split(':')[0];
			}
		});
	}

	public onCollapseTaskDetail(): void {
		this.collapsedTaskDetail = !this.collapsedTaskDetail;
	}

	/**
	 * Change the background color based on the task status
	 * @param {RowClassArgs} context
	 * @returns {string}
	 */
	protected rowStatusColor(context: RowClassArgs) {
		return 'task-' + context.dataItem.status.toLowerCase();
	}

	/**
	 * Close Dialog
	 */
	protected cancelCloseDialog(): void {
		this.dismiss();
	}
	/**
	 * Prompt confirm delete a task
	 * delegate operation to host component
	 */
	deleteTask(): void {
		this.promptService.open(
			'Confirmation Required',
			'Confirm deletion of this task. There is no undo for this action',
			'Confirm', 'Cancel').then(result => {
			if (result) {
				this.close({id: this.taskDetailModel, isDeleted: true})
			}
		});

	}
}