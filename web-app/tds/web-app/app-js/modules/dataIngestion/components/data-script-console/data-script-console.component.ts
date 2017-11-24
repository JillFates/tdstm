import { Component } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'data-script-console',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-console/data-script-console.component.html',
})
export class DataScriptConsoleComponent {

	constructor(private activeDialog: UIActiveDialogService) {

	}

	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

}