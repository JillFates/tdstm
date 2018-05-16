import {Component, ViewChild, ElementRef} from '@angular/core';

import { UIExtraDialog} from '../../../shared/services/ui-dialog.service';
import {AssetComment} from './model/asset-coment.model';

@Component({
	selector: 'dependent-comment',
	templateUrl: '../tds/web-app/app-js/shared/components/dependent-comment/dependent-comment.component.html',
	styles: [`
			div.modal-body,
			div.box-body
			{
				padding-bottom: 0px;
            }
			div.modal-title {
                width: 453px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
			}
	`]
})
export class DependentCommentComponent extends UIExtraDialog {

	constructor(
		public assetCommentModel: AssetComment) {
		super('#dependent-comment-component');
	}

	/**
	 * On EscKey Pressed close the dialog.
	 */
	onEscKeyPressed(): void {
		this.cancelCloseDialog();
	}

	protected cancelCloseDialog(): void {
		this.dismiss();
	}

	protected onUpdateComment(): void {
		this.close(this.assetCommentModel);
	}

}