import { Component, ViewChild, ElementRef, OnInit } from '@angular/core';
import {UIDialogService, UIExtraDialog} from '../../../../../../../shared/services/ui-dialog.service';
import { DeviceManufacturer} from '../../model/device-manufacturer.model';
import { ManufacturerEditComponent } from '../manufacturer-edit/manufacturer-edit.component';
import {Aka} from '../../../../../../../shared/components/aka/model/aka.model';

@Component({
	selector: 'device-manufacturer',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/device/manufacturer/components/manufacturer-show/manufacturer-show.component.html'
})
export class ManufacturerShowComponent extends UIExtraDialog implements OnInit {
	aka: string;
	constructor(
		private dialogService: UIDialogService,
		public deviceManufacturer: DeviceManufacturer) {
		super('#device-manufacturer-component');
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

	protected onUpdateComment(): void {
		console.log('Updating component');
	}

	ngOnInit() {
		console.log('On Edit');
	}

	onEditManufacturer(): void {
		this.dialogService.extra(ManufacturerEditComponent,
			[
				{
					provide: DeviceManufacturer,
					useValue: this.deviceManufacturer
				}
			])
			.then((result) => {
				this.close(result);
			}).catch((error) => console.log(error));
	}
}