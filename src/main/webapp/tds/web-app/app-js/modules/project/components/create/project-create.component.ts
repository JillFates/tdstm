import {Component, ElementRef, OnInit, Renderer2} from '@angular/core';
import {ProjectService} from '../../service/project.service';
import {ProjectModel} from '../../model/project.model';
import {Router} from '@angular/router';
import {UIActiveDialogService, UIDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {UserDateTimezoneComponent} from '../../../../shared/modules/header/components/date-timezone/user-date-timezone.component';
import {ASSET_IMPORT_FILE_UPLOAD_TYPE, DIALOG_SIZE, FILE_UPLOAD_TYPE_PARAM} from '../../../../shared/model/constants';
import {RemoveEvent, SuccessEvent, UploadEvent} from '@progress/kendo-angular-upload';
import {KendoFileUploadBasicConfig} from '../../../../shared/providers/kendo-file-upload.interceptor';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';

@Component({
	selector: `project-create`,
	templateUrl: 'project-create.component.html',
})
export class ProjectCreateComponent implements OnInit {
	public managers;
	public workflowCodes;
	public planMethodologies;
	public clients;
	public partners;
	public projectTypes;
	public orderNums = Array(25).fill(0).map((x, i) => i + 1);
	public projectModel: ProjectModel = null;
	private defaultModel = null;
	public file = new KendoFileUploadBasicConfig();
	public fetchFileContent: any;
	public transformFileContent: any;
	public fetchResult: any;
	public fetchInProcess = false;
	public fetchInputUsed: 'action' | 'file' = 'action';
	public transformResult: ApiResponseModel;
	public transformInProcess = false;

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
			workflowCode: 'STD_PROCESS',
			projectCode: '',
			projectType: 'Standard',
			comment: '',
			defaultBundle: 'TBD',
			timeZone: '',
			collectReportingMetrics: true,
			planMethodology: 'Migration Method'
		};
		this.projectModel = Object.assign({}, this.defaultModel, this.projectModel);
		this.file.uploadRestrictions = {
			allowedExtensions: ['.jpg', '.png', '.gif'],
			maxFileSize: 5000000
		};
	}

	private getModel() {
		this.projectService.getModelForProjectCreate().subscribe((result: any) => {
			let data = result.data;
			this.managers = data.managers;
			this.managers = data.managers.filter((item, index) => index === 0 || item.name !== data.managers[index - 1].name); // Filter duplicate names
			this.workflowCodes = data.workflowCodes;
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
		this.fetchFileContent = null;
		this.transformResult = null;
		this.transformFileContent = null;
	}

	public onUploadFile(e: UploadEvent): void {
		e.data = {};
		e.data[FILE_UPLOAD_TYPE_PARAM] = ASSET_IMPORT_FILE_UPLOAD_TYPE;
		this.clearFilename();
	}

	public completeEventHandler(e: SuccessEvent) {
		let response = e.response.body.data;
		if (response && response.operation === 'delete') { // file deleted successfully
			// console.log(response.data);
			this.clearFilename();
			this.file.fileUID = null;
		} else if (e.files[0]) { // file uploaded successfully
			let filename = e.files[0].name;
			this.fetchResult = { status: 'success', filename: filename };
			this.fetchInputUsed = 'file';
		} else {
			this.clearFilename();
			this.fetchResult = { status: 'error' };
		}
	}

	private clearFilename(e?: any) {
		this.fetchResult = null;
		this.fetchFileContent = null;
	}

	public saveForm() {
		if (this.fetchResult && this.fetchResult.status === 'success') {
			this.projectModel.projectLogo = this.fetchResult.filename;
		}
		this.projectService.saveProject(this.projectModel).subscribe((result: any) => {
			if (result.status === 'success') {
				this.activeDialog.close();
			}
		});
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