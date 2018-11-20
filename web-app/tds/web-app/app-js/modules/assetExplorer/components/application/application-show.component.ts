import { Component} from '@angular/core';
import { UIActiveDialogService, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { DependecyService } from '../../service/dependecy.service';
import { AssetEditComponent } from '../asset/asset-edit.component';
import { DOMAIN, DIALOG_SIZE } from '../../../../shared/model/constants';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AssetCloneComponent} from '../asset-clone/asset-clone.component';
import {CloneCLoseModel} from '../../model/clone-close.model';
import {AssetModalModel} from '../../model/asset-modal.model';
import {AssetCommonShow} from '../asset/asset-common-show';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {AssetCommonHelper} from '../asset/asset-common-helper';

declare var jQuery: any;

export function ApplicationShowComponent(template, modelId: number, metadata: any) {
	@Component({
		selector: `tds-application-show`,
		template: template
	})
	class ApplicationShowComponent extends AssetCommonShow {

		constructor(
			activeDialog: UIActiveDialogService,
			dialogService: UIDialogService,
			assetService: DependecyService,
			prompt: UIPromptService,
			assetExplorerService: AssetExplorerService,
			notifierService: NotifierService,
			preferenceService: PreferenceService) {
				super(activeDialog, dialogService, assetService, prompt, assetExplorerService, notifierService, preferenceService);
				this.mainAsset = modelId;
				this.assetTags = metadata.assetTags;
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
		 * Allows to clone an application asset
		 */
		onCloneAsset(): void {

			const cloneModalModel: AssetModalModel = {
				assetType: DOMAIN.APPLICATION,
				assetId: this.mainAsset
			}
			this.dialogService.extra(AssetCloneComponent, [
				{provide: AssetModalModel, useValue: cloneModalModel}
			], false, false).then( (result: CloneCLoseModel)  => {

				if (result.clonedAsset && result.showEditView) {
					const componentParameters = [
						{ provide: 'ID', useValue: result.assetId },
						{ provide: 'ASSET', useValue: DOMAIN.APPLICATION }
					];

					this.dialogService
						.replace(AssetEditComponent, componentParameters, DIALOG_SIZE.XLG);
				} else if (!result.clonedAsset && result.showView) {
					this.showAssetDetailView(DOMAIN.APPLICATION, result.assetId);
				}
			})
				.catch( error => console.log('error', error));
		}

	}
	return ApplicationShowComponent;
}