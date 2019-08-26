import {Component, ElementRef, Inject, OnInit, Renderer2, ViewChild} from '@angular/core';
import {ProjectService} from '../../service/project.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {UIActiveDialogService, UIDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {ProjectModel} from '../../model/project.model';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {KendoFileUploadBasicConfig} from '../../../../shared/providers/kendo-file-upload.interceptor';
import {UserDateTimezoneComponent} from '../../../../shared/modules/header/components/date-timezone/user-date-timezone.component';
import {RemoveEvent, SuccessEvent, UploadEvent} from '@progress/kendo-angular-upload';
import {ASSET_IMPORT_FILE_UPLOAD_TYPE, FILE_UPLOAD_TYPE_PARAM} from '../../../../shared/model/constants';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {DialogService} from '@progress/kendo-angular-dialog';

@Component({
	selector: `project-view-edit-component`,
	templateUrl: 'project-view-edit.component.html',
})
export class ProjectViewEditComponent implements OnInit {
	public projectModel: ProjectModel = null;
	public savedModel: ProjectModel = null;
	private requiredFields = ['clientId', 'projectCode', 'projectName', 'workflowCode', 'completionDate'];
	public managers;
	public client;
	public clients;
	public planMethodologies;
	public projectTypes;
	public workflowCodes;
	public projectManagers;
	public possiblePartners;
	public possibleManagers;
	public partnerKey = {};
	public projectId;
	public projectLogoId;
	public projectGUID;
	public dateCreated;
	public lastUpdated;
	public canEditProject;
	public editing = false;
	protected userTimeZone: string;
	public file = new KendoFileUploadBasicConfig();
	public fetchResult: any;
	public transformResult: ApiResponseModel;
	public transformInProcess = false;

	@ViewChild('startTimePicker') startTimePicker;
	@ViewChild('completionTimePicker') completionTimePicker;
	constructor(
		private dialogService: UIDialogService,
		private projectService: ProjectService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private promptService: UIPromptService,
		private activeDialog: UIActiveDialogService,
		@Inject('id') private id) {
		this.projectId = this.id;
	}

	ngOnInit() {
		this.projectModel = new ProjectModel();
		let defaultProject = {
			clientId: 0,
			projectName: '',
			description: '',
			startDate: new Date(),
			completionDate: new Date(),
			partnerIds: [],
			projectLogo: '',
			projectManagerId: 0,
			workflowCode: 'STD_PROCESS',
			projectCode: '',
			projectType: 'Standard',
			comment: '',
			defaultBundle: 'TBD',
			timeZone: '',
			collectReportingMetrics: true,
			planMethodology: 'Migration Method'
		};
		this.userTimeZone = this.preferenceService.getUserTimeZone();
		this.projectModel = Object.assign({}, defaultProject, this.projectModel);
		this.getModel(this.projectId);
		this.canEditProject = this.permissionService.hasPermission('ProjectEdit');
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

	private deleteProject() {
		this.projectService.deleteProject(this.projectId)
			.subscribe((result) => {
				if (result.status === 'success') {
					this.activeDialog.close(result);
				}
			});
	}

	public switchToEdit() {
		this.editing = true;
	}

	private getModel(id) {
		this.projectService.getModelForProjectViewEdit(id)
			.subscribe((result) => {
				let data = result.data;
				let projectModel = this.projectModel;
				// Fill the model based on the current person.
				Object.keys(data.projectInstance).forEach((key) => {
					if (key in projectModel && data.projectInstance[key]) {
						projectModel[key] = data.projectInstance[key];
					}
				});
				this.projectModel = projectModel;
				this.possiblePartners = data.possiblePartners;
				data.possiblePartners.forEach((partner) => {
					this.partnerKey[partner.id] = partner.name;
				});

				data.projectPartners.forEach((partner) => {
					this.projectModel.partnerIds.push(partner.id);
				});

				this.possibleManagers = data.possibleManagers ? data.possibleManagers : [];
				this.projectManagers = data.projectManagers ? data.projectManagers : [];
				this.clients = data.clients ? data.clients : [];
				this.client = data.client;
				this.projectLogoId = data.projectLogoForProject ? data.projectLogoForProject.id : 0;
				this.projectModel.clientId = data.client ? data.client.id : 0;
				this.projectModel.startDate = new Date(this.projectModel.startDate);
				this.projectModel.completionDate = new Date(this.projectModel.completionDate);
				this.projectGUID = data.projectInstance ? data.projectInstance.guid : '';
				this.dateCreated = data.projectInstance ? data.projectInstance.dateCreated : '';
				this.lastUpdated = data.projectInstance ? data.projectInstance.lastUpdated : '';
				this.projectModel.defaultBundle = data.defaultBundle ? data.defaultBundle.name : '';
				this.projectModel.projectLogo = data.projectLogoForProject;
				this.projectModel.projectName = data.projectInstance ? data.projectInstance.name : '';
				this.projectModel.timeZone = data.timezone;
				this.workflowCodes = data.workflowCodes;
				this.projectTypes = data.projectTypes;
				this.planMethodologies = data.planMethodologies;

				this.updateSavedFields();
			});
	}

	private updateSavedFields() {
		this.savedModel = JSON.parse(JSON.stringify(this.projectModel));

		this.clients.forEach((client) => {
			if (client.id === this.savedModel.clientId) {
				this.client = client.clientName;
			}
		});
	}

	public saveForm() {
		if (this.validateRequiredFields(this.projectModel)) {
			this.projectService.saveProject(this.projectModel, this.projectId).subscribe((result: any) => {
				if (result.status === 'success') {
					this.updateSavedFields();
					this.editing = false;
				}
			});
		}
	}

	/**
	 * Validate required fields before saving model
	 * @param model - The model to be saved
	 */
	public validateRequiredFields(model: ProjectModel): boolean {
		let returnVal = true;
		this.requiredFields.forEach((field) => {
			if (!model[field]) {
				returnVal = false;
				return false;
			} else if (typeof model[field] === 'string' && !model[field].replace(/\s/g, '').length) {
				returnVal = false;
				return false;
			}
		});
		return returnVal;
	}

	openTimezoneModal() {
		this.dialogService.extra(UserDateTimezoneComponent, [{
			provide: Boolean,
			useValue: true
		}]).then(result => {
			this.projectModel.timeZone = result.timezone;
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}
	public onSelectFile(e?: any): void {
		this.file.fileUID = e.files[0].uid;
	}

	public onRemoveFile(e: RemoveEvent): void {
		if (!this.fetchResult || !this.fetchResult.filename) {
			return;
		}
		// delete temporary server uploaded file
		const tempServerFilesToDelete = [ this.fetchResult.filename ];

		// delete temporary transformed file
		if (this.transformResult) {
			tempServerFilesToDelete.push(this.transformResult.data.filename)
		}

		// get the coma separated file names to delete
		e.data = { filename: tempServerFilesToDelete.join(',') };

		this.fetchResult = null;
		this.transformResult = null;
	}

	public onUploadFile(e: UploadEvent): void {
		e.data = {};
		e.data[FILE_UPLOAD_TYPE_PARAM] = ASSET_IMPORT_FILE_UPLOAD_TYPE;
		this.clearFilename();
	}

	public completeEventHandler(e: SuccessEvent) {
		let response = e.response.body.data;
		if (response && response.operation === 'delete') { // file deleted successfully
			this.clearFilename();
			this.file.fileUID = null;
		} else if (e.files[0]) { // file uploaded successfully
			let filename = e.files[0].name;
			this.fetchResult = { status: 'success', filename: filename };
			this.projectModel.projectLogo = e.files[0].rawFile;
		} else {
			this.clearFilename();
			this.fetchResult = { status: 'error' };
		}
	}

	private clearFilename(e?: any) {
		this.fetchResult = null;
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