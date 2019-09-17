import {Component, OnInit, ViewChild} from '@angular/core';
import {ImportAssetsService} from '../../service/import-assets.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {RemoveEvent, SuccessEvent, UploadComponent, UploadEvent} from '@progress/kendo-angular-upload';
import {KendoFileUploadBasicConfig} from '../../../../shared/providers/kendo-file-upload.interceptor';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';

import {
	DataScriptService,
	PROGRESSBAR_COMPLETED_STATUS, PROGRESSBAR_FAIL_STATUS
} from '../../../dataScript/service/data-script.service';
import {
	ASSET_IMPORT_FILE_UPLOAD_TYPE,
	ETL_SCRIPT_FILE_UPLOAD_TYPE,
	FILE_UPLOAD_TYPE_PARAM,
	PROGRESSBAR_INTERVAL_TIME
} from '../../../../shared/model/constants';
import {ImportBatchStates} from '../../import-batch-routing.states';

declare var jQuery: any;

@Component({
	selector: 'import-assets',
	templateUrl: 'import-assets.component.html'
})
export class ImportAssetsComponent implements OnInit {

	@ViewChild('kendoUploadInstance', {static: false}) kendoUploadInstance: UploadComponent;
	public dataScriptOptions = [];
	public selectedActionOption = -1;
	private transformInterval: any;
	public importResult: any;
	public importInProcess = false;
	public fetchFileContent: any;
	public transformFileContent: any;
	private viewDataType: string;
	protected transformProgress = {
		progressKey: null,
		currentProgress: 0,
	};
	protected IMPORT_BATCH_STATES = ImportBatchStates;
	public transformResult: ApiResponseModel;
	public transformInProcess = false;
	public actionOptions = [];
	public fetchResult: any;
	public fetchInProcess = false;
	public fetchInputUsed: 'action' | 'file' = 'action';
	public selectedScriptOption = -1;
	public file: KendoFileUploadBasicConfig = new KendoFileUploadBasicConfig();
	public uiConfig: any = {
		labelColSize: 3,
		inputColSize: 3,
		buttonColSize: 1,
		urlColSize: 2
	};

	constructor(
		private importAssetsService: ImportAssetsService,
		private notifier: NotifierService,
		private dataIngestionService: DataScriptService) {
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
	public onFetch(): void {
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
	public onActionScriptChange(event: any): void {
		let matchedScript = this.dataScriptOptions.find( script => script.id === event.defaultDataScriptId );
		if (matchedScript) {
			this.selectedScriptOption = matchedScript;
		}
	}

	/**
	 * Transform button clicked event.
	 * Calls Transform process on endpoint.
	 */
	public onTransform(): void {
		this.transformInProcess = true;
		this.transformResult = null;
		this.transformFileContent = null;
		this.importResult = null;
		this.importAssetsService.postTransform(this.selectedScriptOption, this.fetchResult.filename).subscribe( (result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS && result.data.progressKey) {
				this.transformProgress.progressKey = result.data.progressKey;
				this.setTransformProgressInterval();
			} else {
				this.transformResult = new ApiResponseModel();
				this.transformResult.status = ApiResponseModel.API_ERROR;
				this.transformResult.data = {};
			}
		}, error => {
			this.transformResult = new ApiResponseModel();
			this.transformResult.status = ApiResponseModel.API_ERROR;
			this.transformResult.data = {};
			this.transformInProcess = false;
		});
	}

	/**
	 * Clears out the Transform interval loop.
	 */
	private clearTestScriptProgressInterval(): void {
		clearInterval(this.transformInterval);
	}

	/**
	 * Creates an interval loop to retreive Transform current progress.
	 */
	private setTransformProgressInterval(): void {
		this.transformProgress.currentProgress = 1;
		this.transformInterval = setInterval(() => {
			this.getTransformProgress();
		}, PROGRESSBAR_INTERVAL_TIME);
	}

	/**
	 * Operation of the Test Script interval that will be executed n times in a loop.
	 */
	private getTransformProgress(): void {
		this.dataIngestionService.getJobProgress(this.transformProgress.progressKey)
			.subscribe( (response: ApiResponseModel) => {
				let currentProgress = response.data.percentComp;
				this.transformProgress.currentProgress = currentProgress;
				// On Fail
				if (response.data.status === PROGRESSBAR_FAIL_STATUS) {
					this.handleTransformResultError(response.data.detail);
					this.transformInProcess = false;
					this.clearTestScriptProgressInterval();
				} else if (currentProgress === 100 && response.data.status === PROGRESSBAR_COMPLETED_STATUS) {
					// On finish without filename output (Fail)
					if (!response.data.detail) {
						this.handleTransformResultError('The generated intermediate ETL data file could not be accessed.');
						this.transformInProcess = false;
					} else {
						// On Success
						setTimeout( () => {
							this.transformResult = new ApiResponseModel();
							this.transformResult.status = ApiResponseModel.API_SUCCESS;
							this.transformResult.data = {filename: response.data.detail};
							this.transformInProcess = false;
						}, 500);
					}
					this.clearTestScriptProgressInterval();
				}
			});
	}

	private handleTransformResultError(errorMessage: string): void {
		this.transformResult = new ApiResponseModel();
		this.transformResult.status = ApiResponseModel.API_ERROR;
		this.notifier.broadcast({
			name: AlertType.DANGER,
			message: errorMessage
		});
	}

	/**
	 * Import button clicked event.
	 * Calls Import process on endpoint.
	 */
	public onImport(): void {
		this.importInProcess = true;
		this.importResult = null;
		this.importAssetsService.postImport(this.transformResult.data.filename).subscribe( (result) => {
			this.importResult = result;
			this.postImportResult();
			this.importInProcess = false;
		});
	}

	/**
	 * Add a batchId = null if no batchId property comes from import batch domain result.
	 */
	private postImportResult(): void {
		if (this.importResult.data.domains && this.importResult.data.domains.length > 0) {
			this.importResult.data.domains.forEach( (item: any) => {
				if (!item.batchId) {
					item.batchId = null;
				}
			});
		}
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
	public onClear(): void {
		this.removeFileByUID();
	}

	public disableTransformButton() {
		return !this.selectedScriptOption || this.selectedScriptOption === -1
			|| !this.fetchResult || !this.fetchResult.filename || this.fetchResult.status === ApiResponseModel.API_ERROR;
	}

	private clearFilename(e?: any) {
		this.fetchResult = null;
		this.fetchFileContent = null;
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
		this.viewDataType = null;
		this.importResult = null;
	}

	public onUploadFile(e: UploadEvent): void {
		e.data = {};
		e.data[FILE_UPLOAD_TYPE_PARAM] = ASSET_IMPORT_FILE_UPLOAD_TYPE;
		this.clearFilename();
	}

	private removeFileByUID(): void {
		if (this.file.fileUID) {
			this.kendoUploadInstance.removeFilesByUid(this.file.fileUID);
		}
	}

	public completeEventHandler(e: SuccessEvent) {
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
