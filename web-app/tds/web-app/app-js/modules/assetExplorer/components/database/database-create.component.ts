/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import {Component, Inject, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import * as R from 'ramda';
import {TagService} from '../../../assetTags/service/tag.service';
import {AssetCommonEdit} from '../asset/asset-common-edit';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {ASSET_ENTITY_DIALOG_TYPES} from '../../model/asset-entity.model';

declare var jQuery: any;

export function DatabaseCreateComponent(template, model: any, metadata: any) {

	@Component({
		selector: `tds-database-create`,
		template: template,
		providers: [
			{ provide: 'model', useValue: model }
		]
	}) class DatabaseCreateComponent extends AssetCommonEdit {
		@ViewChild('form') form: NgForm;

		constructor(
			@Inject('model') model: any,
			activeDialog: UIActiveDialogService,
			preference: PreferenceService,
			assetExplorerService: AssetExplorerService,
			dialogService: UIDialogService,
			notifierService: NotifierService,
			tagService: TagService,
			promptService: UIPromptService) {

			super(model, activeDialog, preference, assetExplorerService, dialogService, notifierService, tagService, metadata, promptService);

			this.model.asset.retireDate = null;
			this.model.asset.maintExpDate = null;

			this.model.asset.moveBundle = this.model.dependencyMap.moveBundleList[0];
			this.model.asset.planStatus = this.model.planStatusOptions[0];
		}

		/**
		 * Prepare te model and format all pending changes
		 */
		public onCreate(): void {
			if (!this.form.form.valid) {
				return;
			}

			let modelRequest = R.clone(this.model);

			// Scale Format
			modelRequest.asset.scale = (modelRequest.asset.scale && modelRequest.asset.scale.value) ? modelRequest.asset.scale.value : modelRequest.asset.scale;

			// MoveBundle
			modelRequest.asset.moveBundleId = modelRequest.asset.moveBundle.id;

			// AssetClass
			modelRequest.asset.assetClass = {
				name: ASSET_ENTITY_DIALOG_TYPES.DATABASE
			};
			this.model.asset.assetClass = modelRequest.asset.assetClass;

			this.assetExplorerService.createAsset(modelRequest).subscribe((result) => {
				this.notifierService.broadcast({
					name: 'reloadCurrentAssetList'
				});
				if (result.id && !isNaN(result.id) && result.id > 0) {
					this.createTags(result.id);
				}
			});
		}
	}

	return DatabaseCreateComponent;
}