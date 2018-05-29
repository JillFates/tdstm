import {Component, HostListener, Input, OnInit} from '@angular/core';
import {SingleCommentModel} from './single-comment.model';
import {KEYSTROKE, ModalType} from '../../../../shared/model/constants';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: `single-comment`,
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/single-comment/single-comment.component.html',
	styles: []
})
export class SingleCommentComponent  extends UIExtraDialog  implements OnInit  {

	public modalType = ModalType;

	constructor(public singleCommentModel: SingleCommentModel) {
		super('#single-comment-component');
	}

	ngOnInit(): void {
		console.log('init');
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