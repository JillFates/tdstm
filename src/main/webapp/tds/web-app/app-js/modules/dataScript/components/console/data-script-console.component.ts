import { AfterViewInit, Component } from '@angular/core';
import { UIExtraDialog } from '../../../../shared/services/ui-dialog.service';
import { ScriptConsoleSettingsModel } from '../../model/script-result.models';

const DIALOG_ID = '#viewConsole';

@Component({
	selector: 'data-script-console',
	templateUrl: 'data-script-console.component.html',
})
export class DataScriptConsoleComponent extends UIExtraDialog
	implements AfterViewInit {
	constructor(public consoleSettings: ScriptConsoleSettingsModel) {
		super(DIALOG_ID);
	}

	ngAfterViewInit(): void {
		console.log('after view init');
	}

	/**
	 * On close dialog store position/size settings.
	 */
	public cancelCloseDialog(): void {
		this.dismiss();
	}
}
