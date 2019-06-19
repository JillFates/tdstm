// Store
import {Action, Selector, State, StateContext} from '@ngxs/store';
// Models
import {USER_CONTEXT_REQUEST, UserContextModel} from '../model/user-context.model';
// Actions
import {LicenseInfo, Login, Logout, Permissions} from '../action/login.actions';
// Services
import {AuthService} from '../service/auth.service';
import {tap} from 'rxjs/operators';
import {PermissionService} from '../../../shared/services/permission.service';
import {UserService} from '../service/user.service';
import {Observable, from} from 'rxjs';
import {WindowService} from '../../../shared/services/window.service';

@State<UserContextModel>({
	name: 'userContext',
	defaults: {}
})
export class UserContextState {

	@Selector()
	static getUserContext(state: UserContextModel) {
		return state;
	}

	constructor(
		private authService: AuthService,
		private permissionService: PermissionService,
		private userService: UserService,
		private windowService: WindowService) {
	}

	@Action(Login)
	login(ctx: StateContext<UserContextModel>, {payload}: Login) {
		return this.authService.login({payload}).subscribe(userContext => {
			let contextPromises = [];
			contextPromises.push(from(new Promise(resolve => resolve(userContext))));
			contextPromises.push(this.userService.getLicenseInfo());
			contextPromises.push(this.permissionService.getPermissions());

			return new Promise((resolve) => {
				Observable.forkJoin(contextPromises)
					.subscribe((contextResponse: any) => {
						let userContext = contextResponse[USER_CONTEXT_REQUEST.USER_INFO];
						userContext.licenseInfo = contextResponse[USER_CONTEXT_REQUEST.LICENSE_INFO];
						userContext.permissions = contextResponse[USER_CONTEXT_REQUEST.PERMISSIONS];
						ctx.patchState(userContext);
						resolve();
					});
			});
		});
	}

	@Action(Logout)
	logout(ctx: StateContext<UserContextModel>) {
		ctx.setState({});
		this.windowService.getWindow().location.href = '/tdstm/auth/signOut';
	}

	@Action(Permissions)
	permissions(ctx: StateContext<UserContextModel>) {
		const state = ctx.getState();
		return this.permissionService.getPermissions().pipe(
			tap(result => {
				ctx.setState({
					...state,
					permissions: result
				});
			})
		);
	}

	@Action(LicenseInfo)
	licenseInfo(ctx: StateContext<UserContextModel>) {
		const state = ctx.getState();
		return this.userService.getLicenseInfo().pipe(
			tap(result => {
				ctx.setState({
					...state,
					licenseInfo: result
				});
			}),
		);
	}

}
