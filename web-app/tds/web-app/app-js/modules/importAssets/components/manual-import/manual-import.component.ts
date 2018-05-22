import {Component, OnInit, ViewChild} from '@angular/core';
import {ImportAssetsService} from '../../service/import-assets.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {RemoveEvent, SuccessEvent, UploadComponent} from '@progress/kendo-angular-upload';
import {KendoFileUploadBasicConfig} from '../../../../shared/providers/kendo-file-upload.interceptor';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {OperationStatusModel} from '../../../../shared/components/check-action/model/check-action.model';
import {DataIngestionService} from '../../../dataIngestion/service/data-ingestion.service';

@Component({
	selector: 'manual-import',
	templateUrl: '../tds/web-app/app-js/modules/importAssets/components/manual-import/manual-import.component.html'
})
export class ManualImportComponent implements OnInit {

	@ViewChild('kendoUploadInstance') kendoUploadInstance: UploadComponent;
	private file: KendoFileUploadBasicConfig = new KendoFileUploadBasicConfig();
	private actionOptions = [];
	private dataScriptOptions = [];
	private selectedActionOption = -1;
	private selectedScriptOption = -1;
	private fetchResult: any;
	private fetchInProcess = false;
	private fetchInputUsed: 'action' | 'file' = 'action';
	protected transformResult: ApiResponseModel;
	protected transformInProcess = false;
	private importResult: any;
	private importInProcess = false;
	private fetchFileContent: any;
	private transformFileContent: any;
	private viewDataType: string;
	protected uiConfig: any = {
		labelColSize: 3,
		inputColSize: 3,
		buttonColSize: 1,
		urlColSize: 2
	};
	protected transformProgress = {
		progressKey: null,
		currentProgress: 0,
	};
	private transformInterval: any;

	constructor(
		private importAssetsService: ImportAssetsService,
		private notifier: NotifierService,
		private dataIngestionService: DataIngestionService) {
			this.file.fileUID = null;
	}

	ngOnInit(): void {
		this.importAssetsService.getManualOptions().subscribe( (result) => {
			this.actionOptions = result.actions;
			this.dataScriptOptions = result.dataScripts;
		});
	}

	/**
	 * Fetch button clicked event.
	 * Calls the process of fetch.
	 */
	private onFetch(): void {
		this.fetchInProcess = true;
		this.fetchResult = null;
		this.fetchFileContent = null;
		this.transformResult = null;
		this.transformFileContent = null;
		this.importResult = null;
		// this.selectedScriptOption = null;
		this.importAssetsService.postFetch(this.selectedActionOption).subscribe( (result) => {
			this.fetchResult = {
				status: result.status
			};
			this.fetchInputUsed = 'action';
			if (result.status === 'error') {
				this.notifier.broadcast({
					name: AlertType.DANGER,
					message: result.errors[0]
				});
			} else {
				this.fetchResult.filename = result.data.filename;
			}
			this.fetchInProcess = false;
		} );
	}

	/**
	 * Event when action script select changes its value.
	 * @param event
	 */
	private onActionScriptChange(event: any): void {
		let matchedScript = this.dataScriptOptions.find( script => script.id === event.defaultDataScriptId );
		if (matchedScript) {
			this.selectedScriptOption = matchedScript;
		}
	}

	/**
	 * Transform button clicked event.
	 * Calls Transform process on endpoint.
	 */
	protected onTransform(): void {
		this.transformInProcess = true;
		this.transformResult = null;
		this.transformFileContent = null;
		this.importResult = null;
		// ------------------------- DUMMY CODE ---------------------------------
		this.setTransformProgressInterval();
		// ----------------------------------------------------------------------
		// TODO: (real code) uncomment below code when endpoints ready!
		/*
		this.importAssetsService.initiateTransform(this.script, this.filename).subscribe( (result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS && result.data.progressKey) {
				this.transformProgress.progressKey = result.data.progressKey;
				this.setTransformProgressInterval();
			} else {
				this.transformResult = { status: 'error', data: {}};
			}
		}, error => this.transformResult = { status: 'error', data: {}} );
		*/
	}

	/**
	 * Clears out the Transform interval loop.
	 */
	private clearTestScriptProgressInterval(): void {
		clearInterval(this.transformInterval);
		this.transformInProcess = false;
	}

	/**
	 * Creates an interval loop to retreive Transform current progress.
	 */
	private setTransformProgressInterval(): void {
		this.transformProgress.currentProgress = 1;
		this.transformInterval = setInterval(() => {
			this.getTransformProgress();
		}, .5 * 1000); // 5 seconds.
	}

	/**
	 * Operation of the Test Script interval that will be executed n times in a loop.
	 */
	private getTransformProgress(): void {
		// ---------------------- DUMMY CODE -------------------------------
		let currentProgress = this.transformProgress.currentProgress + 10;
		this.transformProgress.currentProgress = currentProgress;
		if (currentProgress >= 100) {
			this.transformResult = { status: 'success', data: {}};
			this.clearTestScriptProgressInterval();
		}
		// -----------------------------------------------------------------

		// TODO: (real code) uncomment below code when endpoints ready!
		/*
		this.dataIngestionService.getJobProgress(this.transformProgress.progressKey)
			.subscribe( (response: ApiResponseModel) => {
				let currentProgress = response.data.percentComp;
				this.transformProgress.currentProgress = currentProgress;
				if (currentProgress === 100) {
					this.transformResult = response.data.detail;
					if (this.transformResult.status === 'error') {
						this.notifier.broadcast({
							name: AlertType.DANGER,
							message: this.transformResult.errors[0]
						});
					}
					this.clearTestScriptProgressInterval();
				}
			});
			*/
	}

	/**
	 * Import button clicked event.
	 * Calls Import process on endpoint.
	 */
	private onImport(): void {
		this.importInProcess = true;
		this.importResult = null;
		this.importAssetsService.postImport(this.transformResult.data.filename).subscribe( (result) => {
			this.importResult = result;
			this.importInProcess = false;
		});
	}

	/**
	 * Gets the raw data of the view data fetch file content result.
	 * @returns {string}
	 */
	private getFetchFileContentValue(): string {
		if (this.fetchFileContent) {
			return JSON.stringify(this.fetchFileContent);
		} else {
			return '';
		}
	}

	/**
	 * Gets the raw data of the view data transform file content result.
	 * @returns {string}
	 */
	private getTransformFileContentValue(): string {
		if (this.transformFileContent) {
			return JSON.stringify(this.transformFileContent);
		} else {
			return '';
		}
	}

	/**
	 * View data clicked event.
	 * Calls endpoint to get file content result.
	 * @param {string} type : can be either 'Fetch' or 'Transform'
	 */
	private onViewData(type: string): void {
		this.viewDataType = type;
		if (this.viewDataType === 'FETCH') {
			this.fetchFileContent = null;
			this.importAssetsService.getFileContent(this.fetchResult.filename).subscribe((result) => {
				this.fetchFileContent = result;
			});
		} else {
			this.transformFileContent = null;
			this.importAssetsService.getFileContent(this.transformResult.data.filename).subscribe((result) => {
				this.transformFileContent = result;
			});
		}
	}

	/**
	 * Close clicked event.
	 * Cleans out results for fetch and transform.
	 */
	private onCloseFileContents(): void {
		this.fetchFileContent = null;
		this.transformFileContent = null;
		this.viewDataType = null;
	}

	/**
	 * Clear clicked event.
	 * Clears out most of results peformed on the page.
	 */
	private onClear(): void {
		this.fetchResult = null;
		this.fetchFileContent = null;
		this.transformResult = null;
		this.transformFileContent = null;
		this.viewDataType = null;
		this.importResult = null;
		this.removeFileByUID();
	}

	private disableTransformButton() {
		return !this.selectedScriptOption || this.selectedScriptOption === -1
			|| !this.fetchResult || !this.fetchResult.filename || this.fetchResult.status === 'error';
	}

	private clearFilename(e?: any) {
		this.fetchResult = null;
		this.fetchFileContent = null;
	}

	private onSelectFile(e?: any): void {
		this.file.fileUID = e.files[0].uid;
	}

	private onRemoveFile(e: RemoveEvent): void {
		if (!this.fetchResult || !this.fetchResult.filename) {
			return;
		}
		e.data = { filename: this.fetchResult.filename };
	}

	private onUploadFile(e: any): void {
		this.clearFilename();
	}

	private removeFileByUID(): void {
		if (this.file.fileUID) {
			this.kendoUploadInstance.removeFilesByUid(this.file.fileUID);
		}
	}

	private completeEventHandler(e: SuccessEvent) {
		let response = e.response.body.data;
		if (response.operation === 'delete') { // file deleted successfully
			// console.log(response.data);
			this.clearFilename();
			this.file.fileUID = null;
		} else if (response.filename) { // file uploaded successfully
			let filename = response.filename;
			this.fetchResult = { status: 'success', filename: filename };
			this.fetchInputUsed = 'file';
		} else {
			this.clearFilename();
			this.fetchResult = { status: 'error' };
		}
	}
}