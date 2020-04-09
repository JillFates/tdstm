import {Component} from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {DependecyService} from '../../service/dependecy.service';
import {AssetEditComponent} from '../asset/asset-edit.component';
import {DOMAIN, DIALOG_SIZE} from '../../../../shared/model/constants';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AssetModalModel} from '../../model/asset-modal.model';
import {AssetCloneComponent} from '../asset-clone/asset-clone.component';
import {CloneCLoseModel} from '../../model/clone-close.model';
import {AssetCommonShow} from '../asset/asset-common-show';
import {WindowService} from '../../../../shared/services/window.service';
import {UserContextService} from '../../../auth/service/user-context.service';

export function StorageShowComponent(template, modelId: number, metadata: any) {
	@Component({
		selector: `tds-storage-show`,
		template: template
	})
	class StorageShowComponent extends AssetCommonShow {
		constructor(
			activeDialog: UIActiveDialogService,
			dialogService: UIDialogService,
			assetService: DependecyService,
			prompt: UIPromptService,
			assetExplorerService: AssetExplorerService,
			notifierService: NotifierService,
			userContextService: UserContextService,
			windowService: WindowService) {
				super(activeDialog, dialogService, assetService, prompt, assetExplorerService, notifierService, userContextService, windowService, metadata);
				this.mainAsset = modelId;
				this.assetType = DOMAIN.STORAGE;
				this.assetTags = metadata.assetTags;
		}

		/**
		 * Allows to clone an application asset
		 */
		onCloneAsset(): void {

			const cloneModalModel: AssetModalModel = {
				assetType: DOMAIN.STORAGE,
				assetId: this.mainAsset
			}
			this.dialogService.extra(AssetCloneComponent, [
				{provide: AssetModalModel, useValue: cloneModalModel}
			], false, false).then( (result: CloneCLoseModel)  => {

				if (result.clonedAsset && result.showEditView) {
					const componentParameters = [
						{ provide: 'ID', useValue: result.assetId },
						{ provide: 'ASSET', useValue: DOMAIN.STORAGE }
					];

					this.dialogService
						.replace(AssetEditComponent, componentParameters, DIALOG_SIZE.XLG);
				} else if (!result.clonedAsset && result.showView) {
					this.showAssetDetailView(DOMAIN.STORAGE, result.assetId);
				}
			})
				.catch( error => console.log('error', error));
		}

	}
	return StorageShowComponent;
}
