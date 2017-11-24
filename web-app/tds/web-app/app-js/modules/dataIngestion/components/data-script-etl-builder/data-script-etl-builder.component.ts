import { Component } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'data-script-etl-builder',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-etl-builder/data-script-etl-builder.component.html',
})
export class DataScriptEtlBuilderComponent {

	constructor(private activeDialog: UIActiveDialogService) {

	}
}