// Angular
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
// Model
import {ProviderAssociatedModel} from '../../model/provider-associated.model';
import {Dialog, DialogButtonModel, DialogButtonType, DialogConfirmAction} from 'tds-component-library';
// Other
import 'rxjs/add/operator/finally';

@Component({
	selector: 'tds-provider-associated',
	templateUrl: 'provider-associated.component.html'
})
export class ProviderAssociatedComponent extends Dialog implements OnInit {

	@Input() data: any;
	@Input() buttons: any;
	@Output() successEvent: EventEmitter<any> = new EventEmitter<any>();

	public providerAssociated: ProviderAssociatedModel;

	ngOnInit(): void {
		this.providerAssociated = this.data.providerAssociatedModel;
		const confirmButton: DialogButtonModel = {
			name: 'confirm',
			icon: 'check',
			text: 'Confirm',
			type: DialogButtonType.CONTEXT,
			action: this.onConfirm.bind(this)
		};

		const cancelButton: DialogButtonModel = {
			name: 'cancel',
			icon: 'ban',
			text: 'Cancel',
			type: DialogButtonType.CONTEXT,
			action: this.onCancel.bind(this)
		};

		this.buttons.push(confirmButton);
		this.buttons.push(cancelButton);
	}

	/**
	 * Close the Dialog by Confirm the Action
	 * @param result
	 */
	public onConfirm(): void {
		const data = {
			confirm: DialogConfirmAction.CONFIRM
		};
		super.onCancelClose(data);
	}

	/**
	 * Close the Dialog by Cancel/Close/Dismiss
	 * @param result
	 */
	public onCancel(): void {
		const data = {
			confirm: DialogConfirmAction.CANCEL
		};
		super.onCancelClose(data);
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		super.onCancelClose();
	}
}
