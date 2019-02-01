import {Component, OnInit} from '@angular/core';
import {AssetCommentModel} from '../../model/asset-comment.model';
import {ModalType} from '../../../../shared/model/constants';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TaskService} from '../../../taskManager/service/task.service';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';

@Component({
	selector: `asset-comment-view-edit`,
	templateUrl: '../tds/web-app/app-js/modules/assetComment/components/view-edit/asset-comment-view-edit.component.html',
	styles: []
})
export class AssetCommentViewEditComponent extends UIExtraDialog implements  OnInit {

	public modalType = ModalType;
	public dateFormatTime: string;
	public assetClassOptions: any[];
	public commentCategories: string[];
	private dataSignature: string;

	constructor(public assetCommentModel: AssetCommentModel, public userPreferenceService: PreferenceService, public taskManagerService: TaskService, public assetExplorerService: AssetExplorerService, public promptService: UIPromptService) {
		super('#asset-comment-view-edit-component');
	}

	ngOnInit(): void {
		this.dateFormatTime = this.userPreferenceService.getUserDateTimeFormat();
		this.loadCommentCategories();
	}

	/**
	 * Load All Comment Categories
	 */
	private loadCommentCategories(): void {
		this.taskManagerService.getCommentCategories().subscribe((res) => {
			this.commentCategories = res;
			if (!this.assetCommentModel.category || this.assetCommentModel.category === null) {
				this.assetCommentModel.category = this.commentCategories[0];
			}
			this.dataSignature = JSON.stringify(this.getModelFields());
		});
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.getModelFields());
	}

	/**
	 * Change to Edit view
	 */
	protected onEdit(): void {
		this.assetCommentModel.modal.title = 'Edit Comment';
		this.assetCommentModel.modal.type = ModalType.EDIT;
	}

	protected onSave(): void {
		this.taskManagerService.saveComment(this.assetCommentModel).subscribe((res) => {
			this.close();
		});
	}

	/**
	 * Get only the fields relevants to the model
	 */
	getModelFields(): any {
		const {id, archive, comment, category, assetClass, asset} = this.assetCommentModel;

		return {id, archive, comment, category, assetClass, asset};
	}

	/**
	 * Delete the Asset Comment
	 */
	protected onDelete(): void {
		this.promptService.open(
			'Confirmation Required',
			'Confirm deletion of this record. There is no undo for this action?',
			'Confirm', 'Cancel')
			.then(confirm => {
				if (confirm) {
					this.taskManagerService.deleteTaskComment(this.assetCommentModel.id).subscribe((res) => {
						this.close();
					});
				}
			})
			.catch((error) => console.log(error));
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
				.then(confirm => {
					if (confirm) {
						this.dismiss();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.dismiss();
		}
	}
}