import { Component, Output, EventEmitter } from '@angular/core';
import { UIExtraDialog, UIDialogService } from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'data-script-etl-builder',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-etl-builder/data-script-etl-builder.component.html'
})
export class DataScriptEtlBuilderComponent extends UIExtraDialog {

	constructor() {
		super('#etlBuilder');
	}

	protected cancelCloseDialog(): void {
		this.dismiss();
	}
}