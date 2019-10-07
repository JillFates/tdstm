import {Component, OnInit, ViewChild} from '@angular/core';
import {ProjectService} from '../../service/project.service';
import {ProjectModel} from '../../model/project.model';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {UserDateTimezoneComponent} from '../../../../shared/modules/header/components/date-timezone/user-date-timezone.component';
import {ASSET_IMPORT_FILE_UPLOAD_TYPE, FILE_UPLOAD_TYPE_PARAM} from '../../../../shared/model/constants';
import {RemoveEvent, SuccessEvent, UploadEvent} from '@progress/kendo-angular-upload';
import {KendoFileUploadBasicConfig} from '../../../../shared/providers/kendo-file-upload.interceptor';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {DateUtils} from '../../../../shared/utils/date.utils';

@Component({
	selector: `project-create`,
	templateUrl: 'project-create.component.html',
})
export class ProjectCreateComponent implements OnInit {
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

	@ViewChild('startDatePicker') startDatePicker;
	@ViewChild('completionDatePicker') completionDatePicker;
	constructor(
		private projectService: ProjectService,
		private promptService: UIPromptService,
		private activeDialog: UIActiveDialogService,
		private dialogService: UIDialogService) {
	}

	ngOnInit() {
		this.getModel();
		this.projectModel = new ProjectModel();
		this.defaultModel = {
			clientId: 0,
			projectName: '',
			description: '',
			startDate: new Date(),
			completionDate: new Date(),
			partnerIds: [],
			projectLogo: '',
			projectManagerId: 0,
			projectCode: '',
			projectType: 'Standard',
			comment: '',
			defaultBundleName: 'TBD',
			timeZone: '',
			collectMetrics: true,
			planMethodology: ''
		};
		this.projectModel = Object.assign({}, this.defaultModel, this.projectModel);
		this.projectModel.completionDate.setMonth(this.projectModel.completionDate.getMonth()+2);
		this.file.uploadRestrictions = {
			allowedExtensions: ['.jpg', '.png', '.gif'],
			maxFileSize: 50000
		};
		this.file.uploadSaveUrl = '../ws/fileSystem/uploadImageFile'
	}

	private getModel() {
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

	openTimezoneModal() {
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

	// This is a work-around for firefox users
	onOpenStartDatePicker (event) {
		event.preventDefault();
		this.startDatePicker.toggle();
	}

	onOpenCompletionDatePicker (event) {
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

	public completeEventHandler(e: SuccessEvent) {
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

	private clearFilename(e?: any) {
		this.fetchResult = null;
	}

	public saveForm() {
		if (DateUtils.validateDateRange(this.projectModel.startDate, this.projectModel.completionDate) && this.validateRequiredFields(this.projectModel)) {
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
					this.activeDialog.close();
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

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (JSON.stringify(this.projectModel) !== JSON.stringify(this.defaultModel)) {
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
}
