import { Component } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'data-script-sample-data',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-sample-data/data-script-sample-data.component.html',
})
export class DataScriptSampleDataComponent {

	constructor(private activeDialog: UIActiveDialogService) {

	}

	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}
}