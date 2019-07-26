import { Component } from '@angular/core';
import {UIDialogService, UIExtraDialog} from '../../../../../../../shared/services/ui-dialog.service';
import {DeviceModel} from '../../model/device-model.model';
import {AssetExplorerService} from '../../../../../../assetManager/service/asset-explorer.service';
import {Permission} from '../../../../../../../shared/model/permission.model';
import {PermissionService} from '../../../../../../../shared/services/permission.service';
import {TDSModalHtmlWrapperComponent} from '../../../../../../../shared/components/modal-html-wrapper/modal-html-wrapper.component';
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

		this.dialogService.extra(TDSModalHtmlWrapperComponent,
					[
						{provide: 'title', useValue: 'Edit Model'},
						{provide: 'html', useValue: ''}
					], false, false)
				.then((result) => {
					this.close(result);
				}).catch((error) => console.log(error));

		/*
		this.modelService.editModel(this.deviceModel.id)
			.subscribe((response) => {
				console.log(response);
				this.dialogService.extra(TDSModalHtmlWrapperComponent,
					[
						{provide: 'title', useValue: 'Edit Model'},
						{provide: 'html', useValue: response}
					], false, false)
				.then((result) => {
					this.close(result);
				}).catch((error) => console.log(error))
			});
		*/

		// fix issue preventing submit form from bootstrap modal
		// document.getElementById('editModelForm')['submit']();
		// this.close();
	}

}