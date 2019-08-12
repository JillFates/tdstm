import { Component } from '@angular/core';
import { UIExtraDialog } from '../../../../../../../shared/services/ui-dialog.service';
import { UIPromptService } from '../../../../../../../shared/directives/ui-prompt.directive';

@Component({
	selector: 'model-device-edit',
	templateUrl: 'model-device-edit.component.html'
})
export class ModelDeviceEditComponent extends UIExtraDialog {
	constructor(private prompt: UIPromptService) {
		super('#model-device-edit-component');
	}

	/***
	 * Close the Active Dialog
	 */
	public cancelCloseDialog(): void {
		this.dismiss();
	}

	/**
	 * On EscKey Pressed close the dialog.
	 */
	onEscKeyPressed(): void {
		this.cancelCloseDialog();
	}

}