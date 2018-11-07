import { Component, OnInit, HostListener } from '@angular/core';
import { UIActiveDialogService, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { AssetShowComponent } from '../asset/asset-show.component';
import { AssetEditComponent } from '../asset/asset-edit.component';
import { AssetDependencyComponent } from '../asset-dependency/asset-dependency.component';
import { DependecyService } from '../../service/dependecy.service';
import {DIALOG_SIZE, DOMAIN, KEYSTROKE} from '../../../../shared/model/constants';
import {TagModel} from '../../../assetTags/model/tag.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AssetModalModel} from '../../model/asset-modal.model';
import {AssetCloneComponent} from '../asset-clone/asset-clone.component';
import {CloneCLoseModel} from '../../model/clone-close.model';
import {AssetCommonHelper} from '../asset/asset-common-helper';

declare var jQuery: any;

export function DatabaseShowComponent(template, modelId: number, metadata: any) {
	@Component({
		selector: `tds-database-show`,
		template: template
	}) class DatabaseShowComponent implements OnInit {
		private mainAsset = modelId;
		protected assetTags: Array<TagModel> = metadata.assetTags;
		protected isHighField = AssetCommonHelper.isHighField;

		constructor(
			private activeDialog: UIActiveDialogService,
			private dialogService: UIDialogService,
			private assetService: DependecyService,
			private prompt: UIPromptService,
			private assetExplorerService: AssetExplorerService,
			private notifierService: NotifierService) {
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
				DIALOG_SIZE.LG);
		}

		showAssetEditView() {
			this.dialogService.replace(AssetEditComponent, [
				{ provide: 'ID', useValue: this.mainAsset },
				{ provide: 'ASSET', useValue: DOMAIN.DATABASE }],
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

		/**
		 allows to delete the application assets
		 */
		onDeleteAsset() {

			this.prompt.open('Confirmation Required',
				'You are about to delete the selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel',
				'OK', 'Cancel')
				.then( success => {
					if (success) {
						this.assetExplorerService.deleteAssets([this.mainAsset.toString()]).subscribe( res => {
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

			const cloneModalModel: AssetModalModel = {
				assetType: DOMAIN.DATABASE,
				assetId: this.mainAsset
			}
			this.dialogService.extra(AssetCloneComponent, [
				{provide: AssetModalModel, useValue: cloneModalModel}
			], false, false).then( (result: CloneCLoseModel)  => {

				if (result.clonedAsset && result.showEditView) {
					const componentParameters = [
						{ provide: 'ID', useValue: result.assetId },
						{ provide: 'ASSET', useValue: DOMAIN.DATABASE }
					];

					this.dialogService
						.replace(AssetEditComponent, componentParameters, DIALOG_SIZE.XLG);
				} else if (!result.clonedAsset && result.showView) {
					this.showAssetDetailView(DOMAIN.DATABASE, result.assetId);
				}
			})
				.catch( error => console.log('error', error));
		}

		getGraphUrl(): string {
			return `/tdstm/assetEntity/architectureViewer?assetId=${this.mainAsset}&level=2`;
		}

	}
	return DatabaseShowComponent;
}