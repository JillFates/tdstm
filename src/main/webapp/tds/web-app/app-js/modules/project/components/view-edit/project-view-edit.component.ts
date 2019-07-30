import {Component, ElementRef, Inject, OnInit, Renderer2, ViewChild} from '@angular/core';
import {ProjectService} from '../../service/project.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {UIActiveDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {ProjectModel} from '../../model/project.model';
import {DateUtils} from '../../../../shared/utils/date.utils';

@Component({
	selector: `project-view-edit-component`,
	templateUrl: 'project-view-edit.component.html',
})
export class ProjectViewEditComponent implements OnInit {
	public projectModel: ProjectModel = null;
	public savedModel: ProjectModel = null;
	public orderNums = Array(25).fill(0).map((x, i) => i + 1);
	public managers;
	public rooms;
	public workflowCodes;
	public isDefaultProject;
	public sourceRoom;
	public targetRoom;
	public projectManager;
	public moveManager;
	public projectId;
	public canEditProject;
	public editing = false;
	protected userTimeZone: string;
	@ViewChild('startTimePicker') startTimePicker;
	@ViewChild('completionTimePicker') completionTimePicker;
	constructor(
		private projectService: ProjectService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private promptService: UIPromptService,
		private activeDialog: UIActiveDialogService,
		@Inject('id') private id) {
		this.canEditProject = this.permissionService.hasPermission('ProjectEdit');
		this.projectId = this.id;
	}

	ngOnInit() {
		this.projectModel = new ProjectModel();
		const defaultProject = {
			name: '',
			description: '',
			fromId: 0,
			toId: 0,
			startTime: '',
			completionTime: '',
			projectManagerId: 0,
			moveManagerId: 0,
			operationalOrder: 1,
			workflowCode: 'STD_PROCESS',
			useForPlanning: false,
		};
		this.userTimeZone = this.preferenceService.getUserTimeZone();
		this.projectModel = Object.assign({}, defaultProject, this.projectModel);
		this.getModel(this.projectId);
	}

	public confirmDeleteProject() {
		this.promptService.open(
			'Confirmation Required',
			'WARNING: Deleting this project will remove any teams and any related step data',
			'Confirm', 'Cancel')
			.then(confirm => {
				if (confirm) {
					this.deleteProject();
				}
			})
			.catch((error) => console.log(error));
	}

	public confirmDeleteProjectAndAssets() {
		this.promptService.open(
			'Confirmation Required',
			'WARNING: Deleting this project will remove any teams, any related step data, AND ASSIGNED ASSETS (NO UNDO)',
			'Confirm', 'Cancel')
			.then(confirm => {
				if (confirm) {
					this.deleteProjectAndAssets();
				}
			})
			.catch((error) => console.log(error));
	}

	private deleteProject() {
		this.projectService.deleteProject(this.projectId)
			.subscribe((result) => {
				if (result.status === 'success') {
					this.activeDialog.close(result);
				}
			});
	}

	private deleteProjectAndAssets() {
		this.projectService.deleteProjectAndAssets(this.projectId)
			.subscribe((result) => {
				if (result.status === 'success') {
					this.activeDialog.close(result);
				}
			});
	}

	public switchToEdit() {
		this.editing = true;
		if (this.projectModel.startTime) {
			this.startTimePicker.dateValue = this.formatForDateTimePicker(this.projectModel.startTime);
		}
		if (this.projectModel.completionTime) {
			this.completionTimePicker.dateValue = this.formatForDateTimePicker(this.projectModel.completionTime);
		}
	}

	private getModel(id) {
		this.projectService.getModelForProjectViewEdit(id)
			.subscribe((result) => {
				let data = result.data;
				let projectModel = this.projectModel;
				// Fill the model based on the current person.
				Object.keys(data.moveProjectInstance).forEach((key) => {
					if (key in projectModel && data.moveProjectInstance[key]) {
						projectModel[key] = data.moveProjectInstance[key];
					}
				});
				this.projectModel = projectModel;

				this.projectModel.projectManagerId = data.projectManager ? data.projectManager : 0;
				this.projectModel.moveManagerId = data.moveManager ? data.moveManager : 0;
				this.projectModel.fromId = data.moveProjectInstance.sourceRoom ? data.moveProjectInstance.sourceRoom.id : 0;
				this.projectModel.toId = data.moveProjectInstance.targetRoom ? data.moveProjectInstance.targetRoom.id : 0;

				this.managers = data.managers;
				this.managers = data.managers.filter((item, index) => index === 0 || item.name !== data.managers[index - 1].name); // Filter duplicate names
				this.workflowCodes = data.workflowCodes;
				this.rooms = data.rooms;

				this.updateSavedFields();
			});
	}

	private updateSavedFields() {
		this.savedModel = JSON.parse(JSON.stringify(this.projectModel));
		this.rooms.forEach((room) => {
			if (room.id === this.savedModel.fromId) {
				this.sourceRoom = room.roomName;
			}
			if (room.id === this.savedModel.toId) {
				this.targetRoom = room.roomName;
			}
		});
		this.managers.forEach((manager) => {
			if (manager.staff.id === this.savedModel.projectManagerId) {
				this.projectManager = manager.name;
			}
			if (manager.staff.id === this.savedModel.moveManagerId) {
				this.moveManager = manager.name;
			}
		});
	}

	public saveForm() {
		this.projectService.saveProject(this.projectModel, this.projectId).subscribe((result: any) => {
			if (result.status === 'success') {
				this.updateSavedFields();
				this.editing = false;
			}
		});
	}

	/**
	 *  Put date in format to be accepted in a dateTimePicker
	 */
	public formatForDateTimePicker (time) {
		let localDateFormatted = DateUtils.convertFromGMT(time, this.userTimeZone);
		return time ? DateUtils.toDateUsingFormat(localDateFormatted, DateUtils.SERVER_FORMAT_DATETIME) : null;
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (JSON.stringify(this.projectModel) !== JSON.stringify(this.savedModel)) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
				.then(confirm => {
					if (confirm) {
						this.activeDialog.close();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.activeDialog.close();
		}
	}

	public cancelEdit(): void {
		if (JSON.stringify(this.projectModel) !== JSON.stringify(this.savedModel)) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
				.then(confirm => {
					if (confirm) {
						this.editing = false;
						this.projectModel = JSON.parse(JSON.stringify(this.savedModel));
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.editing = false;
		}
	}
}