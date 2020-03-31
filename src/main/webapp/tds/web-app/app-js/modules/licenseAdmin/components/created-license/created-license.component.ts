// Angular
import {Component, Input, OnInit} from '@angular/core';
// Model
import {RequestLicenseModel} from '../../model/license.model';
import {Dialog, DialogButtonType} from 'tds-component-library';

@Component({
	selector: 'tds-license-created',
	templateUrl: 'created-license.component.html'
})
export class CreatedLicenseComponent extends Dialog implements OnInit {
	@Input() data: any;
	public requestLicenseModel: RequestLicenseModel = null;

	constructor() {
		super();
	}

	ngOnInit(): void {

		this.requestLicenseModel = Object.assign({}, this.data.requestLicenseModel);

		this.buttons.push({
			name: 'close',
			icon: 'check',
			text: 'Close',
			show: () => true,
			type: DialogButtonType.CONTEXT,
			action: this.cancelCloseDialog.bind(this)
		});
	}

	/**
	 * Close the Dialog
	 */
	public cancelCloseDialog(): void {
		this.onCancelClose();
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
