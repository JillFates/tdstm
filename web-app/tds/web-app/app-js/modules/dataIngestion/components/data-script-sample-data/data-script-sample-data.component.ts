import {Component, ViewChild} from '@angular/core';
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
	private loadSourceOption = 'csv';
	private file: any = {
		uploadRestrictions : { allowedExtensions: ['csv', 'txt', 'xml', 'json', '.xlxs', 'xls'] },
		uploadSaveUrl : 'saveUrl',
		uploadDeleteUrl : 'removeUrl',
		autoUpload: false,
		uploadedFilename: null
	};
	private csv: any = {
		options : ['csv', 'txt', 'xml', 'json'],
		selected : 'csv',
		fileContent : ''
	};
	private webService: any = {
		options: undefined,
		selected: undefined,
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
		this.importAssetsService.getManualOptions().subscribe( (result) => {
			this.webService.options = result.actions;
			this.webService.selected = this.webService.options[0];
		});
	}

	private validForm(): boolean {
		if (this.loadSourceOption === 'service' && this.autoETL && this.assetClassSelected.value === -1) {
			return false;
		}
		return this.file.uploadedFilename && this.file.uploadedFilename.length > 0;
	}

	private onUploadFileText(): void {
		this.file.uploadedFilename = null;
		this.dataIngestionService.uploadText(this.csv.fileContent, this.csv.selected).subscribe( result => {
			if (result.status && result.data.filename) {
				this.notifierService.broadcast({
					name: AlertType.SUCCESS,
					message: 'File saved successfully.'
				});
				this.file.uploadedFilename = result.data.filename;
			} else {
				this.notifierService.broadcast({
					name: AlertType.DANGER,
					message: 'File not saved.'
				});
			}
		});
	}

	private onLoadData(): void {
		this.close();
	}

	private completeEventHandler(e: SuccessEvent) {
		let response = e.response.body.data;
		if (response.operation === 'delete') { // file deleted successfully
			console.log(response.data);
			this.clearFilename();
		} else { // file uploaded successfully
			let filename = response.filename;
			this.file.uploadedFilename = filename;
		}
	}

	private clearFilename(e?: any) {
		this.file.uploadedFilename = null;
	}

	private onRemoveFile(e: RemoveEvent) {
		e.data = { filename: this.file.uploadedFilename};
	}

	protected cancelCloseDialog(): void {
		this.dismiss();
	}
}