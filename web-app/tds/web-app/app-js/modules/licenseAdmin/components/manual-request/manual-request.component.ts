// Angular
import {Component, OnInit} from '@angular/core';
// Service
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {LicenseAdminService} from '../../service/license-admin.service';
// Model
import {LicenseModel} from '../../model/license.model';
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';
import {AlertType} from '../../../../shared/model/alert.model';
// Other
import 'rxjs/add/operator/finally';

@Component({
	selector: 'tds-license-manual-request',
	templateUrl: '../tds/web-app/app-js/modules/licenseAdmin/components/manual-request/manual-request.component.html'
})
export class ManualRequestComponent extends UIExtraDialog implements OnInit {

	protected modalOptions: DecoratorOptions;
	protected licenseEmail = {};

	constructor(
		private licenseModel: LicenseModel,
		private notifierService: NotifierService,
		private promptService: UIPromptService,
		private licenseAdminService: LicenseAdminService) {
		super('#licenseManualRequest');
		this.modalOptions = {isFullScreen: false, isResizable: false};
	}

	ngOnInit(): void {
		this.licenseAdminService.getEmailContent(this.licenseModel.id).subscribe((result: any) => {
			this.licenseEmail = result;
		});
	}

	protected cancelCloseDialog($event): void {
		this.dismiss();
	}

}
