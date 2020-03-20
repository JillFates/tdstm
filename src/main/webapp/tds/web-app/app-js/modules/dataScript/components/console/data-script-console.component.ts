import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {ScriptConsoleSettingsModel} from '../../model/script-result.models';
import {Dialog} from 'tds-component-library';

@Component({
	selector: 'data-script-console',
	templateUrl: 'data-script-console.component.html',
})
export class DataScriptConsoleComponent extends Dialog implements OnInit {
	@Input() data: any;
	public consoleSettings;

	ngOnInit(): void {
		this.consoleSettings = Object.assign({}, this.data.consoleSettingsModel);
	}

	/**
	 * On close dialog store position/size settings.
	 */
	public cancelCloseDialog(): void {
		this.onCancelClose();
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
