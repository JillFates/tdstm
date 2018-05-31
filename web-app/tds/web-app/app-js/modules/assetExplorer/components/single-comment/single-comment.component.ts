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

@Component({
	selector: `single-comment`,
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/single-comment/single-comment.component.html',
	styles: []
})
export class SingleCommentComponent extends UIExtraDialog {

	public modalType = ModalType;
	public dateFormatTime: string;
	public commentCategories: string[];

	constructor(public singleCommentModel: SingleCommentModel, public userPreferenceService: PreferenceService, public taskManagerService: TaskService, public assetExplorerService: AssetExplorerService) {
		super('#single-comment-component');
		this.dateFormatTime = this.userPreferenceService.getUserTimeZone() + ' ' + DateUtils.DEFAULT_FORMAT_TIME;
		this.taskManagerService.getCommentCategories().subscribe((res) => {
			this.commentCategories = res;
		});
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
	 * Pass Service as Reference
	 * @param {ComboBoxSearchModel} searchParam
	 * @returns {Observable<any>}
	 */
	public getAssetListForComboBox = (searchParam: ComboBoxSearchModel): Observable<any> => {
		return this.assetExplorerService.getAssetListForComboBox(searchParam);
	}

	/**
	 * Change to Edit view
	 */
	protected onEdit(): void {
		this.singleCommentModel.modal.type = ModalType.EDIT;
	}

	/**
	 * Close Dialog
	 */
	protected cancelCloseDialog(): void {
		this.dismiss();
	}
}