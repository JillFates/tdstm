import {Component} from '@angular/core';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {FileRestrictions, SuccessEvent} from '@progress/kendo-angular-upload';

@Component({
	selector: 'data-script-sample-data',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-sample-data/data-script-sample-data.component.html',
})
export class DataScriptSampleDataComponent extends UIExtraDialog {

	public uploadRestrictions: FileRestrictions = {
		allowedExtensions: ['.xlsx', '.csv', '.xml', '.json']
	};
	public uploadSaveUrl = 'saveUrl';
	private fileLoadReference: string;

	public completeEventHandler(e: SuccessEvent) {
		console.log('File Uploaded!');
		let filename = e.response.body.data.filename;
		this.fileLoadReference = filename;
	}

	uploadOption = 'csv';

	constructor() {
		super('#loadSampleData');
	}

	private onLoadData(): void {
		this.close();
	}

	protected cancelCloseDialog(): void {
		this.dismiss();
	}
}