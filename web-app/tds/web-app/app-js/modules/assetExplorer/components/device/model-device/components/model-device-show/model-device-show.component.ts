import { Component, ViewChild, ElementRef } from '@angular/core';
import {UIDialogService, UIExtraDialog} from '../../../../../../../shared/services/ui-dialog.service';
import {DeviceModel, DeviceModelDetails} from '../../model/device-model.model';
import {AssetExplorerService} from '../../../../../service/asset-explorer.service';

@Component({
	selector: 'model-device-show',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/device/model-device/components/model-device-show/model-device-show.component.html'
})
export class ModelDeviceShowComponent extends UIExtraDialog {
	constructor(
		private dialogService: UIDialogService,
		public deviceModel: DeviceModel,
		private assetExplorerService: AssetExplorerService) {
		super('#model-device-show-component');
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

	onEditModel(): void {
		console.log('On edit model');
	}

}