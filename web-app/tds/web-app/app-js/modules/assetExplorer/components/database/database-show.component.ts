import { Component, OnInit } from '@angular/core';
import { UIActiveDialogService, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { AssetShowComponent } from '../asset/asset-show.component';
import { AssetEditComponent } from '../asset/asset-edit.component';
import { AssetDependencyComponent } from '../asset-dependency/asset-dependency.component';
import { DependecyService } from '../../service/dependecy.service';
import {DOMAIN} from '../../../../shared/model/constants';

declare var jQuery: any;

export function DatabaseShowComponent(template, modelId: number) {
	@Component({
		selector: `database-show`,
		template: template
	}) class DatabaseShowComponent implements OnInit {
		private mainAsset = modelId;

		constructor(private activeDialog: UIActiveDialogService, private dialogService: UIDialogService, private assetService: DependecyService) {
			jQuery('[data-toggle="popover"]').popover();
		}

		/**
		 * Initiates The Injected Component
		 */
		ngOnInit(): void {
			jQuery('[data-toggle="popover"]').popover();
		}

		cancelCloseDialog(): void {
			this.activeDialog.dismiss();
		}

		showAssetDetailView(assetClass: string, id: number) {
			this.dialogService.replace(AssetShowComponent, [
					{ provide: 'ID', useValue: id },
					{ provide: 'ASSET', useValue: assetClass }],
				'lg');
		}

		showAssetEditView() {
			this.dialogService.replace(AssetEditComponent, [
				{ provide: 'ID', useValue: this.mainAsset },
				{ provide: 'ASSET', useValue: DOMAIN.DATABASE }],
				'xlg');
		}

		showDependencyView(assetId: number, dependencyAsset: number) {
			this.assetService.getDependencies(assetId, dependencyAsset)
				.subscribe((result) => {
					this.dialogService.extra(AssetDependencyComponent, [
						{ provide: 'ASSET_DEP_MODEL', useValue: result }])
						.then(res => console.log(res))
						.catch(res => console.log(res));
				}, (error) => console.log(error));
		}

	}
	return DatabaseShowComponent;
}