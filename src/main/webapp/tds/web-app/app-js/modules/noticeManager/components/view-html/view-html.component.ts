// Angular
import {Component, Input, OnInit} from '@angular/core';
// Model
import {NoticeModel} from '../../model/notice.model';
import {Dialog, DialogButtonType} from 'tds-component-library';
// Other
import 'rxjs/add/operator/finally';
import * as R from 'ramda';
import {ActionType} from '../../../../shared/model/action-type.enum';

@Component({
	selector: 'tds-notice-view-html',
	templateUrl: 'view-html.component.html'
})
export class ViewHtmlComponent extends Dialog implements OnInit {

	@Input() data: any;
	public noticeModel: NoticeModel;

	constructor() {
		super();
	}

	ngOnInit(): void {
		this.noticeModel = R.clone(this.data.model);

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			tooltipText: 'Close',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});
	}

	protected cancelCloseDialog(): void {
		this.onCancelClose();
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
