import {TagModel} from '../../../assetTags/model/tag.model';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TagService} from '../../../assetTags/service/tag.service';
import {DIALOG_SIZE} from '../../../../shared/model/constants';
import {AssetShowComponent} from './asset-show.component';
import {equals as ramdaEquals, clone as ramdaClone} from 'ramda';

declare var jQuery: any;

export class AssetCommonEdit implements OnInit {
	@ViewChild('form') protected form: NgForm;

	private assetTagsDirty = false;
	protected assetTagsModel: any = {tags: []};
	protected newAssetTagsSelection: any = {tags: []};
	protected tagList: Array<TagModel> = [];
	protected dateFormat: string;
	protected isDependenciesValidForm = true;
	protected defaultSelectOption = 'Please Select';
	private initialModel: any = null;

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
	/**
	 * Initiates The Injected Component
	 */
	ngOnInit(): void {
		jQuery('[data-toggle="popover"]').popover();
	}

	/**
	 * Save a reference to the initial model in order to detect changes later on
	 */
	onInitDependenciesDone(model): void {
		this.initialModel = ramdaClone(model);
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
	 * Used on Create Asset view.
	 * Creates the tag associations if configured.
	 */
	protected createTags(assetId: number): void {
		let tagsToAdd = {tags: []};
		if (this.newAssetTagsSelection.tags && this.newAssetTagsSelection.tags.length > 0) {
			tagsToAdd = this.newAssetTagsSelection;
		}
		this.tagService.createAssetTags(assetId, tagsToAdd.tags.map( item => item.id)).subscribe( result => {
			this.showAssetDetailView(this.model.asset.assetClass.name, assetId);
		}, error => console.error('Error while saving asset tags', error));
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
			DIALOG_SIZE.LG);
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

	/**
	 * Notify user there are changes
	 */
	protected promptSaveChanges(): void {
		this.promptService.open(
			'Confirmation Required',
			'You have changes that have not been saved. Do you want to continue and lose those changes?',
			'Confirm', 'Cancel').then(result => {
			if (result) {
				this.cancelCloseDialog();
			} else {
				this.focusAssetModal();
			}
		});
	}

	/**
	 * On Cancel if there is changes notify user
	 */
	protected onCancelEdit(): void {
		if (this.assetTagsDirty || !ramdaEquals(this.initialModel, this.model)) {
			this.promptSaveChanges();
		} else {
			this.cancelCloseDialog();
		}
	}

	/**
	 * Submit the form in case errors select the first invalid field
	 */
	protected submitForm(event): void {
		if (!this.form.onSubmit(event) ) {
			this.focusFirstInvalidFieldInput();
		}
	}

	/**
	 * Focus the first control that belongs to the asset entry form and has an invalid status
	 */
	private focusFirstInvalidFieldInput(): void {
		jQuery('form.asset-entry-form .tm-input-control.ng-invalid:first').focus();
	}

	/**
	 allows to delete the application assets
	 */
	deleteAsset(assetId) {

		this.promptService.open('Confirmation Required',
			'You are about to delete the selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel',
			'OK', 'Cancel')
			.then( success => {
				if (success) {
					this.assetExplorerService.deleteAssets([assetId]).subscribe( res => {
						if (res) {
							this.notifierService.broadcast({
								name: 'reloadCurrentAssetList'
							});
							this.activeDialog.dismiss();
						}
					}, (error) => console.log(error));
				}
			})
			.catch((error) => console.log(error));
	}

	protected focusAssetModal(): void {
		setTimeout(() => jQuery('.modal-content').focus(), 500);
	}

	/**
	 * Focus a control matching by name
	 */
	protected focusControlByName(name): void {
		// delay selection until bootstrap effects are done
		setTimeout(() => {
			jQuery(`form.asset-entry-form .tm-input-control[name='${name}']:first`).focus();
		}, 600);
	}
}