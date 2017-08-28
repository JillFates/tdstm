import { Component } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { ViewModel } from '../../model/view.model';
import { AssetExplorerService } from '../../service/asset-explorer.service';

@Component({
	selector: 'asset-explorer-view-save',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-save/asset-explorer-view-save.component.html'
})
export class AssetExplorerViewSaveComponent {
	model: ViewModel;

	constructor(
		model: ViewModel,
		private assetExpService: AssetExplorerService,
		public activeDialog: UIActiveDialogService,
		private permissionService: PermissionService) {

		this.model = { ...model };
		if (this.model.id) {
			this.model.name = `Copy of ${this.model.name}`;
			this.model.id = null;
			this.model.isSystem = false;
		}
		if (this.model.isSystem) {
			this.model.isShared = true;
		}
	}

	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

	protected confirmCloseDialog() {
		this.assetExpService.saveReport(this.model)
			.subscribe(result => this.activeDialog.close(result),
			error => this.activeDialog.dismiss(error));
	}

	protected isValid(): boolean {
		return this.model.name && this.model.name.trim() !== '';
	}
}