import { Component } from '@angular/core';
import {UIDialogService, UIExtraDialog} from '../../../../../../../shared/services/ui-dialog.service';
import {DeviceModel} from '../../model/device-model.model';
import {AssetExplorerService} from '../../../../../service/asset-explorer.service';
import {Permission} from "../../../../../../../shared/model/permission.model";
import {PermissionService} from "../../../../../../../shared/services/permission.service";

@Component({
	selector: 'model-device-show',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/device/model-device/components/model-device-show/model-device-show.component.html'
})
export class ModelDeviceShowComponent extends UIExtraDialog {
	constructor(
		private dialogService: UIDialogService,
		public deviceModel: DeviceModel,
		private assetExplorerService: AssetExplorerService,
		private permissionService: PermissionService) {
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

	/**
	 * Determine user has permissions to edit model
	 */
	canEditModel(): boolean {
		return this.permissionService.hasPermission(Permission.ModelEdit)
	}

	onEditModel(): void {
		if (!this.canEditModel()) {
			return;
		}

		// fix issue preventing submit form from bootstrap modal
		document.getElementById('editModelForm')['submit']();
		this.close();
	}

}