import { Component } from '@angular/core';
import { UIExtraDialog } from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'data-script-sample-data',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-sample-data/data-script-sample-data.component.html',
})
export class DataScriptSampleDataComponent extends UIExtraDialog {

	uploadOption = 'csv';

	constructor() {
		super('#loadSampleData');
	}

	protected cancelCloseDialog(): void {
		this.dismiss();
	}
}