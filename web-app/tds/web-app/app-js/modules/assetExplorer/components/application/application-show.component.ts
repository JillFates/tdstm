import { Component, OnInit } from '@angular/core';
import { UIActiveDialogService, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { AssetShowComponent } from '../asset/asset-show.component';
import { AssetDependencyComponent } from '../asset-dependency/asset-dependency.component';
import { DependecyService } from '../../service/dependecy.service';
import { AssetEditComponent } from '../asset/asset-edit.component';
import { DOMAIN, DIALOG_SIZE } from '../../../../shared/model/constants';

declare var jQuery: any;

export function ApplicationShowComponent(template, modelId: number) {
	@Component({
		selector: `application-show`,
		template: template
	}) class ApplicationShowComponent implements OnInit {
		mainAsset = modelId;

		constructor(private activeDialog: UIActiveDialogService, private dialogService: UIDialogService, private assetService: DependecyService) {

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
				DIALOG_SIZE.XLG);
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

		showAssetEditView(): Promise<any> {
			const componentParameters = [
				{ provide: 'ID', useValue: this.mainAsset },
				{ provide: 'ASSET', useValue: DOMAIN.APPLICATION }
			];

			return this.dialogService
				.replace(AssetEditComponent, componentParameters, DIALOG_SIZE.XLG);
		}

	}
	return ApplicationShowComponent;
}