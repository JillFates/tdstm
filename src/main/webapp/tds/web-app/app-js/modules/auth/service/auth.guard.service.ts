// Angular
import {Injectable} from '@angular/core';
import {Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';
// Services
import {PermissionService} from '../../../shared/services/permission.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class AuthGuardService implements CanActivate {

	constructor(private permissionService: PermissionService, private router: Router) {
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
			console.error('AuthGuardService:', err);
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}