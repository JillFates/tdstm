// Angular
import {Component, Input, OnInit, ViewChild} from '@angular/core';
// Service
import {NotifierService} from '../../../../shared/services/notifier.service';
import {LicenseAdminService} from '../../service/license-admin.service';
// Component
import {CopyClipboardDirective} from '../../../../shared/directives/copy-clipboard.directive';
// Model
import {LicenseModel} from '../../model/license.model';
import {Dialog, DialogButtonType, DialogService} from 'tds-component-library';
// Other
import 'rxjs/add/operator/finally';

@Component({
	selector: 'tds-license-manual-request',
	templateUrl: 'manual-request.component.html'
})
export class ManualRequestComponent extends Dialog implements OnInit {
	@Input() data: any;

	@ViewChild(CopyClipboardDirective, {static: false}) public copyClipboard: CopyClipboardDirective;

	private licenseModel: LicenseModel;
	public licenseEmail: any = {};

	constructor(
		private notifierService: NotifierService,
		private dialogService: DialogService,
		private licenseAdminService: LicenseAdminService
	) {
		super();
	}

	ngOnInit(): void {
		this.licenseModel = Object.assign({}, this.data.licenseModel);

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			tooltipText: 'Close',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.onCancelClose.bind(this)
		});

		this.buttons.push({
			name: 'emailRequest',
			icon: 'details',
			text: 'Email Request',
			show: () => true,
			type: DialogButtonType.CONTEXT,
			action: this.emailRequestTo.bind(this)
		});

		this.buttons.push({
			name: 'ccClipboard',
			icon: 'copy',
			text: 'Copy to Clipboard',
			show: () => true,
			type: DialogButtonType.CONTEXT,
			action: this.copyToClipBoard.bind(this)
		});

		this.licenseAdminService.getEmailContent(this.licenseModel.id).subscribe((result: any) => {
			this.licenseEmail = result;
		});
	}

	/**
	 * Email Request to
	 */
	public emailRequestTo(): void {
		window.location.href = `mailto:${this.licenseEmail.toEmail}?cc=${this.licenseEmail.ccEmail}&subject=${this.licenseEmail.subject}&body=${encodeURI(this.licenseEmail.body)}`;
	}

	/**
	 * Copy to Clipboard
	 */
	public copyToClipBoard(): void {
		this.copyClipboard.copy();
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.onCancelClose();
	}

}
