import {TagModel} from '../../../assetTags/model/tag.model';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {HostListener, Inject, OnInit} from '@angular/core';
import {TagService} from '../../../assetTags/service/tag.service';
import {DIALOG_SIZE, KEYSTROKE} from '../../../../shared/model/constants';
import {AssetShowComponent} from './asset-show.component';

declare var jQuery: any;

export class AssetCommonEdit implements OnInit {

	private assetTagsDirty = false;
	protected assetTagsModel: any = {tags: []};
	protected newAssetTagsSelection: any = {tags: []};
	protected tagList: Array<TagModel> = [];
	protected dateFormat: string;
	protected isDependenciesValidForm = true;

	constructor(
		protected model: any,
		protected activeDialog: UIActiveDialogService,
		protected preference: PreferenceService,
		protected assetExplorerService: AssetExplorerService,
		protected dialogService: UIDialogService,
		protected notifierService: NotifierService,
		protected tagService: TagService,
		protected metadata: any,
		private promptService: UIPromptService) {
			this.assetTagsModel = {tags: metadata.assetTags};
			this.tagList = metadata.tagList;
			this.dateFormat = this.preference.preferences['CURR_DT_FORMAT'];
			this.dateFormat = this.dateFormat.toLowerCase().replace(/m/g, 'M');
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

	/**
	 * On Tag Selector change event.
	 * @param $event
	 */
	protected onTagValueChange($event: any): void {
		this.newAssetTagsSelection.tags = $event.tags;
		this.assetTagsDirty = true;
	}

	/**
	 * Save Asset Tags configuration
	 */
	protected saveAssetTags(): void {
		let tagsToAdd = {tags: []};
		let tagsToDelete = {...this.assetTagsModel};
		this.newAssetTagsSelection.tags.forEach((asset: TagModel) => {
			let foundIndex = this.assetTagsModel.tags.findIndex( item => item.id === asset.id);
			if (foundIndex === -1) {
				// add tag
				tagsToAdd.tags.push(asset);
			} else {
				// tag remains
				tagsToDelete.tags.splice(foundIndex, 1);
			}
		});
		if (!this.assetTagsDirty ||
			(tagsToAdd.tags.length === 0 && tagsToDelete.tags.length === 0) ) {
			this.showAssetDetailView(this.model.asset.assetClass.name, this.model.assetId);
		} else {
			this.tagService.createAndDeleteAssetTags(this.model.assetId,
				tagsToAdd.tags.map( item => item.id),
				tagsToDelete.tags.map( item => item.assetTagId))
				.subscribe(result => {
					this.showAssetDetailView(this.model.asset.assetClass.name, this.model.assetId);
				}, error => console.log('error when saving asset tags', error));
		}
	}

	protected showAssetDetailView(assetClass: string, id: number) {
		this.dialogService.replace(AssetShowComponent, [
				{ provide: 'ID', useValue: id },
				{ provide: 'ASSET', useValue: assetClass }],
			DIALOG_SIZE.XLG);
	}

	/***
	 * Close the Active Dialog
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.close();
	}

	/**
	 * Validate if the current content of the Dependencies is correct
	 * @param {boolean} invalidForm
	 */
	protected onDependenciesValidationChange(validForm: boolean): void {
		this.isDependenciesValidForm = validForm;
	}

	protected prompSaveChanges(): void {
		this.promptService.open(
			'Confirmation Required',
			'You have changes that have not been saved. Do you want to continue and lose those changes?',
			'Confirm', 'Cancel').then(result => {
			if (result) {
				this.cancelCloseDialog();
			}
		});
	}
}