import { Component } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { ViewModel } from '../../model/view.model';

@Component({
	selector: 'asset-explorer-view-export',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-export/asset-explorer-view-export.component.html',
	styles: [`
		.has-error,.has-error:focus { border: 1px #f00 solid;}
	`]
})
export class AssetExplorerViewExportComponent {
	private columns: any[];
	protected fileName = 'asset_explorer';
	protected dataToExport = [];

	constructor(
		model: ViewModel,
		public activeDialog: UIActiveDialogService,
		private permissionService: PermissionService) {

		let configuredColumns = { ...model.schema.columns } ;

		this.columns = Object.keys(configuredColumns).map( key => configuredColumns[key]['label']);

		console.log(this.columns);
	}

	cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

}