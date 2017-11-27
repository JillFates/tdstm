import { Component } from '@angular/core';
import { UIExtraDialog } from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'data-script-console',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-console/data-script-console.component.html',
	styles: [`
	textarea.console {
		color:#39d854;
		background-color: #000;
	}
	textarea.console[disabled] {
		cursor: auto;
	}`]
})
export class DataScriptConsoleComponent extends UIExtraDialog {

	message: string;

	constructor() {
		super('#viewConsole');
	}

	protected cancelCloseDialog(): void {
		this.dismiss();
	}

}