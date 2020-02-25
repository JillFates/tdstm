// Angular
import {
	Component, ComponentFactoryResolver,
	Input,
	OnInit,
	ViewChild
} from '@angular/core';
import {Router} from '@angular/router';
// Component
import {UserDateTimezoneComponent} from '../../../../shared/modules/header/components/date-timezone/user-date-timezone.component';
// Model
import {ProjectModel} from '../../model/project.model';
import {ASSET_IMPORT_FILE_UPLOAD_TYPE, FILE_UPLOAD_TYPE_PARAM} from '../../../../shared/model/constants';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {Store} from '@ngxs/store';
import {SetProject} from '../../actions/project.actions';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {
	Dialog,
	DialogButtonType,
	DialogConfirmAction,
	DialogExit,
	DialogService,
	ModalSize
} from 'tds-component-library';
import {ActionType} from '../../../dataScript/model/data-script.model';
// Service
import {ProjectService} from '../../service/project.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {KendoFileUploadBasicConfig} from '../../../../shared/providers/kendo-file-upload.interceptor';
// Others
import {RemoveEvent, SuccessEvent, UploadEvent} from '@progress/kendo-angular-upload';
import * as R from 'ramda';

@Component({
	selector: `project-view-edit-component`,
	templateUrl: 'project-view-edit.component.html',
})
export class ProjectViewEditComponent extends Dialog implements OnInit {
	@Input() data: any;

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
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private router: Router,
		private projectService: ProjectService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private store: Store) {
		super();
	}

	ngOnInit() {
		this.projectId = R.clone(this.data.projectModelId);
		this.editing = (this.data.actionType === ActionType.EDIT);

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			show: () => this.canEditProject,
			active: () => this.editing,
			type: DialogButtonType.ACTION,
			action: this.switchToEdit.bind(this)
		});

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => this.editing,
			disabled: () => !this.validateRequiredFields(this.projectModel),
			type: DialogButtonType.ACTION,
			action: this.saveForm.bind(this)
		});

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			show: () => this.canEditProject,
			type: DialogButtonType.ACTION,
			action: this.confirmDeleteProject.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			show: () => !this.editing,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			show: () => this.editing,
			type: DialogButtonType.ACTION,
			action: this.cancelEdit.bind(this)
		});

		this.buttons.push({
			name: 'fieldSettings',
			text: 'Field Settings',
			show: () => !this.editing,
			type: DialogButtonType.CONTEXT,
			action: this.openFieldSettings.bind(this)
		});

		this.buttons.push({
			name: 'planningDashboard',
			text: 'Planning Dashboard',
			show: () => !this.editing,
			type: DialogButtonType.CONTEXT,
			action: this.openPlanningDashboard.bind(this)
		});

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

		setTimeout(() => {
			this.setTitle(this.getModalTitle());
		});

	}

	/**
	 * Open the Field Setting Pages
	 */
	public openFieldSettings(): void {
		super.onCancelClose();
		this.router.navigate(['/fieldsettings/list']);
	}

	/**
	 * Open Planning Dashboard
	 */
	public openPlanningDashboard(): void {
		super.onCancelClose();
		this.router.navigate(['/planning/dashboard']);
	}

	// This is a work-around for firefox users
	onOpenStartDatePicker(event): void {
		event.preventDefault();
		this.startDatePicker.toggle();
	}

	onOpenCompletionDatePicker(event): void {
		event.preventDefault();
		this.completionDatePicker.toggle();
	}

	public confirmDeleteProject(): void {
		this.dialogService.confirm(
			'Confirmation Required',
			'WARNING: Are you sure you want to delete this project? This cannot be undone.'
		)
			.subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					this.deleteProject();
				}
			})
	}

	private deleteProject(): void {
		this.projectService.deleteProject(this.projectId)
			.subscribe((result) => {
				if (result.status === 'success') {
					super.onCancelClose(result);
				}
			});
	}

	public switchToEdit(): void {
		this.editing = true;
		this.setTitle(this.getModalTitle());
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

				this.store.dispatch(new SetProject({
					id: this.projectId,
					name: this.projectModel.projectName,
					logoUrl: this.projectLogoId ? '/tdstm/project/showImage/' + this.projectLogoId : ''
				}));
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

					this.store.dispatch(new SetProject({
						id: this.projectId,
						name: this.projectModel.projectName,
						logoUrl: this.projectLogoId ? '/tdstm/project/showImage/' + this.projectLogoId + '?' + this.retrieveImageTimestamp : ''
					}));
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
		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: UserDateTimezoneComponent,
			data: {
				shouldReturnData: true,
				defaultTimeZone: this.projectModel.timeZone
			},
			modalConfiguration: {
				title: 'Time Zone Select',
				draggable: true,
				modalCustomClass: 'custom-time-zone-dialog',
				modalSize: ModalSize.CUSTOM
			}
		}).subscribe((data) => {
			if (data.status === DialogExit.ACCEPT) {
				this.projectModel.timeZone = data.timezone;
			}
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
		const tempServerFilesToDelete = [this.fetchResult.filename];

		// delete temporary transformed file
		if (this.transformResult) {
			tempServerFilesToDelete.push(this.transformResult.data.filename)
		}

		// get the coma separated file names to delete
		e.data = {filename: tempServerFilesToDelete.join(',')};

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
			this.fetchResult = {status: 'success', filename: filename};
			this.projectModel.projectLogo = response.filename;

			this.logoOriginalFilename = response.originalFilename;
		} else {
			this.clearFilename();
			this.fetchResult = {status: 'error'};
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
			this.dialogService.confirm(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?'
			)
				.subscribe((data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM) {
						super.onCancelClose();
					}
				});
		} else {
			super.onCancelClose();
		}
	}

	public cancelEdit(): void {
		if (JSON.stringify(this.projectModel) !== JSON.stringify(this.savedModel)) {
			this.dialogService.confirm(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?'
			)
				.subscribe((data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM && !this.data.openFromList) {
						this.editing = false;
						this.projectModel = JSON.parse(JSON.stringify(this.savedModel));
						this.projectLogoId = this.savedProjectLogoId;
						this.setTitle(this.getModalTitle());
					}
				});
		} else {
			this.editing = false;
			if (!this.data.openFromList) {
				this.setTitle(this.getModalTitle());
			} else {
				this.onCancelClose();
			}
		}
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}

	/**
	 * Based on modalType action returns the corresponding title
	 * @returns {string}
	 */
	private getModalTitle(): string {
		// Every time we change the title, it means we switched to View, Edit or Create
		setTimeout(() => {
			// This ensure the UI has loaded since Kendo can change the signature of an object
			// this.dataSignature = JSON.stringify(this.credentialModel);
		}, 800);

		if (this.editing) {
			return 'Project Edit';
		}
		return 'Project Detail';
	}
}
