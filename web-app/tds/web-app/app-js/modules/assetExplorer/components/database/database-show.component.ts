import { Component, OnInit, HostListener } from '@angular/core';
import { UIActiveDialogService, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { AssetShowComponent } from '../asset/asset-show.component';
import { AssetEditComponent } from '../asset/asset-edit.component';
import { AssetDependencyComponent } from '../asset-dependency/asset-dependency.component';
import { DependecyService } from '../../service/dependecy.service';
import {DIALOG_SIZE, DOMAIN, KEYSTROKE} from '../../../../shared/model/constants';

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

		@HostListener('keydown', ['$event']) handleKeyboardEvent(event: KeyboardEvent) {
			if (event && event.code === KEYSTROKE.ESCAPE) {
				this.cancelCloseDialog();
			}
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

		showAssetEditView() {
			this.dialogService.replace(AssetEditComponent, [
				{ provide: 'ID', useValue: this.mainAsset },
				{ provide: 'ASSET', useValue: DOMAIN.DATABASE }],
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

	}
	return DatabaseShowComponent;
}