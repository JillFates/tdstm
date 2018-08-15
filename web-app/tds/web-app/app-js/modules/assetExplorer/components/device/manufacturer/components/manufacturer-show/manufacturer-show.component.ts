import { Component, ViewChild, ElementRef, OnInit } from '@angular/core';
import {UIDialogService, UIExtraDialog} from '../../../../../../../shared/services/ui-dialog.service';
import { DeviceManufacturer} from '../../model/device-manufacturer.model';
import {Aka} from '../../../../../../../shared/components/aka/model/aka.model';
import {DIALOG_SIZE} from '../../../../../../../shared/model/constants';

@Component({
	selector: 'device-manufacturer',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/device/manufacturer/components/manufacturer-show/manufacturer-show.component.html'
})
export class ManufacturerShowComponent extends UIExtraDialog implements OnInit {
	aka: string;
	alias: string;
	constructor(
		private dialogService: UIDialogService,
		public deviceManufacturer: DeviceManufacturer) {
		super('#device-manufacturer-component');
		this.alias = this.deviceManufacturer.aka.toString();
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
		const aka = (this.deviceManufacturer.aka || [])
			.map((aka: Aka) => aka.value);

		if (aka.length) {
			this.aka = aka.join(',');
		}
	}

	onEditManufacturer(): void {
		console.log('On Edit manufacturer');
	}
}