// Angular
import {
	Component,
	OnInit,
	ViewChild,
	Input, Output, EventEmitter, ComponentFactoryResolver,
} from '@angular/core';
import {NgForm} from '@angular/forms';
// Model

// Component

// Service

import {Dialog, DialogButtonType, DialogConfirmAction, DialogService, ModalSize} from 'tds-component-library';
// Other
import {Subject} from 'rxjs/Subject';
import {PermissionService} from '../../../../../../shared/services/permission.service';
import {ProviderService} from '../../../../../provider/service/provider.service';
import {TranslatePipe} from '../../../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'regenerate-modal',
	templateUrl: 'regenerate.component.html'
})
export class RegenerateComponent extends Dialog implements OnInit {
	@Input() data: any;

	@ViewChild('providerForm', {read: NgForm, static: true}) providerForm: NgForm;

	public modalTitle: string;
	private dataSignature: string;
	protected isUnique = true;
	private providerName = new Subject<String>();

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private translatePipe: TranslatePipe,
		private providerService: ProviderService,
		private permissionService: PermissionService
	) {
		super();
	}

	ngOnInit(): void {
		console.log('init del regenerate');
		if (this.data) {
			console.log(this.data);
		}
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
			// this.dialogService.confirm(
			// 	this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
			// 	this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			// ).subscribe((result: any) => {
			// 	if (result.confirm === DialogConfirmAction.CONFIRM) {
			// 		this.onCancelClose();
			// 	}
			// });
			this.onCancelClose();
	}

	public cancelEditDialog(): void {
		this.onCancelClose();
		// if (this.isDirty()) {
		// 	this.dialogService.confirm(
		// 		this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
		// 		this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
		// 	).subscribe((result: any) => {
		// 		if (result.confirm === DialogConfirmAction.CONFIRM && !this.data.openFromList) {
		// 			// Put back original model
		// 			this.providerModel = JSON.parse(this.dataSignature);
		// 			this.dataSignature = JSON.stringify(this.providerModel);
		// 			this.modalType = this.actionTypes.VIEW;
		// 			this.setTitle(this.getModalTitle(this.modalType));
		// 		} else if (result.confirm === DialogConfirmAction.CONFIRM && this.data.openFromList) {
		// 			this.onCancelClose();
		// 		}
		// 	});
		// } else {
		// 	if (!this.data.openFromList) {
		// 		this.modalType = this.actionTypes.VIEW;
		// 		this.setTitle(this.getModalTitle(this.modalType));
		// 	} else {
		// 		this.onCancelClose();
		// 	}
		// }
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
