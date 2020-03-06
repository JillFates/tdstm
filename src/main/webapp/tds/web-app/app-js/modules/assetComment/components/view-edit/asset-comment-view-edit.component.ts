// Angular
import {Component, Input, OnInit, ViewChild} from '@angular/core';
// Model
import {AssetCommentModel} from '../../model/asset-comment.model';
import {ModalType} from '../../../../shared/model/constants';
import {Permission} from '../../../../shared/model/permission.model';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
// Service
import {PreferenceService} from '../../../../shared/services/preference.service';
import {TaskService} from '../../../taskManager/service/task.service';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import * as R from 'ramda';
import {NgForm} from '@angular/forms';

@Component({
	selector: `asset-comment-view-edit`,
	templateUrl: 'asset-comment-view-edit.component.html',
	styles: [],
})
export class AssetCommentViewEditComponent extends Dialog implements OnInit {
	@Input() data: any;

	// Forms
	@ViewChild('dependentForm', {static: false}) dependentForm: NgForm;

	public modalType = ModalType;
	public dateFormatTime: string;
	public assetClassOptions: any[];
	public commentCategories: string[];
	public assetCommentModel: AssetCommentModel;
	private dataSignature: string;

	constructor(
		private dialogService: DialogService,
		public userPreferenceService: PreferenceService,
		public taskManagerService: TaskService,
		public assetExplorerService: AssetExplorerService,
		private translatePipe: TranslatePipe,
		private permissionService: PermissionService
	) {
		super();
	}

	ngOnInit(): void {
		this.assetCommentModel = R.clone(this.data.assetCommentModel);

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			show: () => (this.assetCommentModel.modal.type === this.modalType.VIEW || this.assetCommentModel.modal.type === this.modalType.EDIT) && this.isCommentEditAvailable(),
			active: () => this.assetCommentModel.modal.type === this.modalType.EDIT,
			type: DialogButtonType.ACTION,
			action: this.onEdit.bind(this)
		});

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => this.assetCommentModel.modal.type !== this.modalType.VIEW,
			disabled: () => !this.dependentForm.dirty || !this.dependentForm.valid,
			type: DialogButtonType.ACTION,
			action: this.onSave.bind(this)
		});

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			show: () => this.isCommentDeleteAvailable() && this.assetCommentModel.modal.type !== this.modalType.CREATE,
			type: DialogButtonType.ACTION,
			action: this.onDelete.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			show: () => this.assetCommentModel.modal.type === this.modalType.VIEW || this.assetCommentModel.modal.type === this.modalType.CREATE,
			type: DialogButtonType.ACTION,
			action: this.closeDialog.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			show: () => this.assetCommentModel.modal.type === this.modalType.EDIT,
			type: DialogButtonType.ACTION,
			action: this.closeDialog.bind(this)
		});

		this.dateFormatTime = this.userPreferenceService.getUserDateTimeFormat();
		// ModalType.VIEW doesn't need the categories,
		// in fact we need not to load them in that case for permission issues
		if (this.assetCommentModel.modal.type !== ModalType.VIEW) {
			this.loadCommentCategories();
		}

		setTimeout(() => {
			this.setTitle(this.getModalTitle(this.assetCommentModel.modal.type));
		});
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
		this.setTitle(this.getModalTitle(this.assetCommentModel.modal.type));
		this.loadCommentCategories();
	}

	protected onSave(): void {
		this.taskManagerService
			.saveComment(this.assetCommentModel)
			.subscribe(res => {
				super.onCancelClose();
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
		this.dialogService.confirm(
			this.translatePipe.transform(
				'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'
			),
			'Confirm deletion of this record. There is no undo for this action.'
		)
			.subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					this.taskManagerService
						.deleteTaskComment(this.assetCommentModel.id)
						.subscribe(res => {
							super.onCancelClose();
						});
				}
			});
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public closeDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform(
					'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'
				),
				this.translatePipe.transform(
					'GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'
				)
			)
				.subscribe((data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM) {
						super.onCancelClose();
					}
				});
		} else {
			super.onCancelClose();
		}
	}

	/**
	 * Based on modalType action returns the corresponding title
	 * @param {ModalType} modalType
	 * @returns {string}
	 */
	getModalTitle(modalType: ModalType): string {
		let title = '';
		if (modalType === ModalType.EDIT) {
			title = this.translatePipe.transform('COMMENT.EDIT_COMMENT');
		}

		if (modalType === ModalType.CREATE) {
			title = this.translatePipe.transform('COMMENT.CREATE_COMMENT');
		}

		if (modalType === ModalType.VIEW) {
			title = this.translatePipe.transform('COMMENT.SHOW_COMMENT');
		}

		return title;
	}

	protected isCommentEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.CommentEdit);
	}

	protected isCommentDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.CommentDelete);
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.closeDialog();
	}
}
