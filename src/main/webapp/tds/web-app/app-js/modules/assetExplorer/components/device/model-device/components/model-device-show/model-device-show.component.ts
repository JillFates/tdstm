import { Component } from '@angular/core';
import {UIDialogService, UIExtraDialog} from '../../../../../../../shared/services/ui-dialog.service';
import {DeviceModel} from '../../model/device-model.model';
import {AssetExplorerService} from '../../../../../../assetManager/service/asset-explorer.service';
import {Permission} from '../../../../../../../shared/model/permission.model';
import {PermissionService} from '../../../../../../../shared/services/permission.service';
import {ModelService} from '../../../../../service/model.service';
import {ModelDeviceEditComponent} from '../model-device-edit/model-device-edit.component';
import {DeviceManufacturer} from '../../../manufacturer/model/device-manufacturer.model';

@Component({
	selector: 'model-device-show',
	templateUrl: 'model-device-show.component.html'
})
export class ModelDeviceShowComponent extends UIExtraDialog {
	constructor(
		private dialogService: UIDialogService,
		public deviceModel: DeviceModel,
		public deviceManufacturer: DeviceManufacturer,
		private assetExplorerService: AssetExplorerService,
		private permissionService: PermissionService,
		private modelService: ModelService) {
		super('#model-device-show-component');
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

	/**
	 * On update comment
	 */
	protected onUpdateComment(): void {
		console.log('Updating component');
	}

	/**
	 * Determine if user has permissions to edit model
	 */
	canEditModel(): boolean {
		return this.permissionService.hasPermission(Permission.ModelEdit)
	}

	/**
	 * On click Edit button
	 */
	onEditModel(): void {
		if (!this.canEditModel()) {
			return;
		}

		this.dialogService.extra(ModelDeviceEditComponent,
				[], true, false)
			.then((result) => {
				console.log(result);
			}).catch((error) => console.log(error));
	}
}