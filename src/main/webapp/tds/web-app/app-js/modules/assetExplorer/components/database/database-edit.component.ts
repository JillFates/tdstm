// Angular
import {Component, ComponentFactoryResolver, Inject, OnInit} from '@angular/core';
// Model
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
// Component
import {AssetCommonEdit} from '../asset/asset-common-edit';
// Service
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {TagService} from '../../../assetTags/service/tag.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {DialogService} from 'tds-component-library';
// Other
import * as R from 'ramda';

export function DatabaseEditComponent(template, editModel, metadata: any, parentDialog: any) {

	@Component({
		selector: `tds-database-edit`,
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	}) class DatabaseShowComponent extends AssetCommonEdit implements OnInit {
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
			this.model.asset = R.clone(editModel.asset);

			if (this.model.asset.scale && this.model.asset.scale.name) {
				this.model.asset.scale = { value: this.model.asset.scale.name, text: ''}
			}
			this.model.asset.environment = this.model.asset.environment || '';

			this.focusControlByName('assetName');
			this.onFocusOutOfCancel();
		}

		/**
		 * Prepare te model and format all pending changes
		 */
		public onUpdate(): void {
			let modelRequest = R.clone(this.model);
			// Scale Format
			modelRequest.asset.scale = (modelRequest.asset.scale && modelRequest.asset.scale.value) ? modelRequest.asset.scale.value : modelRequest.asset.scale;
			this.model.customs.forEach((custom: any) => {
				let customValue = modelRequest.asset[custom.field.toString()];
				if (customValue && customValue.value) {
					modelRequest.asset[custom.field.toString()] = customValue.value;
				}
			});
			modelRequest.asset.moveBundleId = modelRequest.asset.moveBundle.id;
			delete modelRequest.asset.moveBundle;

			modelRequest.asset.environment = modelRequest.asset.environment === this.defaultSelectOption ?
				''	 : modelRequest.asset.environment;

			this.assetExplorerService.saveAsset(modelRequest).subscribe((result) => {
				this.notifierService.broadcast({
					name: 'reloadCurrentAssetList'
				});
				if (result === ApiResponseModel.API_SUCCESS || result === 'Success!') {
					this.saveAssetTags();
				}
			});
		}

		onDeleteAsset() {
			this.deleteAsset(this.model.asset.id);
		}
	}

	return DatabaseShowComponent;
}
