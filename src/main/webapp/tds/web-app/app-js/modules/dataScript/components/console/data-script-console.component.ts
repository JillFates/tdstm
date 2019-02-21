import {AfterViewInit, Component, Inject} from '@angular/core';
import { UIExtraDialog } from '../../../../shared/services/ui-dialog.service';
import {ScriptConsoleSettingsModel} from '../../model/script-result.models';
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';

declare var jQuery: any;
const DIALOG_ID = '#viewConsole';

@Component({
	selector: 'data-script-console',
	templateUrl: 'data-script-console.component.html'
})
export class DataScriptConsoleComponent extends UIExtraDialog implements AfterViewInit {
	public isWindowMaximized = false;
	public initialWindowStyle = null;
	public modalOptions: DecoratorOptions;
	constructor(public consoleSettings: ScriptConsoleSettingsModel) {
		super(DIALOG_ID);
		this.modalOptions = { isFullScreen: true, isResizable: true };
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

	protected maximizeWindow() {
		this.isWindowMaximized = true;
	}

	protected restoreWindow() {
		this.isWindowMaximized = false;
	}

}