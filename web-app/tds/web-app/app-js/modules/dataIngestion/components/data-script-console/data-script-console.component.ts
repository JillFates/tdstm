import {AfterViewInit, Component, Inject} from '@angular/core';
import { UIExtraDialog } from '../../../../shared/services/ui-dialog.service';

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

	constructor(@Inject('consoleSettings') private consoleSettings: any) {
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
		this.consoleSettings.top = this.dialogInstance.css('top');
		this.consoleSettings.left = this.dialogInstance.css('left');
		this.consoleSettings.height = this.textAreaInstance.css('height');
		this.consoleSettings.width = this.textAreaInstance.css('width');
		this.dismiss();
	}

}