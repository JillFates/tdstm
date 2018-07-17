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
import {TagService} from '../../../assetTags/service/tag.service';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';

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

		protected assetTagsModel: any = {tags: metadata.assetTags};
		protected newAssetTagsSelection: any = {tags: []};
		protected tagList: Array<TagModel> = metadata.tagList;
		private isDependenciesValidForm = true;
		constructor(
			@Inject('model') private model: any,
			private activeDialog: UIActiveDialogService,
			private preference: PreferenceService,
			private assetExplorerService: AssetExplorerService,
			private dialogService: UIDialogService,
			private notifierService: NotifierService,
			private tagService: TagService) {
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
		 * TODO: Document.
		 * @param $event
		 */
		protected onTagValueChange($event: any): void {
			this.newAssetTagsSelection.tags = $event.tags;
			console.log(this.newAssetTagsSelection);
		}

		private saveAssetTags(): void {
			let tagsToAdd = {tags: []};
			let tagsToDelete = {...this.assetTagsModel};
			this.newAssetTagsSelection.tags.forEach((asset: TagModel) => {
				let foundIndex = this.assetTagsModel.tags.findIndex( item => item.id === asset.id);
				if (foundIndex === -1) {
					tagsToAdd.tags.push(asset);
				} else {
					// tag remains
					tagsToDelete.tags.splice(foundIndex, 1);
				}
			});
			console.log('to add', tagsToAdd);
			console.log('to delete', tagsToDelete);
			this.tagService.createAssetTags(this.model.assetId, tagsToAdd.tags.map( item => item.id) )
				.subscribe(result => {
					this.showAssetDetailView(this.model.asset.assetClass.name, this.model.assetId);
				}, error => console.log('error when saving asset tags', error));
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
