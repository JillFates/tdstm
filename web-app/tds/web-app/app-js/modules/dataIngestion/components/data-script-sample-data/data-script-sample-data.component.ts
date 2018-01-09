import {Component, ViewChild} from '@angular/core';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {
	FileRestrictions, SelectEvent, SuccessEvent, UploadComponent,
	UploadEvent
} from '@progress/kendo-angular-upload';
import {DataIngestionService} from '../../service/data-ingestion.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {FormBuilder, FormGroup} from '@angular/forms';

@Component({
	selector: 'data-script-sample-data',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-sample-data/data-script-sample-data.component.html',
})
export class DataScriptSampleDataComponent extends UIExtraDialog {

	@ViewChild('kendoUploadInstance') kendoUploadInstance: UploadComponent;
	private uploadOption = 'csv';
	private file: any = {
		uploadRestrictions : { allowedExtensions: ['csv', 'txt', 'xml', 'json', '.xlxs', 'xls'] },
		uploadSaveUrl : 'saveUrl',
		autoUpload: false,
		uploadedFilename: null
	};
	private csv: any = {
		options : ['csv', 'txt', 'xml', 'json'],
		selected : 'csv',
		fileContent : ''
	};

	constructor(
		private dataIngestionService: DataIngestionService,
		private notifierService: NotifierService) {
		super('#loadSampleData');
	}

	private validForm(): boolean {
		if (this.uploadOption === 'csv' && this.csv.fileContent.length < 1) {
			return false;
		}
		return true;
	}

	private onLoadData(): void {
		if (this.uploadOption === 'csv') {
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
		} else if (this.uploadOption === 'file') {
			this.kendoUploadInstance.uploadFiles();
		} else {
			console.log('upload option not supported, closing dialog ..');
			this.close();
		}
	}

	protected cancelCloseDialog(): void {
		this.dismiss();
	}

	private completeEventHandler(e: SuccessEvent) {
		let filename = e.response.body.data.filename;
		this.file.uploadedFilename = filename;
	}

	private uploadEventHandler(e: UploadEvent) {
		this.file.uploadedFilename = null;
	}
}