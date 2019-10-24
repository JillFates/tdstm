// Angular
import {Injectable, OnInit} from '@angular/core';
import {Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';
// State
import {Logout} from '../action/login.actions';
import {Store} from '@ngxs/store';
// Services
import {PermissionService} from '../../../shared/services/permission.service';
// Models
import {APP_STATE_KEY} from '../../../shared/providers/localstorage.provider';
// Others
import {Observable} from 'rxjs';
import {WindowService} from '../../../shared/services/window.service';
import {TaskManagerRoutingStates} from '../../taskManager/task-manager-routing.states';
import {UserContextModel} from '../model/user-context.model';
import {UserContextService} from './user-context.service';
import {UserContextState} from '../state/user-context.state';

@Injectable()
export class AuthGuardService implements CanActivate {
	private userContext: UserContextModel;

	constructor(
		private appSettingsService: UserContextService,
		private permissionService: PermissionService,
		private windowService: WindowService,
		private router: Router,
		private store: Store) {
		this.getUserContext();
	}

	/**
	 * Guard Code to prevent the user to enter to the component if it does not fulfill requirements:
	 * 1.- Permissions
	 * @param route
	 * @param state
	 */
	canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | boolean {

		// TODO : Need to call /ws/user/updateLastPage sync with path=... -- if error then route to login

		let requiresLicense: boolean = route.data['requiresLicense'];
		if (requiresLicense && (!this.userContext.licenseInfo || !this.userContext.licenseInfo.license.isValid)) {
			this.windowService.getWindow().location.href = '/tdstm/errorHandler/licensing';
			return false;
		}
		let requiresPermission: string[] = route.data['requiresPermissions'];
		// Always get Permissions, even if the view does not have it in order to make it available for the component
		return this.permissionService.getPermissions().map(() => {
			if (requiresPermission && requiresPermission.length > 0) {
				requiresPermission.forEach((permission) => {
					if (!this.permissionService.hasPermission(permission)) {
						// Do not have permission to enter to this Route
						this.router.navigate(['/security/unauthorized']);
						return false;
					}
				});
			}
			return true;
		}).catch((err) => {
			// If you don't have permission, kick it from the application
			console.error('AuthGuardService:', err);
			localStorage.removeItem(APP_STATE_KEY);
			this.windowService.getWindow().location.href = '/tdstm/module/auth/login';
			return Observable.of(true);
		});
	}

	protected getUserContext(): void {
		this.userContext = this.store.selectSnapshot(UserContextState.getUserContext);
	}
}
