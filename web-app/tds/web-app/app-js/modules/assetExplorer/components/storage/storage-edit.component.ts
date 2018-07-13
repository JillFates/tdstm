/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import {Component, HostListener, Inject, OnInit} from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import { PreferenceService } from '../../../../shared/services/preference.service';
import {AssetShowComponent} from '../asset/asset-show.component';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import * as R from 'ramda';
import {KEYSTROKE} from '../../../../shared/model/constants';
import {TagModel} from '../../../assetTags/model/tag.model';

declare var jQuery: any;

export function StorageEditComponent(template: string, editModel: any, metadata: any): any {
	@Component({
		selector: 'storage-edit',
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	})
	class StorageShowComponent implements  OnInit {

		protected assetTagsModel: any = {operator: 'OR', tags: metadata.assetTags};
		protected tagList: Array<TagModel> = metadata.tagList;
		private isDependenciesValidForm = true;
		constructor(
			@Inject('model') private model: any,
			private activeDialog: UIActiveDialogService,
			private preference: PreferenceService,
			private assetExplorerService: AssetExplorerService,
			private dialogService: UIDialogService,
			private notifierService: NotifierService) {
		}

		/**
		 * TODO: Document.
		 * @param $event
		 */
		protected onTagValueChange($event: any): void {
			console.log($event);
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

		/***
		 * Close the Active Dialog
		 */
		public cancelCloseDialog(): void {
			this.activeDialog.close();
		}

		/**
		 * Prepare te model and format all pending changes
		 */
		public onUpdate(): void {
			let modelRequest = R.clone(this.model);
			// Scale Format
			modelRequest.asset.scale = (modelRequest.asset.scale.name.value) ? modelRequest.asset.scale.name.value : modelRequest.asset.scale.name;
			this.model.customs.forEach((custom: any) => {
				let customValue = modelRequest.asset[custom.field.toString()];
				if (customValue && customValue.value) {
					modelRequest.asset[custom.field.toString()] = customValue.value;
				}
			});
			modelRequest.asset.moveBundleId = modelRequest.asset.moveBundle.id;
			delete modelRequest.asset.moveBundle;
			// Date Formats
			// modelRequest.asset.maintExpDate = DateUtils.translateTimeZoneFormat(modelRequest.asset.maintExpDate);
			// modelRequest.asset.retireDate
			this.assetExplorerService.saveAsset(modelRequest).subscribe((res) => {
				this.notifierService.broadcast({
					name: 'reloadCurrentAssetList'
				});
				this.showAssetDetailView(this.model.asset.assetClass.name, this.model.assetId);
			});
		}

		private showAssetDetailView(assetClass: string, id: number) {
			this.dialogService.replace(AssetShowComponent, [
					{ provide: 'ID', useValue: id },
					{ provide: 'ASSET', useValue: assetClass }],
				'lg');
		}

		/**
		 * Validate if the current content of the Dependencies is correct
		 * @param {boolean} invalidForm
		 */
		public onDependenciesValidationChange(validForm: boolean): void {
			this.isDependenciesValidForm = validForm;
		}

	}

	return StorageShowComponent;
}
