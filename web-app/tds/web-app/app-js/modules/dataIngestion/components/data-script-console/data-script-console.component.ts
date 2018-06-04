import {AfterViewInit, Component, Inject} from '@angular/core';
import { UIExtraDialog } from '../../../../shared/services/ui-dialog.service';
import {ScriptConsoleSettingsModel} from '../../model/script-result.models';

declare var jQuery: any;
const DIALOG_ID = '#viewConsole';
const TEXT_AREA_LOG_ID = '#textAreaLog';

@Component({
	selector: 'data-script-console',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-console/data-script-console.component.html'
})
export class DataScriptConsoleComponent extends UIExtraDialog implements AfterViewInit {

	private dialogInstance: any;
	private textAreaInstance: any;

	constructor(private consoleSettings: ScriptConsoleSettingsModel) {
		super(DIALOG_ID);
	}

	ngAfterViewInit(): void {
		this.dialogInstance = jQuery(DIALOG_ID);
		this.textAreaInstance = jQuery(TEXT_AREA_LOG_ID);
		// fix to resize modal on textarea resize
		this.textAreaInstance.resizable({
			resize: function (event) {
				let modalDialogInstance = jQuery('#console-modal-dialog');
				modalDialogInstance.css('width', event.target.offsetWidth);
			}
		});
	}

	/**
	 * On close dialog store position/size settings.
	 */
	protected cancelCloseDialog(): void {
		this.consoleSettings.top =  parseInt(this.dialogInstance.css('top'), 10);
		this.consoleSettings.left = parseInt(this.dialogInstance.css('left'), 10);
		this.consoleSettings.height = parseInt(this.textAreaInstance.css('height'), 10);
		this.consoleSettings.width = parseInt(this.textAreaInstance.css('width'), 10);
		this.dismiss();
	}

}