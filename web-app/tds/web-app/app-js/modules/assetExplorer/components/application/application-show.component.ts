import { Component, OnInit } from '@angular/core';
import { UIActiveDialogService, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { AssetShowComponent } from '../asset/asset-show.component';
import { AssetDependencyComponent } from '../asset-dependency/asset-dependency.component';
import { DependecyService } from '../../service/dependecy.service';
import { AssetEditComponent } from '../asset/asset-edit.component';
import { DOMAIN, DIALOG_SIZE } from '../../../../shared/model/constants';
import {TagModel} from '../../../assetTags/model/tag.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AssetCloneComponent} from '../asset-clone/asset-clone.component';
import {CloneCLoseModel} from '../../model/clone-close.model';
import {CloneModalModel} from '../../model/clone-modal.model';

declare var jQuery: any;

export function ApplicationShowComponent(template, modelId: number, metadata: any) {
	@Component({
		selector: `tds-application-show`,
		template: template
	}) class ApplicationShowComponent implements OnInit {
		mainAsset = modelId;
		protected assetTags: Array<TagModel> = metadata.assetTags;

		constructor(
			private activeDialog: UIActiveDialogService,
			private dialogService: UIDialogService,
			private assetService: DependecyService,
			private prompt: UIPromptService,
			private assetsExplorerService: AssetExplorerService,
			private notifierService: NotifierService) {

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
				DIALOG_SIZE.LG);
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
				.replace(AssetEditComponent, componentParameters, DIALOG_SIZE.LG);
		}

		/**
			allows to delete the application assets
		 */
		onDeleteAsset() {

			this.prompt.open('Confirmation Required',
				'You are about to delete selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel',
				'OK', 'Cancel')
				.then( success => {
					if (success) {
						this.assetsExplorerService.deleteAssets([this.mainAsset.toString()]).subscribe( res => {
							if (res) {
								this.notifierService.broadcast({
									name: 'reloadCurrentAssetList'
								});
								this.activeDialog.dismiss();
							}
						}, (error) => console.log(error));
					}
				})
				.catch((error) => console.log(error));
		}

		/**
		 * Allows to clone an application asset
		 */
		onCloneAsset(): void {

			const cloneModalModel: CloneModalModel = {
				assetType: DOMAIN.APPLICATION,
				assetId: this.mainAsset
			}
			this.dialogService.extra(AssetCloneComponent, [
				{provide: CloneModalModel, useValue: cloneModalModel}
			], false, false).then( (result: CloneCLoseModel)  => {

				if (result.clonedAsset && result.showEditView) {
					const componentParameters = [
						{ provide: 'ID', useValue: result.assetId },
						{ provide: 'ASSET', useValue: DOMAIN.APPLICATION }
					];

					this.dialogService
						.replace(AssetEditComponent, componentParameters, DIALOG_SIZE.XLG);
				}
			})
				.catch( error => console.log('error', error));
		}

		getGraphUrl(): string {
			return `/tdstm/assetEntity/architectureViewer?assetId=${this.mainAsset}&level=2`;
		}

	}
	return ApplicationShowComponent;
}