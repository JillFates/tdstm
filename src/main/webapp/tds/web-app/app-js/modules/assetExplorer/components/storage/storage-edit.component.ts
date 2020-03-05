/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import {Component, ComponentFactoryResolver, Inject, OnInit} from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import * as R from 'ramda';
import {TagService} from '../../../assetTags/service/tag.service';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {AssetCommonEdit} from '../asset/asset-common-edit';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {UserContextService} from '../../../auth/service/user-context.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {DialogService} from 'tds-component-library';

export function StorageEditComponent(template: string, editModel: any, metadata: any, parentDialog: any): any {
	@Component({
		selector: 'tds-storage-edit',
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	})
	class StorageShowComponent extends AssetCommonEdit implements OnInit {
		constructor(
			@Inject('model') model: any,
			componentFactoryResolver: ComponentFactoryResolver,
			activeDialog: UIActiveDialogService,
			userContextService: UserContextService,
			permissionService: PermissionService,
			assetExplorerService: AssetExplorerService,
			dialogService: DialogService,
			oldDialogService: UIDialogService,
			notifierService: NotifierService,
			tagService: TagService,
			promptService: UIPromptService,
			translatePipe: TranslatePipe) {

			super(componentFactoryResolver, model, activeDialog, userContextService, permissionService, assetExplorerService, dialogService, oldDialogService, notifierService, tagService, metadata, promptService, translatePipe, parentDialog);
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
			if (this.model.asset.scale && this.model.asset.scale.name) {
				this.model.asset.scale = { value: this.model.asset.scale.name, text: ''}
			}

			this.model.asset.environment = this.model.asset.environment || '';
		}

		/**
		 * Prepare te model and format all pending changes
		 */
		public onUpdate(): void {
			let modelRequest = R.clone(this.model);
			// Scale Format
			modelRequest.asset.scale = (modelRequest.asset.scale && modelRequest.asset.scale.value) ?
				modelRequest.asset.scale.value : modelRequest.asset.scale;

			this.model.customs.forEach((custom: any) => {
				let customValue = modelRequest.asset[custom.field.toString()];
				if (customValue && customValue.value) {
					modelRequest.asset[custom.field.toString()] = customValue.value;
				}
			});
			modelRequest.asset.moveBundleId = modelRequest.asset.moveBundle.id;
			delete modelRequest.asset.moveBundle;
			this.assetExplorerService.saveAsset(modelRequest).subscribe((result) => {
				this.notifierService.broadcast({
					name: 'reloadCurrentAssetList'
				});
				if (result === ApiResponseModel.API_SUCCESS || result === 'Success!') {
					this.saveAssetTags();
				}
			});
		}

		/**
		 * Delete the storage asset
		 */
		onDeleteAsset(): void {
			this.deleteAsset(this.model.asset.id);
		}

	}

	return StorageShowComponent;
}
