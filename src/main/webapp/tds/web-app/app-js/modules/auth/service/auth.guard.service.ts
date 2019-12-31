// Angular
import {Injectable} from '@angular/core';
import {Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';
// State
import {Logout} from '../action/login.actions';
import {Store} from '@ngxs/store';
import {SetPageChange} from '../action/page.actions';
import {UserContextState} from '../state/user-context.state';
// Services
import {PermissionService} from '../../../shared/services/permission.service';
import {WindowService} from '../../../shared/services/window.service';
import {UserContextService} from './user-context.service';
import {PageService} from './page.service';
// Models
import {UserContextModel} from '../model/user-context.model';
// Others
import {Observable} from 'rxjs';
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
	canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
		let requestedPath = `/module${state.url}`;

		let guardRequests = [
			this.pageService.updateLastPage(requestedPath),
			this.permissionService.getPermissions()
		];

		let guardResults = Observable.forkJoin(guardRequests).pipe(
			map(([successToSaveLastPage, permissions]) => {
				if (!successToSaveLastPage) {
					// console.error('AuthGuardService:', 'Session has expired');
					this.store.dispatch(new Logout());
					return Observable.of(false);
				}
				// Save last Page Requested
				this.store.dispatch(new SetPageChange({path: requestedPath}));

				this.userContext = this.store.selectSnapshot(UserContextState.getUserContext);
				let requiresLicense: boolean = route.data['requiresLicense'];
				if (requiresLicense && (!this.userContext.license || !this.userContext.license.isValid)) {
					this.router.navigate(['/security/licenseNotFound']);
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

		// Need to convert the Observable back to a Promise that canActivate requires
		return new Promise((resolve) => {
			return guardResults.subscribe((result: any) => {
				if (result.value) {
					resolve(true);
				} else {
					resolve(false);
				}
			})
		});
	}
}
