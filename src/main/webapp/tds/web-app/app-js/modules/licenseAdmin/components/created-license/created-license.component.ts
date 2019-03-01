// Angular
import {Component} from '@angular/core';
// Service
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
// Model
import {RequestLicenseModel} from '../../model/license.model';

@Component({
	selector: 'tds-license-created',
	templateUrl: 'created-license.component.html'
})
export class CreatedLicenseComponent {

	constructor(
		public requestLicenseModel: RequestLicenseModel,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService) {
	}

	/**
	 * Close the Dialog
	 */
	public cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}
}
