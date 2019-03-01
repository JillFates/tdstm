// Angular
import {Component} from '@angular/core';
// Model
import {UserContextModel} from '../../../../../modules/security/model/user-context.model';
import {Permission} from '../../../../model/permission.model';
// Service
import {UserContextService} from '../../../../../modules/security/services/user-context.service';
import {PermissionService} from '../../../../services/permission.service';

@Component({
	selector: 'tds-tranman-menu',
	templateUrl: 'tranman-menu.component.html',
})

export class TranmanMenuComponent {
	protected permission: Permission = Permission;
	public userContext: UserContextModel;

	constructor(
		private appSettingsService: UserContextService,
		private permissionService: PermissionService) {
		this.getUserContext();
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