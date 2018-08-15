import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {
	FileRestrictions,
	RemoveEvent,
	SuccessEvent,
	UploadComponent,
	UploadEvent
} from '@progress/kendo-angular-upload';
import {DataIngestionService} from '../../service/data-ingestion.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {ImportAssetsService} from '../../../importBatch/service/import-assets.service';
import {KendoFileUploadBasicConfig} from '../../../../shared/providers/kendo-file-upload.interceptor';
import {
	ETL_SCRIPT_FILE_UPLOAD_TYPE,
	FILE_UPLOAD_TYPE_PARAM,
	REMOVE_FILENAME_PARAM
} from '../../../../shared/model/constants';
import {DataScriptModel} from '../../model/data-script.model';

@Component({
	selector: 'data-script-sample-data',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-sample-data/data-script-sample-data.component.html',
})
export class DataScriptSampleDataComponent extends UIExtraDialog {

	@ViewChild('kendoUploadInstance') kendoUploadInstance: UploadComponent;
	protected file: KendoFileUploadBasicConfig = new KendoFileUploadBasicConfig();
	protected originalFileName: any = { temporary: null, fileUploaded: null};
	protected OPTIONS: any = {
		FILE: 'file',
		SERVICE: 'service',
		CSV: 'csv',
		selected: undefined,
		useFileFrom: undefined
	};
	private csv: any = {
		options : [
			{ text: 'Select a format', value: -1 },
			{ text: 'csv', value: 0 },
			{ text: 'xml', value: 2 },
			{ text: 'json', value: 3 }
		],
		selected : undefined,
		fileContent : '',
		filename: null,
		state: undefined,
	};
	private webService: any = {
		options: [],
		selected: undefined,
		state: undefined,
		filename: null,
	};

	private autoETL = false;
	private assetClassOptions: Array<any> = [
		{ text: 'Select a class', value:  -1 },
		{ text: 'Application', value: 0 },
		{ text: 'Device', value: 1 }
	];
	private assetClassSelected = this.assetClassOptions[0];
	private apiActionOptions = [];

	constructor(
		@Inject('etlScript') protected etlScriptModel: DataScriptModel,
		private dataIngestionService: DataIngestionService,
		private notifierService: NotifierService,
		private importAssetsService: ImportAssetsService) {
			super('#loadSampleData');
			this.onPageLoad();
	}

	/**
	 * On Page loads, pre-select default options.
	 * Set File radio button as pre-selected.
	 * Pre-select option with value 1(CSV) on file content type options.
	 * Load Manual Options for web services dropdown.
	 */
	private onPageLoad(): void {
		this.file.uploadedFilename = null;
		this.csv.selected = this.csv.options[0];
		this.OPTIONS.selected = this.OPTIONS.FILE;
		this.importAssetsService.getManualOptions().subscribe( (result) => {
			if (result.actions && result.actions.length > 0) {
				this.webService.options = result.actions;
			}
		});
	}

	/**
	 * On Continue button click.
	 * Set the current sample data upload type and close the dialog.
	 */
	private onContinue(): void {
		let filename: any = { temporaryFileName: null, originalFileName: null};
		if (this.OPTIONS.useFileFrom === this.OPTIONS.CSV) {
			filename.temporaryFileName = this.csv.filename;
			filename.originalFileName = this.csv.filename;
		}
		if (this.OPTIONS.useFileFrom === this.OPTIONS.SERVICE) {
			filename.temporaryFileName = this.webService.filename;
			filename.originalFileName = this.webService.filename;
		}
		if (this.OPTIONS.useFileFrom === this.OPTIONS.FILE) {
			filename.temporaryFileName = this.file.uploadedFilename;
			filename.originalFileName = this.originalFileName.fileUploaded;
		}
		this.close(filename);
	}

	/**
	 * Validates the form.
	 * Filename should not be empty for the current selection.
	 * @returns {boolean}
	 */
	private validForm(): boolean {
		if (this.OPTIONS.selected === this.OPTIONS.CSV && this.csv.filename) {
			return true;
		} else if (this.OPTIONS.selected === this.OPTIONS.SERVICE && this.webService.filename) {
			return true;
		} else if (this.OPTIONS.selected === this.OPTIONS.FILE && this.file.uploadedFilename) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Upload Content action.
	 * On Upload button click.
	 */
	private onUploadFileText(): void {
		this.dataIngestionService.uploadETLScriptFileText(this.csv.fileContent, this.csv.selected.text).subscribe( result => {
			if (result.status === 'success' && result.data.filename) {
				this.csv.filename = result.data.filename;
				this.csv.state = 'success';
				this.OPTIONS.useFileFrom = this.OPTIONS.CSV;
			} else {
				this.csv.state = 'fail';
			}
		});
	}

	/**
	 * Fetch from webservice action.
	 * On Fetch button click.
	 */
	private onFetch(): void {
		this.importAssetsService.postFetch(this.webService.selected).subscribe( (result) => {
			if (result.status === 'success') {
				this.webService.state = 'success';
				this.webService.filename = result.data.filename;
				this.OPTIONS.useFileFrom = this.OPTIONS.SERVICE;
			} else {
				this.notifierService.broadcast({
					name: AlertType.DANGER,
					message: result.errors[0]
				});
				this.webService.state = 'fail';
			}
		} );
	}

	/**
	 * Upload File event.
	 * On upload completed.
	 * @param {SuccessEvent} e
	 */
	private completeEventHandler(e: SuccessEvent) {
		let response = e.response.body.data;
		if (response.operation === 'delete') { // file deleted successfully
			// console.log(response.data);
			this.clearFilename();
		} else { // file uploaded successfully
			let filename = response.filename;
			this.file.uploadedFilename = filename;
			this.OPTIONS.useFileFrom = this.OPTIONS.FILE;
			this.originalFileName.fileUploaded = this.originalFileName.temporary;
		}
	}

	protected onUploadFile(e: UploadEvent): void {
		e.data = {};
		e.data[FILE_UPLOAD_TYPE_PARAM] = ETL_SCRIPT_FILE_UPLOAD_TYPE;
		this.clearFilename();
		this.originalFileName.temporary = e.files[0].name;
	}

	/**
	 * Upload File action.
	 * On clear file name.
	 * @param e
	 */
	private clearFilename(e?: any) {
		this.file.uploadedFilename = null;
		this.OPTIONS.useFileFrom = null;
		this.originalFileName.temporary = null;
		this.originalFileName.fileUploaded = null;
	}

	/**
	 * Upload File action.
	 * On Remove file button click.
	 * @param {RemoveEvent} e
	 */
	private onRemoveFile(e: RemoveEvent) {
		e.data = {};
		e.data[REMOVE_FILENAME_PARAM] = this.file.uploadedFilename;
	}

	/**
	 * On Cancel Close Dialog Popup Component.
	 */
	protected cancelCloseDialog(): void {
		this.dismiss();
	}
}