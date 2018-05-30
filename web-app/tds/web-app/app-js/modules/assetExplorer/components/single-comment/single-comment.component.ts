import {Component, HostListener} from '@angular/core';
import {SingleCommentModel} from './single-comment.model';
import {KEYSTROKE, ModalType} from '../../../../shared/model/constants';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';

@Component({
	selector: `single-comment`,
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/single-comment/single-comment.component.html',
	styles: []
})
export class SingleCommentComponent  extends UIExtraDialog {

	public modalType = ModalType;
	public dateFormatTime: string;

	constructor(public singleCommentModel: SingleCommentModel, public userPreferenceService: PreferenceService) {
		super('#single-comment-component');
		this.dateFormatTime = this.userPreferenceService.getUserTimeZone() + ' ' + DateUtils.DEFAULT_FORMAT_TIME;
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