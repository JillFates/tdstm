import {Component, HostListener, OnInit} from '@angular/core';
import {SingleCommentModel} from './model/single-comment.model';
import {KEYSTROKE, ModalType} from '../../../../shared/model/constants';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {TaskService} from '../../../taskManager/service/task.service';
import {ComboBoxSearchModel} from '../../../../shared/components/combo-box/model/combobox-search-param.model';
import {Observable} from 'rxjs';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

@Component({
	selector: `single-comment`,
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/single-comment/single-comment.component.html',
	styles: []
})
export class SingleCommentComponent extends UIExtraDialog implements  OnInit {

	public modalType = ModalType;
	public dateFormatTime: string;
	public assetClassOptions: any[];
	public commentCategories: string[];
	private dataSignature: string;

	constructor(public singleCommentModel: SingleCommentModel, public userPreferenceService: PreferenceService, public taskManagerService: TaskService, public assetExplorerService: AssetExplorerService, public promptService: UIPromptService) {
		super('#single-comment-component');
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
		this.singleCommentModel.modal.title = 'Edit Comment';
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