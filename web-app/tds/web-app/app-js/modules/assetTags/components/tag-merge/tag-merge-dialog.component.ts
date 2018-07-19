import {Component} from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {TagService} from '../../service/tag.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TagModel} from '../../model/tag.model';
import {KEYSTROKE, PROMPT_CANCEL, PROMPT_CONFIRM, PROMPT_DEFAULT_TITLE_KEY} from '../../../../shared/model/constants';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'tag-merge-dialog',
	templateUrl: '../tds/web-app/app-js/modules/assetTags/components/tag-merge/tag-merge-dialog.component.html',
	host: {
		'(keydown)': 'keyDownHandler($event)'
	},
	providers: [TranslatePipe]
})
export class TagMergeDialogComponent {

	protected tagList: Array<TagModel> = [];
	protected mergeToTag: TagModel;

	private readonly MERGE_CONFIRMATION = 'ASSET_TAGS.TAG_LIST.MERGE_CONFIRMATION';

	constructor(
		protected tagModel: TagModel,
		private tagService: TagService,
		private promptService: UIPromptService,
		private activeDialog: UIActiveDialogService,
		private translatePipe: TranslatePipe) {
			this.onLoad();
	}

	/**
	 * Load necessary lists to render the view.
	 */
	private onLoad(): void {
		this.tagService.getTags().subscribe((result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				let defaultEmptyItem = new TagModel();
				defaultEmptyItem.name = 'Select...';
				this.mergeToTag = defaultEmptyItem;
				this.tagList.push(defaultEmptyItem);
				this.tagList.push(...result.data.filter( item => item.id !== this.tagModel.id ));
			} else {
				this.handleError(result.errors ? result.errors[0] : 'an error ocurred while loading the tag list.');
			}
		}, error => this.handleError(error));
	}

	/**
	 * On Merge button click, prompts a confirmation, then does the merge operation if confirmed.
	 */
	protected onMerge(): void {
		this.promptService.open(
			this.translatePipe.transform(PROMPT_DEFAULT_TITLE_KEY),
			this.translatePipe.transform(this.MERGE_CONFIRMATION),
			this.translatePipe.transform(PROMPT_CONFIRM),
			this.translatePipe.transform(PROMPT_CANCEL)).then(result => {
			if (result) {
				// Do the merge, then close popup
				this.tagService.mergeTags(this.tagModel.id, this.mergeToTag.id).subscribe( (result: ApiResponseModel) => {
					if (result.status === ApiResponseModel.API_SUCCESS) {
						this.activeDialog.close(true);
					} else {
						this.handleError(result.errors ? result.errors[0] : 'error ocurred while merging tags.')
					}
				}, error => this.handleError(error));
			}
		}, (reason: any) => console.log('confirm rejected', reason));
	}

	/**
	 * Detect if the use has pressed the on Escape to close the dialog and popup if there are pending changes.
	 * @param {KeyboardEvent} event
	 */
	private keyDownHandler($event: KeyboardEvent): void {
		if ($event && $event.code === KEYSTROKE.ESCAPE) {
			this.cancelCloseDialog();
		}
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.close(false);
	}

	/**
	 * Generic error handler function.
	 * @param error
	 */
	private handleError(error): void {
		console.log(error);
	}

}