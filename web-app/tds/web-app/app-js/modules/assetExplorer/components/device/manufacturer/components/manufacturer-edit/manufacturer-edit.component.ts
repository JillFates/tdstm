import { Component } from '@angular/core';
import { UIExtraDialog } from '../../../../../../../shared/services/ui-dialog.service';
import { UIPromptService } from '../../../../../../../shared/directives/ui-prompt.directive';
import { DeviceManufacturer } from '../../model/device-manufacturer.model';
import {AkaChanges} from '../../../../../../../shared/components/aka/model/aka.model';
import {ManufacturerService} from '../../../../../service/manufacturer.service';

@Component({
	selector: 'device-manufacturer-edit',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/device/manufacturer/components/manufacturer-edit/manufacturer-edit.component.html'
})
export class ManufacturerEditComponent extends UIExtraDialog {
	isEditing: boolean;
	hasAkaValidationErrors: boolean;
	constructor(
		public deviceManufacturer: DeviceManufacturer,
		private manufacturerService: ManufacturerService,
		private prompt: UIPromptService) {
		super('#device-manufacturer-edit-component');
		this.isEditing = false;
		this.hasAkaValidationErrors = false;
	}

	/***
	 * Close the Active Dialog
	 */
	protected cancelCloseDialog(): void {
		this.close(null);
	}

	/**
	 * On EscKey Pressed close the dialog.
	*/
	onEscKeyPressed(): void {
		this.close(null);
	}

	public onUpdateManufacturer(): void {
		this.isEditing = false;
		this.manufacturerService.updateManufacturer(this.deviceManufacturer)
			.subscribe((result) => {
				this.close(this.deviceManufacturer);

			}, err => {
				console.log('There is an error updating');
				console.log(err);
				console.log('-----');
			})
	}

	public onAkaChange(akaChanges: AkaChanges): void {
		if (this.deviceManufacturer.akaChanges) {
			this.isEditing = true;
		}
		this.deviceManufacturer.akaChanges = akaChanges;
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
	onValidationErrors(hasAkaValidationErrors: boolean): void {
		this.hasAkaValidationErrors = hasAkaValidationErrors;
		console.log('Setting the value');
		console.log(this.hasAkaValidationErrors);
	}
}