import {
	Component,
	Inject,
	OnInit,
	ViewChild
} from '@angular/core';
import {ProjectService} from '../../service/project.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {ProjectModel} from '../../model/project.model';
import {KendoFileUploadBasicConfig} from '../../../../shared/providers/kendo-file-upload.interceptor';
import {UserDateTimezoneComponent} from '../../../../shared/modules/header/components/date-timezone/user-date-timezone.component';
import {RemoveEvent, SuccessEvent, UploadEvent} from '@progress/kendo-angular-upload';
import {ASSET_IMPORT_FILE_UPLOAD_TYPE, FILE_UPLOAD_TYPE_PARAM} from '../../../../shared/model/constants';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {Store} from '@ngxs/store';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {SetProject} from '../../actions/project.actions';
import {DateUtils} from '../../../../shared/utils/date.utils';

@Component({
	selector: `project-view-edit-component`,
	templateUrl: 'project-view-edit.component.html',
})
export class ProjectViewEditComponent implements OnInit {
	public projectModel: ProjectModel = null;
	public savedModel: ProjectModel = null;
	private requiredFields = ['clientId', 'projectCode', 'projectName', 'completionDate'];
	public managers;
	public client;
	public clients;
	public planMethodologies;
	public projectTypes;
	public projectManagers;
	public possiblePartners;
	public possibleManagers;
	public availableBundles;
	public projectId;
	public projectLogoId;
	public savedProjectLogoId;
	public projectGUID;
	public dateCreated;
	public lastUpdated;
	public canEditProject;
	public editing = false;
	protected userTimeZone: string;
	protected userDateFormat: string;
	public file = new KendoFileUploadBasicConfig();
	public fetchResult: any;
	public transformResult: ApiResponseModel;
	public transformInProcess = false;
	private logoOriginalFilename;
	public retrieveImageTimestamp = (new Date()).getTime(); // Update this to refresh the project logo

	@ViewChild('startDatePicker', {static: false}) startDatePicker;
	@ViewChild('completionDatePicker', {static: false}) completionDatePicker;
	constructor(
		private dialogService: UIDialogService,
		private projectService: ProjectService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private promptService: UIPromptService,
		private activeDialog: UIActiveDialogService,
		private store: Store,
		@Inject('id') private id) {
		this.projectId = this.id;
	}

	ngOnInit() {
		this.preferenceService.getUserDatePreferenceAsKendoFormat()
			.subscribe(() => {
				this.userDateFormat = this.preferenceService.getUserDateFormat().toUpperCase();
				this.userTimeZone = this.preferenceService.getUserTimeZone();
				this.projectModel = new ProjectModel();
				let defaultProject = {
					clientId: 0,
					projectName: '',
					description: '',
					startDate: new Date(),
					completionDate: new Date(),
					partners: [],
					projectLogo: '',
					projectManagerId: 0,
					projectCode: '',
					projectType: 'Standard',
					comment: '',
					defaultBundleName: 'TBD',
					timeZone: '',
					collectMetrics: 1,
					planMethodology: {field: '', label: 'Select...'}
				};
				this.projectModel = Object.assign({}, defaultProject, this.projectModel);
				this.file.uploadRestrictions = {
					allowedExtensions: ['.jpg', '.png', '.gif'],
					maxFileSize: 50000
				};
				this.file.uploadSaveUrl = '../ws/fileSystem/uploadImageFile'
				this.getModel(this.projectId);
				this.canEditProject = this.permissionService.hasPermission('ProjectEdit');
			});
	}

	// This is a work-around for firefox users
	onOpenStartDatePicker (event): void {
		event.preventDefault();
		this.startDatePicker.toggle();
	}

	onOpenCompletionDatePicker (event): void {
		event.preventDefault();
		this.completionDatePicker.toggle();
	}

	public confirmDeleteProject(): void {
		this.promptService.open(
			'Confirmation Required',
			'WARNING: Are you sure you want to delete this project? This cannot be undone.', 'Confirm', 'Cancel')
			.then(confirm => {
				if (confirm) {
					this.deleteProject();
				}
			})
			.catch((error) => console.log(error));
	}

	private deleteProject(): void {
		this.projectService.deleteProject(this.projectId)
			.subscribe((result) => {
				if (result.status === 'success') {
					this.activeDialog.close(result);
				}
			});
	}

	public switchToEdit(): void {
		this.editing = true;
	}

	private getModel(id): void {
		this.projectService.getModelForProjectViewEdit(id)
			.subscribe((result) => {
				let data = result.data;
				let projectModel = this.projectModel;
				// Fill the model based on the current person.
				Object.keys(data.projectInstance).forEach((key) => {
					if (key in projectModel && data.projectInstance[key] !== null) {
						projectModel[key] = data.projectInstance[key];
					}
				});
				this.projectModel = projectModel;
				this.possiblePartners = data.possiblePartners;

				data.projectPartners.forEach((partner) => {
					this.projectModel.partners.push({id: partner.id, name: partner.name});
				});

				this.planMethodologies = data.planMethodologies ? data.planMethodologies : [];
				this.possibleManagers = data.possibleManagers ? data.possibleManagers : [];
				this.projectManagers = data.projectManagers ? data.projectManagers : [];
				this.clients = data.clients ? data.clients : [];
				this.client = data.client;
				this.projectLogoId = data.projectLogoForProject ? data.projectLogoForProject.id : 0;
				this.savedProjectLogoId = this.projectLogoId;
				this.projectModel.clientId = data.client ? data.client.id : 0;
				this.projectModel.startDate = this.projectModel.startDate ? DateUtils.adjustDateTimezoneOffset(new Date(this.projectModel.startDate)) : null
				if (this.projectModel.startDate) {
					this.projectModel.startDate.setHours(0, 0, 0, 0);
				}
				this.projectModel.completionDate = this.projectModel.completionDate ? DateUtils.adjustDateTimezoneOffset(new Date(this.projectModel.completionDate)) : null;
				if (this.projectModel.completionDate) {
					this.projectModel.completionDate.setHours(0, 0, 0, 0);
				}
				let methodologyField = data.projectInstance ? data.projectInstance.planMethodology : '';
				this.planMethodologies.forEach((methodology) => {
					if (methodology.field === methodologyField) {
						this.projectModel.planMethodology = methodology;
					}
				});
				this.projectGUID = data.projectInstance ? data.projectInstance.guid : '';
				this.dateCreated = data.projectInstance ? data.projectInstance.dateCreated : '';
				this.lastUpdated = data.projectInstance ? data.projectInstance.lastUpdated : '';
				this.availableBundles = data.availableBundles;
				this.projectModel.defaultBundle = data.defaultBundle ? data.defaultBundle : {};
				this.projectModel.projectLogo = data.projectLogoForProject;
				this.projectModel.projectName = data.projectInstance ? data.projectInstance.name : '';
				this.projectModel.timeZone = data.timezone;
				this.projectTypes = data.projectTypes;

				this.store.dispatch(new SetProject({id: this.projectId, name: this.projectModel.projectName, logoUrl: this.projectLogoId ? '/tdstm/project/showImage/' + this.projectLogoId : ''}));
				this.updateSavedFields();
			});
	}

	private updateSavedFields(): void {
		this.savedModel = JSON.parse(JSON.stringify(this.projectModel));

		this.clients.forEach((client) => {
			if (client.id === this.savedModel.clientId) {
				this.client = client.clientName;
			}
		});
	}

	public saveForm(): void {
		if (DateUtils.validateDateRange(this.projectModel.startDate, this.projectModel.completionDate) && this.validateRequiredFields(this.projectModel)
			&& this.validatePartners(this.projectModel.partners)) {
			if (this.projectModel.startDate.getHours() > 0 || this.projectModel.completionDate.getHours() > 0) {
				this.projectModel.startDate.setHours(0, 0, 0, 0);
				this.projectModel.completionDate.setHours(0, 0, 0, 0);
				this.projectModel.startDate.setMinutes(this.projectModel.startDate.getMinutes() - this.projectModel.startDate.getTimezoneOffset());
				this.projectModel.completionDate.setMinutes(this.projectModel.completionDate.getMinutes() - this.projectModel.completionDate.getTimezoneOffset());
			}
			if (this.projectModel.projectLogo && this.projectModel.projectLogo.name) {
				this.projectModel.projectLogo = this.projectModel.projectLogo.name;
			}
			this.projectService.saveProject(this.projectModel, this.logoOriginalFilename, this.projectId).subscribe((result: any) => {
				if (result.status === 'success') {
					this.updateSavedFields();
					this.editing = false;
					this.projectLogoId = result.data.projectLogoForProject ? result.data.projectLogoForProject.id : 0;
					this.savedProjectLogoId = this.projectLogoId;
					this.retrieveImageTimestamp = (new Date()).getTime();

					this.store.dispatch(new SetProject({id: this.projectId, name: this.projectModel.projectName, logoUrl:  this.projectLogoId ? '/tdstm/project/showImage/' + this.projectLogoId + '?' + this.retrieveImageTimestamp : ''}));
				}
			});
		}
	}

	/**
	 * Validates that there are no duplicate partners and no blank partners in the partner list
	 * @param partnerList - The list of partners from the project model
	 */
	public validatePartners(partnerList: any[]): boolean {
		let partners = [...partnerList];
		partners.sort((a, b) => (a.id > b.id) ? 1 : -1);
		let i = 1;
		for (i = 0; i < partners.length; i++) {
			if (!partners[i].id) {
				alert('Partner cannot be blank.');
				return false;
			}
			if (i !== partners.length - 1 && partners[i].id === partners[i + 1].id) {
				alert('Duplicate partners are not allowed.');
				return false;
			}
		}
		return true;
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

	openTimezoneModal(): void {
		this.dialogService.extra(UserDateTimezoneComponent, [{
			provide: Boolean,
			useValue: true
		}, {
			provide: String,
			useValue: this.projectModel.timeZone
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
		e.headers.set('Content-Type', 'multipart/form-data')
		e.data = {};
		e.data[FILE_UPLOAD_TYPE_PARAM] = ASSET_IMPORT_FILE_UPLOAD_TYPE;
		this.clearFilename();
	}

	public completeEventHandler(e: SuccessEvent): void {
		let response = e.response.body.data;
		if (response && response.operation === 'delete') { // file deleted successfully
			this.clearFilename();
			this.file.fileUID = null;
		} else if (e.files[0]) { // file uploaded successfully
			let filename = response.filename;
			this.fetchResult = { status: 'success', filename: filename };
			this.projectModel.projectLogo = response.filename;

			this.logoOriginalFilename = response.originalFilename;
		} else {
			this.clearFilename();
			this.fetchResult = { status: 'error' };
		}
	}

	private clearFilename(e?: any): void {
		this.fetchResult = null;
	}

	/**
	 * Handling for partner selection
	 * @param partner - The partner to modify
	 * @param selection - The event (Object that will be used to modify the selected partner)
	 */
	public onPartnerSelectionChange(partner: any, selection: any): void {
		partner.id = selection.id;
		partner.name = selection.name;
	}

	public onDeleteLogo(): void {
		this.projectLogoId = 0;
		this.projectModel.projectLogo = null;
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
						this.projectLogoId = this.savedProjectLogoId;
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.editing = false;
		}
	}
}
