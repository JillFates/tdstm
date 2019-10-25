// Store
import {Action, Selector, State, StateContext} from '@ngxs/store';
// Models
import {UserContextModel} from '../model/user-context.model';
// Actions
import {LicenseInfo, LoginInfo, Login, Logout, Permissions, SessionExpired, PostNotices} from '../action/login.actions';
import {SetEvent} from '../../event/action/event.actions';
import {SetBundle} from '../../bundle/action/bundle.actions';
import {SetProject} from '../../project/actions/project.actions';
import {SetPageChange} from '../action/page.actions';
// Services
import {AuthService} from '../service/auth.service';
import {PermissionService} from '../../../shared/services/permission.service';
import {UserService} from '../service/user.service';
import {LoginService} from '../service/login.service';
import {PostNoticesManagerService} from '../service/post-notices-manager.service';
// Others
import {tap, catchError} from 'rxjs/operators';
import {of} from 'rxjs';

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
		private postNoticesManagerService: PostNoticesManagerService,
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
		const state = ctx.getState();
		return this.authService.getUserContext({payload})
			.pipe(
				tap(result => {
					ctx.patchState(result);
				}),
				catchError(err => {
					ctx.setState({
						...state,
						error: err
					});
					return of(err);
				})
			);
	}

	@Action(Logout)
	logout(ctx: StateContext<UserContextModel>) {
		const state = ctx.getState();
		if (state.user) {
			ctx.setState({});
			return this.authService.logout().pipe(
				tap()
			);
		}
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

	@Action(PostNotices)
	postNotices(ctx: StateContext<UserContextModel>) {
		const state = ctx.getState();
		return this.postNoticesManagerService.getNotices().pipe(
			tap(result => {
				ctx.setState({
					...state,
					postNotices: result
				});
			}),
		);
	}

	@Action(SetEvent)
	setEvent(ctx: StateContext<UserContextModel>, {payload}: SetEvent) {
		const state = ctx.getState();
		ctx.setState({
			...state,
			event: payload,
			bundle: null
		});
	}

	@Action(SetBundle)
	setBundle(ctx: StateContext<UserContextModel>, {payload}: SetBundle) {
		const state = ctx.getState();
		ctx.setState({
			...state,
			bundle: payload,
		});
	}

	@Action(SetProject)
	setProject(ctx: StateContext<UserContextModel>, {payload}: SetProject) {
		const state = ctx.getState();
		return this.userService.getLicenseInfo().pipe(
			tap(result => {
				ctx.setState({
					...state,
					licenseInfo: result,
					project: payload,
					event: null,
					bundle: null,
					alternativeProjects: []
				});
			}),
		);
	}

	/**
	 * Set the new Latest Page to the context
	 * @param ctx
	 * @param payload
	 */
	@Action(SetPageChange)
	setPageChange(ctx: StateContext<UserContextModel>, {payload}: SetPageChange) {
		const state = ctx.getState();
		return this.userService.getLicenseInfo().pipe(
			tap(result => {
				let notices = state.notices;
				notices.redirectUrl = payload.path;
				ctx.setState({
					...state,
					notices: notices
				});
			}),
		);
	}
}
