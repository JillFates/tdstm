import {Component} from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {TagService} from '../../service/tag.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TagModel} from '../../model/tag.model';
import {KEYSTROKE} from '../../../../shared/model/constants';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';

@Component({
	selector: 'tag-merge-dialog',
	templateUrl: '../tds/web-app/app-js/modules/assetTags/components/tag-merge/tag-merge-dialog.component.html',
	host: {
		'(keydown)': 'keyDownHandler($event)'
	}
})
export class TagMergeDialogComponent {

	protected tagList: Array<TagModel> = [];
	protected mergeTag: TagModel;

	constructor(
		protected tagModel: TagModel,
		private tagService: TagService,
		private promptService: UIPromptService,
		private activeDialog: UIActiveDialogService) {
			this.onLoad();
	}

	/**
	 * TODO: document.
	 */
	private onLoad(): void {
		this.tagService.getTags().subscribe( (result: Array<TagModel>) => {
			let defaultEmptyItem = new TagModel();
			defaultEmptyItem.Name = 'Select...';
			this.mergeTag = defaultEmptyItem;
			this.tagList.push(defaultEmptyItem);
			this.tagList.push(...result.filter( item => item.id !== this.tagModel.id ));
		}, error => this.handleError(error));
	}

	protected onMerge(): void {
		this.promptService.open(
			'Confirmation Required',
			'Confirm merging of Tags. There is no undo for this action.',
			'Confirm', 'Cancel').then(result => {
			if (result) {
				// Do the merge, then close popup
				this.tagService.mergeTags(this.tagModel.id, this.mergeTag.id).subscribe( result => {
					if (result.status === ApiResponseModel.API_SUCCESS) {
						this.activeDialog.close(true);
					} else {
						this.handleError(result.errors ? result.errors[0] : 'Error ocurred when merging tags.')
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
	 * TODO: document this.
	 * @param error
	 */
	private handleError(error): void {
		console.log(error);
	}

}