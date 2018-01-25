import {Component, OnInit, ViewChild} from '@angular/core';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import { FileRestrictions, RemoveEvent, SuccessEvent, UploadComponent } from '@progress/kendo-angular-upload';
import {DataIngestionService} from '../../service/data-ingestion.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {ImportAssetsService} from '../../../importAssets/service/import-assets.service';

@Component({
	selector: 'data-script-sample-data',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-sample-data/data-script-sample-data.component.html',
})
export class DataScriptSampleDataComponent extends UIExtraDialog {

	@ViewChild('kendoUploadInstance') kendoUploadInstance: UploadComponent;

	private OPTIONS: any = {
		CSV: 'csv',
		FILE: 'file',
		SERVICE: 'service',
		selected: undefined,
		useFileFrom: undefined
	};
	private file: any = {
		uploadRestrictions : { allowedExtensions: ['csv', 'txt', 'xml', 'json', '.xlxs', 'xls'] },
		uploadSaveUrl : 'saveUrl',
		uploadDeleteUrl : 'removeUrl',
		autoUpload: false,
		uploadedFilename: undefined,
		checked: false
	};
	private csv: any = {
		options : [
			{ text: 'Select a format', value: -1 },
			{ text: 'csv', value: 0 },
			{ text: 'txt', value: 1 },
			{ text: 'xml', value: 2 },
			{ text: 'json', value: 3 }
		],
		selected : undefined,
		fileContent : '',
		filename: undefined,
		state: undefined,
		checked: false
	};
	private webService: any = {
		options: [],
		selected: undefined,
		state: undefined,
		filename: undefined,
		checked: false
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
		private dataIngestionService: DataIngestionService,
		private notifierService: NotifierService,
		private importAssetsService: ImportAssetsService) {
			super('#loadSampleData');
			this.onPageLoad();
	}

	private onPageLoad(): void {
		this.csv.selected = this.csv.options[0];
		this.OPTIONS.selected = this.OPTIONS.CSV;
		this.importAssetsService.getManualOptions().subscribe( (result) => {
			if (result.actions && result.actions.length > 0) {
				this.webService.options = result.actions;
			}
		});
	}

	private onSelectFilename(option: string): void {
		if (this.csv.checked) {
			this.OPTIONS.useFileFrom = this.OPTIONS.CSV;
		} else if (this.file.checked) {
			this.OPTIONS.useFileFrom = this.OPTIONS.FILE;
		} else if (this.webService.checked) {
			this.OPTIONS.useFileFrom = this.OPTIONS.SERVICE;
		}
		console.log(this.OPTIONS.useFileFrom);
	}

	private validForm(): boolean {
		if (this.OPTIONS.useFileFrom === this.OPTIONS.CSV && this.csv.filename.length > 0) {
			return true;
		}
		if (this.OPTIONS.useFileFrom === this.OPTIONS.SERVICE && this.webService.filename.length > 0) {
			return true;
		}
		if (this.OPTIONS.useFileFrom === this.OPTIONS.FILE && this.file.uploadedFilename.length > 0) {
			return true;
		}
		return false;
	}

	private onUploadFileText(): void {
		this.dataIngestionService.uploadText(this.csv.fileContent, this.csv.selected.text).subscribe( result => {
			if (result.status === 'success' && result.data.filename) {
				this.csv.filename = result.data.filename;
				this.csv.state = 'success';
			} else {
				this.csv.state = 'fail';
			}
		});
	}

	private onFetch(): void {
		this.importAssetsService.postFetch(this.webService.selected).subscribe( (result) => {
			if (result.status === 'success' && result.data.filename) {
				this.webService.state = 'success';
				this.webService.filename = result.data.filename;
			} else {
				this.notifierService.broadcast({
					name: AlertType.DANGER,
					message: result.errors[0]
				});
				this.webService.state = 'fail';
			}
		} );
	}

	private onLoadData(): void {
		let filename = null;
		if (this.OPTIONS.useFileFrom === this.OPTIONS.CSV) {
			filename = this.csv.filename;
		}
		if (this.OPTIONS.useFileFrom === this.OPTIONS.SERVICE) {
			filename = this.webService.filename;
		}
		if (this.OPTIONS.useFileFrom === this.OPTIONS.FILE) {
			filename = this.file.uploadedFilename;
		}
		this.close(filename);
	}

	private completeEventHandler(e: SuccessEvent) {
		let response = e.response.body.data;
		if (response.operation === 'delete') { // file deleted successfully
			// console.log(response.data);
			this.clearFilename();
		} else { // file uploaded successfully
			let filename = response.filename;
			this.file.uploadedFilename = filename;
			this.OPTIONS.useFileFrom = this.OPTIONS.FILE;
		}
	}

	private clearFilename(e?: any) {
		this.file.uploadedFilename = null;
		this.OPTIONS.useFileFrom = null;
	}

	private onRemoveFile(e: RemoveEvent) {
		e.data = { filename: this.file.uploadedFilename};
	}

	protected cancelCloseDialog(): void {
		this.dismiss();
	}
}