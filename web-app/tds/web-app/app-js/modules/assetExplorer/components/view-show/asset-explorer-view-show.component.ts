import { Component, Inject } from '@angular/core';
import { Observable } from 'rxjs/Rx';

import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { ViewModel } from '../../model/view.model';

@Component({
	selector: 'asset-explorer-view-show',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-show/asset-explorer-view-show.component.html'
})
export class AssetExplorerViewShowComponent {
	model: ViewModel;

	constructor(
		@Inject('report') report: Observable<ViewModel>,
		private dialogService: UIDialogService,
		private permissionService: PermissionService) {
		report.subscribe(
			(result) => {
				this.model = result;
			},
			(err) => console.log(err));
	}
}