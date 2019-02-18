// Angular
import {AfterContentInit, Component} from '@angular/core';
// Model
import {UserContextModel} from '../../../../../modules/security/model/user-context.model';
import {Permission} from '../../../../model/permission.model';
// Service
import {UserContextService} from '../../../../../modules/security/services/user-context.service';
import {PermissionService} from '../../../../services/permission.service';

declare var jQuery: any;

@Component({
	selector: 'tds-license-warning',
	template: `
        <div class="license-warning-component">
            <a class='licensing-error-warning btn' href="#"
               data-html="true"
               data-toggle="popover"
               tabindex="0"
               attr.data-trigger="focus"
               attr.data-content="
		            <div class='license-warning-message' style='word-wrap: break-word;'>
		                <p>{{userContext.licenseInfo.license.message}}</p>
		            </div>
		            <div class='license-warning-message-button'>
		                <button type='button' class='btn btn-primary' onClick='location.href=&quot;/tdstm/module/license/admin/list&quot;'>Administer License</button>
		            </div>
        	">
                <i class="fa fa-fw fa-warning licensing-error-warning"></i>
            </a>
        </div>
	`
})

export class LicenseWarningComponent implements AfterContentInit {
	protected userContext: UserContextModel;
	protected permission: Permission = Permission;

	constructor(
		private appSettingsService: UserContextService,
		private permissionService: PermissionService) {
		this.getUserContext();
	}

	ngAfterContentInit(): void {
		jQuery('.licensing-error-warning').popover({placement: 'bottom', container: 'body'});
		jQuery('.licensing-error-warning').click(function (event) {
			event.preventDefault();
		});
	}

	protected getUserContext(): void {
		this.appSettingsService.getUserContext().subscribe((userContext: UserContextModel) => {
			this.userContext = userContext;
		});
	}

	/**
	 * Validate if the User has the proper permission to access the Menu Item
	 * @param value
	 */
	protected hasPermission(value: string): boolean {
		return this.permissionService.hasPermission(value);
	}
}