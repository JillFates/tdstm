import {Component} from '@angular/core';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {FileRestrictions, SuccessEvent} from '@progress/kendo-angular-upload';
import {DataIngestionService} from '../../service/data-ingestion.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';

@Component({
	selector: 'data-script-sample-data',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-sample-data/data-script-sample-data.component.html',
})
export class DataScriptSampleDataComponent extends UIExtraDialog {

	// private allowedFileExtension = ['csv', 'txt', 'xml', 'json'];
	// private fileLoadReference: string;
	private uploadOption = 'csv';
	private file: any = {
		uploadRestrictions : { allowedExtensions: ['csv', 'txt', 'xml', 'json', '.xlxs', 'xls'] },
		uploadSaveUrl : 'saveUrl',
		fileLoadReference: ''
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

	public completeEventHandler(e: SuccessEvent) {
		console.log('File Uploaded!');
		let filename = e.response.body.data.filename;
		this.file.fileLoadReference = filename;
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
					this.close(result.data.filename);
				} else {
					this.notifierService.broadcast({
						name: AlertType.DANGER,
						message: 'File not saved.'
					});
				}
			});
		} else {
			console.log('upload option not supported, closing dialog ..');
			this.close();
		}
	}

	protected cancelCloseDialog(): void {
		this.dismiss();
	}
}