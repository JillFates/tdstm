import { Component, Inject } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { StateService } from '@uirouter/angular';
import { AssetExplorerStates } from '../../asset-explorer-routing.states';

import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { ViewModel } from '../../model/view.model';
import { AssetExplorerService } from '../../service/asset-explorer.service';
import { Permission } from '../../../../shared/model/permission.model';

@Component({
	selector: 'asset-explorer-view-show',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-show/asset-explorer-view-show.component.html'
})
export class AssetExplorerViewShowComponent {
	model: ViewModel;
	data = [];

	constructor(
		@Inject('report') report: Observable<ViewModel>,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private assetService: AssetExplorerService,
		private stateService: StateService) {
		report.subscribe(
			(result) => {
				this.model = result;
			},
			(err) => console.log(err));
	}

	protected onQuery(justPlanning, page, offset): void {
		console.log(justPlanning);
		this.assetService.query(this.model.id, {
			offset: page,
			limit: offset,
			sortDomain: this.model.schema.sort.domain,
			sortField: this.model.schema.sort.property,
			sortOrder: this.model.schema.sort.order,
			justPlanning: justPlanning,
			filters: {
				domains: this.model.schema.domains,
				columns: this.model.schema.columns
			}
		}).subscribe(result => {
			console.log(result);
		}, err => console.log(err));
	}

	protected onEdit(): void {
		this.stateService.go(AssetExplorerStates.REPORT_EDIT.name, { id: this.model.id });
	}

	protected isSaveAsAvailable(): boolean {
		return this.model.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs) :
			this.permissionService.hasPermission(Permission.AssetExplorerSaveAs);
	}

	protected isEditAvailable(): boolean {
		return this.model.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit) :
			this.permissionService.hasPermission(Permission.AssetExplorerEdit);
	}

}