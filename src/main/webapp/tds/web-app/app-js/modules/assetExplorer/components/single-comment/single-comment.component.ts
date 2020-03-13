import {Component, OnInit} from '@angular/core';
import {SingleCommentModel} from './model/single-comment.model';
import { ModalType} from '../../../../shared/model/constants';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {TaskService} from '../../../taskManager/service/task.service';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {UserContextService} from '../../../auth/service/user-context.service';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {Permission} from '../../../../shared/model/permission.model';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: `single-comment`,
	templateUrl: 'single-comment.component.html',
	styles: []
})
export class SingleCommentComponent extends UIExtraDialog implements  OnInit {

	public modalType = ModalType;
	public userTimeZone: string;
	public dateFormatTime: string;
	public assetClassOptions: any[];
	public commentCategories: string[];
	private dataSignature: string;

	constructor(
		public singleCommentModel: SingleCommentModel,
		public userContextService: UserContextService,
		public taskManagerService: TaskService,
		public assetExplorerService: AssetExplorerService,
		public promptService: UIPromptService,
		private translatePipe: TranslatePipe,
		private userPreferenceService: PreferenceService,
		private permissionService: PermissionService) {
		super('#single-comment-component');
	}

	ngOnInit(): void {
		this.userTimeZone = this.userPreferenceService.getUserTimeZone();
		this.dateFormatTime = this.userPreferenceService.getUserDateTimeFormat();

		this.loadCommentCategories();
	}

	/**
	 * Load All Comment Categories
	 */
	private loadCommentCategories(): void {
			this.taskManagerService.getAssetCommentCategories().subscribe((res) => {
				this.commentCategories = res;
				if (!this.singleCommentModel.category || this.singleCommentModel.category === null) {
					this.singleCommentModel.category = this.commentCategories[0];
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
		this.loadCommentCategories();
		this.singleCommentModel.modal.type = ModalType.EDIT;
	}

	protected onSave(): void {
		this.taskManagerService.saveComment(this.singleCommentModel).subscribe((res) => {
			this.close();
		});
	}

	/**
	 * Get only the fields relevants to the model
	 */
	getModelFields(): any {
		const {id, archive, comment, category, assetClass, asset} = this.singleCommentModel;

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
					this.taskManagerService.deleteTaskComment(this.singleCommentModel.id).subscribe((res) => {
						this.close();
					});
				}
			})
			.catch((error) => console.log(error));
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.promptService.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'),
			)
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

	/**
	 * Based on modalType action returns the corresponding title
	 * @param {ModalType} modalType
	 * @returns {string}
	 */
	getModalTitle(modalType: ModalType): string {
		if (modalType === ModalType.EDIT) {
			return this.translatePipe.transform('COMMENT.EDIT_COMMENT');
		}

		if (modalType === ModalType.CREATE) {
			return this.translatePipe.transform('COMMENT.CREATE_COMMENT');
		}

		if (modalType === ModalType.VIEW) {
			return this.translatePipe.transform('COMMENT.SHOW_COMMENT');
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
