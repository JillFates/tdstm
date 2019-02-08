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
	templateUrl: '../tds/web-app/app-js/shared/modules/header/components/license-warning/license-warning.component.html',
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
		jQuery('.licensing-error-warning').popover({placement: 'bottom', container: 'body' });
		jQuery('.licensing-error-warning').click(function(event) { event.preventDefault(); });
	}

	protected getUserContext(): void {
		this.appSettingsService.getUserContext().subscribe( (userContext: UserContextModel) => {
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