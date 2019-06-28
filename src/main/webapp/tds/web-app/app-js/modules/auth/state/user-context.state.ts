// Store
import {Action, Selector, State, StateContext} from '@ngxs/store';
// Models
import {UserContextModel} from '../model/user-context.model';
// Actions
import {LicenseInfo, LoginInfo, Login, Logout, Permissions, SessionExpired} from '../action/login.actions';
// Services
import {AuthService} from '../service/auth.service';
import {PermissionService} from '../../../shared/services/permission.service';
import {UserService} from '../service/user.service';
import {LoginService} from '../service/login.service';
// Others
import {tap} from 'rxjs/operators';

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
		private loginService: LoginService,
		private userService: UserService) {
	}

	@Action(LoginInfo)
	loginInfo(ctx: StateContext<UserContextModel>, {payload}: LoginInfo) {
		const state = ctx.getState();
		ctx.setState({
			...state,
			buildVersion: payload.buildVersion
		});
	}

	@Action(Login)
	login(ctx: StateContext<UserContextModel>, {payload}: Login) {
		return this.authService.getUserContext({payload}).pipe(
			tap(result => {
				ctx.patchState(result);
			})
		);
	}

	@Action(Logout)
	logout(ctx: StateContext<UserContextModel>) {
		ctx.setState({});
		return this.authService.logout().pipe(
			tap()
		);
	}

	@Action(SessionExpired)
	sessionExpired(ctx: StateContext<UserContextModel>) {
		ctx.setState({});
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
