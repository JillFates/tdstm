// Angular
import {Component, ComponentFactoryResolver, Inject, OnInit} from '@angular/core';
// Model
import {ASSET_ENTITY_DIALOG_TYPES} from '../../model/asset-entity.model';
// Service
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {TagService} from '../../../assetTags/service/tag.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {DialogService} from 'tds-component-library';
// Component
import {AssetCommonEdit} from '../asset/asset-common-edit';
// Other
import * as R from 'ramda';

export function StorageCreateComponent(template: string, model: any, metadata: any, parentDialog: any): any {
	@Component({
		selector: 'tds-storage-create',
		template: template,
		providers: [
			{ provide: 'model', useValue: model }
		]
	})
	class StorageCreateComponent extends AssetCommonEdit implements OnInit {
		constructor(
			@Inject('model') model: any,
			componentFactoryResolver: ComponentFactoryResolver,
			userContextService: UserContextService,
			permissionService: PermissionService,
			assetExplorerService: AssetExplorerService,
			dialogService: DialogService,
			notifierService: NotifierService,
			tagService: TagService,
			translatePipe: TranslatePipe
		) {
			super(componentFactoryResolver, model, userContextService, permissionService, assetExplorerService, dialogService, notifierService, tagService, metadata, translatePipe, parentDialog);
		}

		ngOnInit() {
			this.initModel();
			this.focusControlByName('assetName');
			this.onFocusOutOfCancel();
		}

		/**
		 * Init model with necessary changes to support UI components.
		 */
		private initModel(): void {
			this.setDefaultBundleAndCompany();
			this.model.asset.planStatus = this.model.planStatusOptions.find((plan: string) => plan === this.defaultPlanStatus);
			this.model.asset.validation =  this.defaultValidation;
			this.model.asset.environment = '';
			this.model.asset.scale = null;
		}

		/**
		 * Prepare te model and format all pending changes
		 */
		public onCreate(): void {
			let modelRequest = R.clone(this.model);

			// Scale Format
			modelRequest.asset.scale = (modelRequest.asset.scale && modelRequest.asset.scale.value) ?
				modelRequest.asset.scale.value : modelRequest.asset.scale;

			// MoveBundle
			modelRequest.asset.moveBundleId = modelRequest.asset.moveBundle.id;

			// AssetClass
			modelRequest.asset.assetClass = {
				name: ASSET_ENTITY_DIALOG_TYPES.STORAGE
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

	return StorageCreateComponent;
}
