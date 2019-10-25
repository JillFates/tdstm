// Angular
import {Injectable} from '@angular/core';
import {Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';
// State
import {Logout} from '../action/login.actions';
import {Store} from '@ngxs/store';
// Services
import {PermissionService} from '../../../shared/services/permission.service';
// Others
import { Observable} from 'rxjs';
import {WindowService} from '../../../shared/services/window.service';
import {UserContextModel} from '../model/user-context.model';
import {UserContextService} from './user-context.service';
import {UserContextState} from '../state/user-context.state';
import {PageService} from './page.service';
import {catchError, map} from 'rxjs/operators';

@Injectable()
export class AuthGuardService implements CanActivate {
	private userContext: UserContextModel;

	constructor(
		private appSettingsService: UserContextService,
		private permissionService: PermissionService,
		private windowService: WindowService,
		private pageService: PageService,
		private router: Router,
		private store: Store) {
	}

	/**
	 * Guard Code to prevent the user to enter to the component if it does not fulfill requirements:
	 * @param route
	 * @param state
	 */
	canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): any {
		let guard = [
			this.pageService.updateLastPage(`module${state.url}`),
			this.permissionService.getPermissions()
		];
		return Observable.forkJoin(guard).pipe(
			map(([successToSaveLastPage, permissions]) => {
				if (!successToSaveLastPage) {
					console.error('AuthGuardService:', 'Session has expired');
					this.store.dispatch(new Logout());
					return Observable.of(false);
				}

				this.userContext = this.store.selectSnapshot(UserContextState.getUserContext);
				let requiresLicense: boolean = route.data['requiresLicense'];
				if (requiresLicense && (!this.userContext.licenseInfo || !this.userContext.licenseInfo.license.isValid)) {
					this.windowService.getWindow().location.href = '/tdstm/errorHandler/licensing';
					return Observable.of(false);
				}

				let requiresPermission: string[] = route.data['requiresPermissions'];
				if (permissions) {
					if (requiresPermission && requiresPermission.length > 0) {
						requiresPermission.forEach((permission) => {
							if (!this.permissionService.hasPermission(permission)) {
								// Do not have permission to enter to this Route
								this.router.navigate(['/security/unauthorized']);
								return Observable.of(false);
							}
						});
					}
					return Observable.of(true);
				}
			}),
			catchError(() => {
				console.error('AuthGuardService:', 'An error occurred while saving the Last Page');
				this.store.dispatch(new Logout());
				return Observable.of(false);
			})
		);
	}
}
