import { Component, OnInit } from '@angular/core';
import { AssetCommentModel } from '../../model/asset-comment.model';
import { ModalType } from '../../../../shared/model/constants';
import { UIExtraDialog } from '../../../../shared/services/ui-dialog.service';
import { PreferenceService } from '../../../../shared/services/preference.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { TaskService } from '../../../taskManager/service/task.service';
import { AssetExplorerService } from '../../../assetManager/service/asset-explorer.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { Permission } from '../../../../shared/model/permission.model';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: `asset-comment-view-edit`,
	templateUrl: 'asset-comment-view-edit.component.html',
	styles: [],
})
export class AssetCommentViewEditComponent extends UIExtraDialog
	implements OnInit {
	public modalType = ModalType;
	public dateFormatTime: string;
	public assetClassOptions: any[];
	public commentCategories: string[];
	private dataSignature: string;

	constructor(
		private translate: TranslatePipe,
		public assetCommentModel: AssetCommentModel,
		public userPreferenceService: PreferenceService,
		public taskManagerService: TaskService,
		public assetExplorerService: AssetExplorerService,
		private translatePipe: TranslatePipe,
		public promptService: UIPromptService,
		private permissionService: PermissionService
	) {
		super('#asset-comment-view-edit-component');
	}

	ngOnInit(): void {
		this.dateFormatTime = this.userPreferenceService.getUserDateTimeFormat();
		// ModalType.VIEW doesn't need the categories,
		// in fact we need not to load them in that case for permission issues
		if (this.assetCommentModel.modal.type !== ModalType.VIEW) {
			this.loadCommentCategories();
		}
	}

	/**
	 * Load All Comment Categories
	 */
	private loadCommentCategories(): void {
		this.taskManagerService.getAssetCommentCategories().subscribe(res => {
			this.commentCategories = res;
			if (
				!this.assetCommentModel.category ||
				this.assetCommentModel.category === null
			) {
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
		return this.assetCommentModel.modal.type === ModalType.VIEW ?
			false : this.dataSignature !== JSON.stringify(this.getModelFields());
	}

	/**
	 * Change to Edit view
	 */
	protected onEdit(): void {
		this.assetCommentModel.modal.type = ModalType.EDIT;
		this.loadCommentCategories();
	}

	protected onSave(): void {
		this.taskManagerService
			.saveComment(this.assetCommentModel)
			.subscribe(res => {
				this.close();
			});
	}

	/**
	 * Get only the fields relevants to the model
	 */
	getModelFields(): any {
		const {
			id,
			archive,
			comment,
			category,
			assetClass,
			asset,
		} = this.assetCommentModel;

		return { id, archive, comment, category, assetClass, asset };
	}

	/**
	 * Delete the Asset Comment
	 */
	protected onDelete(): void {
		this.promptService
			.open(
				'Confirmation Required',
				'Confirm deletion of this record. There is no undo for this action.',
				'Confirm',
				'Cancel'
			)
			.then(confirm => {
				if (confirm) {
					this.taskManagerService
						.deleteTaskComment(this.assetCommentModel.id)
						.subscribe(res => {
							this.close();
						});
				}
			})
			.catch(error => console.log(error));
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public closeDialog(): void {
		if (this.isDirty()) {
			this.promptService
				.open(
					this.translatePipe.transform(
						'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'
					),
					this.translatePipe.transform(
						'GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'
					),
					this.translatePipe.transform('GLOBAL.CONFIRM'),
					this.translatePipe.transform('GLOBAL.CANCEL')
				)
				.then(confirm => {
					if (confirm) {
						this.dismiss();
					}
				})
				.catch(error => console.log(error));
		} else {
			this.dismiss();
		}
	}

	/**
	 * Based on modalType action returns the corresponding title
	 * @param {ModalType} modalType
	 * @returns {string}
	 */
	getModalTitle(modalType: ModalType): string {
		if (modalType === ModalType.EDIT) {
			return this.translate.transform('COMMENT.EDIT_COMMENT');
		}

		if (modalType === ModalType.CREATE) {
			return this.translate.transform('COMMENT.CREATE_COMMENT');
		}

		if (modalType === ModalType.VIEW) {
			return this.translate.transform('COMMENT.SHOW_COMMENT');
		}

		return '';
	}

	protected isCommentEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.CommentEdit);
	}

	protected isCommentDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.CommentDelete);
	}
}
