import {Component} from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {AssetEditComponent} from '../asset/asset-edit.component';
import {DependecyService} from '../../service/dependecy.service';
import {DIALOG_SIZE, DOMAIN} from '../../../../shared/model/constants';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AssetModalModel} from '../../model/asset-modal.model';
import {AssetCloneComponent} from '../asset-clone/asset-clone.component';
import {CloneCLoseModel} from '../../model/clone-close.model';
import {AssetCommonShow} from '../asset/asset-common-show';
import {WindowService} from '../../../../shared/services/window.service';
import {UserContextService} from '../../../auth/service/user-context.service';

export function DatabaseShowComponent(template, modelId: number, metadata: any) {
	@Component({
		selector: `tds-database-show`,
		template: template
	})
	class DatabaseShowComponent extends AssetCommonShow {
		constructor(
			activeDialog: UIActiveDialogService,
			dialogService: UIDialogService,
			assetService: DependecyService,
			prompt: UIPromptService,
			assetExplorerService: AssetExplorerService,
			notifierService: NotifierService,
			userContextService: UserContextService,
			windowService: WindowService) {
				super(activeDialog, dialogService, assetService, prompt, assetExplorerService, notifierService, userContextService, windowService);
				this.mainAsset = modelId;
				this.assetTags = metadata.assetTags;
		}

		showAssetEditView() {
			this.dialogService.replace(AssetEditComponent, [
					{ provide: 'ID', useValue: this.mainAsset },
					{ provide: 'ASSET', useValue: DOMAIN.DATABASE }],
				DIALOG_SIZE.LG);
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

	}
	return DatabaseShowComponent;
}