import { Component } from '@angular/core';
import { UIExtraDialog } from '../../../../../../../shared/services/ui-dialog.service';
import { UIPromptService } from '../../../../../../../shared/directives/ui-prompt.directive';
import { DeviceManufacturer } from '../../model/device-manufacturer.model';

@Component({
	selector: 'device-manufacturer-edit',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/device/manufacturer/components/manufacturer-edit/manufacturer-edit.component.html'
})
export class ManufacturerEditComponent extends UIExtraDialog {
	isEditing: boolean;
	constructor(
		public deviceManufacturer: DeviceManufacturer,
		private prompt: UIPromptService) {
		super('#device-manufacturer-edit-component');
		this.isEditing = false;
	}

	/***
	 * Close the Active Dialog
	 */
	protected cancelCloseDialog(): void {
		this.dismiss();
	}

	/**
	 * On EscKey Pressed close the dialog.
	*/
	onEscKeyPressed(): void {
		this.cancelCloseDialog();
	}

	public onUpdateManufacturer(): void {
		this.isEditing = false;
	}

	public onAkaChange(model: any): void {
		console.log('Sending changes from aka component');
		console.log(model);
		console.log('-----------------------');
		this.isEditing = true;
	}

	public onChange(event: any): void {
		this.isEditing = true;
	}

	public onDeleteManufacturer() {
		this.prompt.open(
			'Confirmation Required',
			'Are you sure?',
			'Ok', 'Cancel').then(result => {
			if (result) {
				alert('deleting...');
			} else {
				alert('canceling...');
			}
		});
	}
}