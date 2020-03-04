// Angular
import {Component, Input, OnInit} from '@angular/core';
// Service
import {TagService} from '../../service/tag.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Model
import {TagModel} from '../../model/tag.model';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
// Other
import * as R from 'ramda';

@Component({
	selector: 'tag-merge-dialog',
	templateUrl: 'tag-merge-dialog.component.html',
	providers: [TranslatePipe]
})
export class TagMergeDialogComponent extends Dialog implements OnInit {

	@Input() data: any;

	public tagList: Array<TagModel> = [];
	public tagModel: TagModel;
	public mergeToTag: TagModel;
	private dataSignature: string;

	private readonly MERGE_CONFIRMATION = 'ASSET_TAGS.TAG_LIST.MERGE_CONFIRMATION';

	constructor(
		private tagService: TagService,
		private dialogService: DialogService,
		private translatePipe: TranslatePipe) {
		super();
	}

	ngOnInit(): void {
		this.tagModel = R.clone(this.data.tagModel);

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			disabled: () => !this.mergeToTag || !this.mergeToTag.id,
			type: DialogButtonType.ACTION,
			action: this.onMerge.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.onLoad();
	}

	/**
	 * Load necessary lists to render the view.
	 */
	private onLoad(): void {
		this.tagService.getTags().subscribe((result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				let defaultEmptyItem = new TagModel();
				defaultEmptyItem.name = this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER');
				this.mergeToTag = defaultEmptyItem;
				this.tagList.push(defaultEmptyItem);
				this.tagList.push(...result.data.filter(item => item.id !== this.tagModel.id));
				this.dataSignature = JSON.stringify(this.mergeToTag);
			} else {
				this.handleError(result.errors ? result.errors[0] : 'an error ocurred while loading the tag list.');
			}
		}, error => this.handleError(error));
	}

	/**
	 * On Merge button click, prompts a confirmation, then does the merge operation if confirmed.
	 */
	public onMerge(): void {
		this.dialogService.confirm(
			this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONTINUE_WITH_CHANGES'),
			this.translatePipe.transform(this.MERGE_CONFIRMATION)).subscribe((confirmation: any) => {
			if (confirmation) {
				if (confirmation.confirm === DialogConfirmAction.CONFIRM) {
					// Do the merge, then close popup
					this.tagService.mergeTags(this.tagModel.id, this.mergeToTag.id).subscribe((result: ApiResponseModel) => {
						if (result.status === ApiResponseModel.API_SUCCESS) {
							this.onAcceptSuccess();
						} else {
							this.handleError(result.errors ? result.errors[0] : 'error ocurred while merging tags.')
						}
					}, error => this.handleError(error));
				}
			}
		});
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((result: any) => {
				if (result.confirm === DialogConfirmAction.CONFIRM) {
					this.onCancelClose();
				}
			});
		} else {
			this.onCancelClose();
		}
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.mergeToTag);
	}

	/**
	 * Generic error handler function.
	 * @param error
	 */
	private handleError(error): void {
		console.log(error);
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}

}
