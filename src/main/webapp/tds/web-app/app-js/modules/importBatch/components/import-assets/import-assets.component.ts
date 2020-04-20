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
	FILE_UPLOAD_TYPE_PARAM,
	PROGRESSBAR_INTERVAL_TIME

} from '../../../../shared/model/constants';
import {ImportBatchStates} from '../../import-batch-routing.states';
import { ImportBatchService } from '../../service/import-batch.service';

@Component({
	selector: 'import-assets',
	templateUrl: 'import-assets.component.html'
})
export class ImportAssetsComponent implements OnInit {

	@ViewChild('kendoUploadInstance', {static: false}) kendoUploadInstance: UploadComponent;

	private readonly dataScriptOptionsInitial = {
		id: -1,
		name: 'GLOBAL.PLEASE_SELECT',
		isAutoProcess: false
	};
	public dataScriptOptions = [this.dataScriptOptionsInitial];
	public selectedScriptOption = this.dataScriptOptions[0];
	public selectedActionOption = -1;
	private transformInterval: any;
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
	public file: KendoFileUploadBasicConfig = new KendoFileUploadBasicConfig();
	readonly UI_CONFIG = {
		labelColSize: 3,
		inputColSize: 3,
		buttonColSize: 1,
		urlColSize: 2,
		showTransformButton: false,
		showAutoProcessElements: false,
		showManualProcesslElements: false,
		hasFinishedManualImport: false,
		transformBtnLabel: '',
		sendNotification: false
	};

	public uiConfig: any = {...this.UI_CONFIG};
	private importInterval: any;
	importResult: any;
	importProgress = {
		progressKey: null,
		currentProgress: 0,
		inProgress: false
	};

	constructor(
		private importAssetsService: ImportAssetsService,
		private importBatchService: ImportBatchService,
		private notifier: NotifierService,
		private dataIngestionService: DataScriptService) {
			this.file.fileUID = null;
	}

	ngOnInit(): void {
		this.importAssetsService.getManualOptions().subscribe( (result) => {
			this.actionOptions = result.actions;
			this.dataScriptOptions = [...this.dataScriptOptions, ...result.dataScripts];
		});
	}

	onChangeSelectedScript(etlScript) {
		this.selectedScriptOption = etlScript;
		if (etlScript === this.dataScriptOptionsInitial) {
			this.uiConfig = {...this.UI_CONFIG};
		} else if (etlScript.isAutoProcess) {
			this.setAutoProcessScript();
		} else {
			this.setManualProcessScript();
		}
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
		this.importAssetsService.postTransform(this.selectedScriptOption, this.fetchResult.filename, this.uiConfig.sendNotification).subscribe( (result: ApiResponseModel) => {
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
		let filename: any;
		this.importProgress.inProgress = true;
		this.importResult = null;
		if (this.selectedScriptOption.isAutoProcess) {
			this.runAutoImport(this.fetchResult.filename);
		} else {
			filename = this.transformResult.data.filename;
			this.importAssetsService.postImport(filename).subscribe(result => {
				if (result.status === ApiResponseModel.API_SUCCESS && result.data.progressKey) {
					this.importProgress.progressKey = result.data.progressKey;
					this.setImportProgressInterval();
				} else {
					this.handleImportResultError()
				}
			});
		}
	}

	/**
	 * Runs auto import process.
	 * @param filename
	 */
	runAutoImport(filename: string): void {
		this.importAssetsService.postTransform(this.selectedScriptOption, filename, this.uiConfig.sendNotification)
			.subscribe(( result: any ) => {
				this.importResult = result;
				if (this.importResult.data.domains && this.importResult.data.domains.length > 0) {
					this.importResult.data.domains.forEach( (item: any) => {
						if (!item.batchId) {
							item.batchId = null;
						}
					});
				}
				this.importProgress.inProgress = false;
				if (this.uiConfig.showAutoProcessElements) {
					this.onClear();
					this.notifier.broadcast({
						name: AlertType.SUCCESS,
						message: 'The ETL import process was succesfully initiated'
					});
				}
			}, (err) => {
				console.log(err);
			});
	}

	/**
	 * Creates an interval loop to retrieve Import current progress.
	 */
	private setImportProgressInterval(): void {
		this.importProgress.currentProgress = 1;
		this.importInterval = setInterval(() => {
			this.getImportProgress();
		}, PROGRESSBAR_INTERVAL_TIME);
	}

	/**
	 * Operation of the Test Script interval that will be executed n times in a loop.
	 */
	private getImportProgress(): void {
		this.dataIngestionService.getJobProgress(this.importProgress.progressKey)
			.subscribe( (response: ApiResponseModel) => {
				let currentProgress = response.data.percentComp;
				this.importProgress.currentProgress = currentProgress;
				// On Fail
				if (response.data.status === PROGRESSBAR_FAIL_STATUS) {
					this.handleImportResultError(response.data.detail);
					this.importProgress.inProgress = false;
					this.clearImportProgressInterval();
				} else if (currentProgress === 100 && response.data.status === PROGRESSBAR_COMPLETED_STATUS) {
					// On finish without filename output (Fail)
					if (!response.data.detail || !response.data.data || !response.data.data.groupGuid) {
						this.handleImportResultError();
						this.importProgress.inProgress = false;
					} else {
						// On Success
						setTimeout( () => {
							this.importBatchService.getImportBatches(response.data.data.groupGuid)
								.subscribe(result => {
									this.postImportResult(result);
									this.importResult.status = ApiResponseModel.API_SUCCESS;
									this.importProgress.inProgress = false;
									console.log(result);
								});
						}, 500);
					}
					this.clearImportProgressInterval();
				}
			});
	}

	/**
	 * Clears out the Transform interval loop.
	 */
	private clearImportProgressInterval(): void {
		clearInterval(this.importInterval);
	}

	/**
	 * Handle and display Import result error message.
	 * @param errorMessage
	 */
	private handleImportResultError(errorMessage: string = null): void {
		this.importResult = {};
		this.importResult.status = ApiResponseModel.API_ERROR;
		this.notifier.broadcast({
			name: AlertType.DANGER,
			message: errorMessage || 'An error occurred during the import process'
		});
	}

	/**
	 * Process the import result from import batch endpoint call to build json result.
	 */
	private postImportResult(result: any): void {
		this.importResult = {
			data: {
				domains: []
			}
		};
		if (result.data && result.data.length > 0) {
			this.importResult.data.domains = result.data.map(item => {
				return {
					domainClass: item.domainClassName,
					batchId: item.id,
					rowsCreated: item.recordsSummary ? item.recordsSummary.count : 0
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
		this.uiConfig = {...this.UI_CONFIG};
		this.selectedScriptOption = this.dataScriptOptionsInitial;
		this.selectedActionOption = -1;
		this.clearFileList();
		this.transformInProcess = false;
		this.transformResult = null;
		this.importResult = null;
		this.importProgress.inProgress = false;
	}

	public disableTransformButton() {
		return (
			!this.selectedScriptOption ||
			this.selectedScriptOption.id === this.dataScriptOptionsInitial.id ||
			!this.fetchResult ||
			!this.fetchResult.filename ||
			this.fetchResult.status === ApiResponseModel.API_ERROR
		);
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
		if (this.transformResult && this.transformResult.data) {
		// if (this.transformResult && this.transformResult.status !== 'error') {
			tempServerFilesToDelete.push(this.transformResult.data.filename)
		}

		if (this.selectedScriptOption.isAutoProcess) {
			tempServerFilesToDelete.push( this.fetchResult.filename);
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

	private clearFileList(): void {
		this.kendoUploadInstance.fileList.clear();
		this.fetchResult = null;
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

	private setAutoProcessScript() {
		this.uiConfig = {
			...this.uiConfig,
			showAutoProcessElements: true,
			showManualProcessElements: false,
			sendNotification: true,
			transformBtnLabel: 'IMPORT_ASSETS.AUTO_IMPORT.INITIATE_IMPORT'
		}
	}

	private setManualProcessScript() {
		this.uiConfig = {
			...this.uiConfig,
			showAutoProcessElements: false,
			showManualProcessElements: true,
			sendNotification: false,
			transformBtnLabel: 'IMPORT_ASSETS.MANUAL_IMPORT.TRANSFORM'
		};
	}
}
