import { Component, Output, EventEmitter } from '@angular/core';
import { UIExtraDialog, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { DataScriptSampleDataComponent } from '../data-script-sample-data/data-script-sample-data.component';
import { DataScriptConsoleComponent } from '../data-script-console/data-script-console.component';

@Component({
	selector: 'data-script-etl-builder',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-etl-builder/data-script-etl-builder.component.html'
})
export class DataScriptEtlBuilderComponent extends UIExtraDialog {

	constructor(private dialogService: UIDialogService) {
		super('#etlBuilder');
	}

	protected cancelCloseDialog(): void {
		this.dismiss();
	}

	protected onLoadSampleData(): void {
		this.dialogService.extra(DataScriptSampleDataComponent, [])
			.then(() => console.log('ok'))
			.catch(() => console.log('still ok'));
	}

	protected onViewConsole(): void {
		this.dialogService.extra(DataScriptConsoleComponent, [])
			.then(() => console.log('ok'))
			.catch(() => console.log('still ok'));
	}

}