import { Component } from '@angular/core';
import {UIDialogService, UIExtraDialog} from '../../../../../../../shared/services/ui-dialog.service';
import {DeviceModel} from '../../model/device-model.model';
import {AssetExplorerService} from '../../../../../../assetManager/service/asset-explorer.service';
import {Permission} from '../../../../../../../shared/model/permission.model';
import {PermissionService} from '../../../../../../../shared/services/permission.service';
import {TDSModalPageWrapperComponent} from '../../../../../../../shared/components/modal-page-wrapper/modal-page-wrapper.component';
import {ModelService} from '../../../../../service/model.service';

@Component({
	selector: 'model-device-show',
	templateUrl: 'model-device-show.component.html'
})
export class ModelDeviceShowComponent extends UIExtraDialog {
	constructor(
		private dialogService: UIDialogService,
		public deviceModel: DeviceModel,
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

		this.dialogService.extra(TDSModalPageWrapperComponent,
				[
					{provide: 'title', useValue: 'Edit Model'},
					{provide: 'action', useValue: this.getEditModelUrl()}
				], false, false)
			.then((result) => {
				console.log(result);
			}).catch((error) => console.log(error));
	}

	/**
	 * get the url for the model edit view
	*/
	private getEditModelUrl(): string {
		return `/tdstm/model/edit?id=${this.deviceModel.id}&modal=true`;
	}
}