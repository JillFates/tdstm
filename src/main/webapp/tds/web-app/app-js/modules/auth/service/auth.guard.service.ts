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

@Injectable()
export class AuthGuardService implements CanActivate, OnInit {

	public licenseRequiredUrls = [TaskManagerRoutingStates.TASK_MANAGER_LIST]

	constructor(
		private permissionService: PermissionService,
		private windowService: WindowService,
		private router: Router,
		private store: Store) {
	}

	ngOnInit() {

	}

	/**
	 * Guard Code to prevent the user to enter to the component if it does not fulfill requirements:
	 * 1.- Permissions
	 * @param route
	 * @param state
	 */
	canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
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
}
