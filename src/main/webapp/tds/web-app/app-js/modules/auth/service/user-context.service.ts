/**
 * User Context is an early service injected at the beginning of the App that initialize the App Object that contains
 * multiple information necessary to run the application, like permissions, user info, license management, etc.
 */
// Angular
import {Injectable} from '@angular/core';
// Model
import {UserContextModel, USER_CONTEXT_REQUEST} from '../model/user-context.model';
// Services
import {UserService} from './user.service';
import {PermissionService} from '../../../shared/services/permission.service';
import {AuthService} from './auth.service';
// Others
import {BehaviorSubject, Observable} from 'rxjs';

@Injectable()
export class UserContextService {

	private userContextSubject = new BehaviorSubject(new UserContextModel());
	private userContext = this.userContextSubject.asObservable();

	constructor(
		private userService: UserService,
		private permissionService: PermissionService,
		private authService: AuthService) {
	}

	/**
	 * Get the User Context
	 */
	public getUserContext(): Observable<UserContextModel> {
		return Observable.from(this.userContext);
	}

	/**
	 * Being call in the Bootstrap of the Application
	 */
	public initializeUserContext() {
		if (this.authService.isUserAuthenticated()) {
			let contextPromises = [];
			contextPromises.push(this.userService.getUserContext());
			contextPromises.push(this.userService.getLicenseInfo());
			contextPromises.push(this.permissionService.getPermissions());

			return new Promise((resolve) => {
				Observable.forkJoin(contextPromises)
					.subscribe((contextResponse: any) => {
						let userContext = contextResponse[USER_CONTEXT_REQUEST.USER_INFO];
						userContext.licenseInfo = contextResponse[USER_CONTEXT_REQUEST.LICENSE_INFO];
						userContext.permissions = contextResponse[USER_CONTEXT_REQUEST.PERMISSIONS];
						this.userContextSubject.next(userContext);
						resolve(true);
					});
			});
		} else {
			// Both actions get resolved but one contains the entire Use Context
			return new Promise((resolve) => {
				resolve(true);
			});
		}
	}
}