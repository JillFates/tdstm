// Angular
import {Component, ComponentFactoryResolver, OnInit, ViewChild, ElementRef} from '@angular/core';
// Modal
import {ProjectModel} from '../../model/project.model';
import {
	Dialog,
	DialogButtonType,
	DialogConfirmAction,
	DialogExit,
	DialogService,
	ModalSize
} from 'tds-component-library';
import {ASSET_IMPORT_FILE_UPLOAD_TYPE, FILE_UPLOAD_TYPE_PARAM} from '../../../../shared/model/constants';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {KendoFileUploadBasicConfig} from '../../../../shared/providers/kendo-file-upload.interceptor';
// Component
import {UserDateTimezoneComponent} from '../../../../shared/modules/header/components/date-timezone/user-date-timezone.component';
// Service
import {ProjectService} from '../../service/project.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
// Other
import {RemoveEvent, SuccessEvent, UploadEvent} from '@progress/kendo-angular-upload';

@Component({
	selector: `project-create`,
	templateUrl: 'project-create.component.html',
})
export class ProjectCreateComponent extends Dialog implements OnInit {
	public managers;
	public planMethodologies;
	public clients;
	public partners;
	public projectTypes;
	public projectModel: ProjectModel = null;
	private requiredFields = ['clientId', 'projectCode', 'projectName', 'completionDate'];
	private defaultModel = null;
	private logoOriginalFilename;
	public file = new KendoFileUploadBasicConfig();
	public fetchResult: any;
	public transformResult: ApiResponseModel;
	public transformInProcess = false;
	@ViewChild('projectCodeInput', {static: false}) projectCodeInput: ElementRef;
	@ViewChild('startDatePicker', {static: false}) startDatePicker;
	@ViewChild('completionDatePicker', {static: false}) completionDatePicker;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private projectService: ProjectService,
		private dialogService: DialogService) {
		super();
	}

	ngOnInit() {

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			tooltipText: 'Save',
			show: () => true,
			disabled: () => !this.validateRequiredFields(this.projectModel),
			type: DialogButtonType.ACTION,
			action: this.saveForm.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			tooltipText: 'Cancel',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.getModel();

		let today = new Date();
		this.projectModel = new ProjectModel();
		this.defaultModel = {
			clientId: null,
			projectName: '',
			description: '',
			startDate: new Date(),
			completionDate: new Date(today.setMonth(today.getMonth() + 12)),
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
		this.projectModel = Object.assign({}, this.defaultModel, this.projectModel);
		this.file.uploadRestrictions = {
			allowedExtensions: ['.jpg', '.png', '.gif'],
			maxFileSize: 50000
		};
		this.file.uploadSaveUrl = '../ws/fileSystem/uploadImageFile'
		setTimeout(() => {
			this.onSetUpFocus(this.projectCodeInput);
		});
	}

	private getModel(): void {
		this.projectService.getModelForProjectCreate().subscribe((result: any) => {
			let data = result.data;
			this.managers = data.managers;
			this.managers = data.managers.filter((item, index) => index === 0 || item.name !== data.managers[index - 1].name); // Filter duplicate names
			this.clients = data.clients;
			this.partners = data.partners;
			this.projectTypes = data.projectTypes;
			this.planMethodologies = data.planMethodologies;
		});
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

	// This is a work-around for firefox users
	onOpenStartDatePicker(event) {
		event.preventDefault();
		this.startDatePicker.toggle();
	}

	onOpenCompletionDatePicker(event) {
		event.preventDefault();
		this.completionDatePicker.toggle();
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
	onPartnerSelectionChange(partner, selection): void {
		partner.id = selection.id;
		partner.name = selection.name;
	}

	public saveForm(): void {
		const validateDate = DateUtils.validateDateRange(this.projectModel.startDate, this.projectModel.completionDate) && this.validateRequiredFields(this.projectModel) && this.validatePartners(this.projectModel.partners);
		if (!validateDate) {
			this.dialogService.notify(
				'Validation Required',
				'The completion time must be later than the start time.'
			).subscribe();
		} else {
			if (this.projectModel.startDate) {
				this.projectModel.startDate.setHours(0, 0, 0, 0);
				this.projectModel.startDate.setMinutes(this.projectModel.startDate.getMinutes() - this.projectModel.startDate.getTimezoneOffset());
			}
			if (this.projectModel.completionDate) {
				this.projectModel.completionDate.setHours(0, 0, 0, 0);
				this.projectModel.completionDate.setMinutes(this.projectModel.completionDate.getMinutes() - this.projectModel.completionDate.getTimezoneOffset());
			}
			this.projectService.saveProject(this.projectModel, this.logoOriginalFilename).subscribe((result: any) => {
				if (result.status === 'success') {
					this.onAcceptSuccess();
					// get the project image logo
					const projectLogo = result.data && result.data.projectLogoForProject && result.data.projectLogoForProject.id || '';

					// notify about project updates
					this.projectService.updateProjectInfo(
						result.data.id,
						this.projectModel.projectName,
						this.projectService.getProjectImagePath(projectLogo)
					);
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

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (JSON.stringify(this.projectModel) !== JSON.stringify(this.defaultModel)) {
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

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}

}
