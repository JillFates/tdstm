import {Injectable} from '@angular/core';
import {
	Router, CanActivate,
	ActivatedRouteSnapshot,
	RouterStateSnapshot
} from '@angular/router';
import {PermissionService} from '../../../shared/services/permission.service';
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
		return this.permissionService.getPermissions().map(() => {
			requiresPermission.forEach((permission) => {
				if (!this.permissionService.hasPermission(permission)) {
					return false;
				}
			});
			return true;
		}).catch(() => {
			this.router.navigate(['/login']);
			return Observable.of(false);
		});
	}
}