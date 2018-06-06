import {Component, HostListener} from '@angular/core';
import {SingleCommentModel} from './single-comment.model';
import {KEYSTROKE, ModalType} from '../../../../shared/model/constants';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {TaskService} from '../../../taskManager/service/task.service';
import {ComboBoxSearchModel} from '../../../../shared/components/combo-box/model/combobox-search-param.model';
import {Observable} from 'rxjs/Observable';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

@Component({
	selector: `single-comment`,
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/single-comment/single-comment.component.html',
	styles: []
})
export class SingleCommentComponent extends UIExtraDialog {

	public modalType = ModalType;
	public dateFormatTime: string;
	public assetClassOptions: any[];
	public commentCategories: string[];

	constructor(public singleCommentModel: SingleCommentModel, public userPreferenceService: PreferenceService, public taskManagerService: TaskService, public assetExplorerService: AssetExplorerService, public promptService: UIPromptService) {
		super('#single-comment-component');
		this.dateFormatTime = this.userPreferenceService.getUserTimeZone() + ' ' + DateUtils.DEFAULT_FORMAT_TIME;
		this.loadAssetClass();
		this.loadCommentCategories();
	}

	/**
	 * Load All Asset Class and Retrieve
	 */
	private loadAssetClass(): void {
		this.assetExplorerService.getAssetClassOptions().subscribe((res) => {
			this.assetClassOptions = [];
			for (let prop in res) {
				if (res[prop]) {
					this.assetClassOptions.push({id: prop, text: res[prop]});
				}
			}
			if (!this.singleCommentModel.assetClass || this.singleCommentModel.assetClass.id === '') {
				this.singleCommentModel.assetClass = this.assetClassOptions[0];
			} else {
				this.singleCommentModel.assetClass = this.assetClassOptions.find((res) => {
					return res.id === this.singleCommentModel.assetClass.text.toUpperCase();
				});
			}
		});
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
		});
	}

	/**
	 * Pass Service as Reference
	 * @param {ComboBoxSearchModel} searchParam
	 * @returns {Observable<any>}
	 */
	public getAssetListForComboBox = (searchParam: ComboBoxSearchModel): Observable<any> => {
		return this.assetExplorerService.getAssetListForComboBox(searchParam);
	}

	/**
	 * Change the Asset selected since the class has changed
	 * @param assetClass
	 */
	public onAssetClassChange(assetClass): void {
		this.singleCommentModel.asset = {
			id: '',
			text: ''
		};
	}

	/**
	 *  Change the Asset Selection
	 * @param asset
	 */
	public onAssetChange(asset: any): void {
		this.singleCommentModel.asset = asset;
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
	 * Detect if the use has pressed the on Escape to close the dialog and popup if there are pending changes.
	 * @param {KeyboardEvent} event
	 */
	@HostListener('keydown', ['$event']) handleKeyboardEvent(event: KeyboardEvent) {
		if (event && event.code === KEYSTROKE.ESCAPE) {
			this.cancelCloseDialog();
		}
	}

	/**
	 * Close Dialog
	 */
	protected cancelCloseDialog(): void {
		this.dismiss();
	}
}