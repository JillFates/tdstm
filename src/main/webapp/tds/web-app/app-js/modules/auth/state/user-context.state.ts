// Store
import {Action, Selector, State, StateContext} from '@ngxs/store';
// Models
import {UserContextModel} from '../model/user-context.model';
// Actions
import {GetLicense, LoginInfo, Login, Logout, GetPermissions, SessionExpired} from '../action/login.actions';
import {PostNoticeRemove, PostNotices} from '../action/notice.actions';
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
import {SetTimeZoneAndDateFormat} from '../action/timezone-dateformat.actions';
import {PostNoticesService} from '../service/post-notices.service';
import {NoticeModel} from '../../noticeManager/model/notice.model';
import {SetUserContextPerson} from '../action/user-context-person.actions';

@State<UserContextModel>({
	name: 'userContext',
	defaults: {
		logged: false
	}
})
export class UserContextState {

	@Selector()
	static getTimezone(state: UserContextModel) {
		return state.timezone;
	}

	@Selector()
	static getDateFormat(state: UserContextModel) {
		return state.dateFormat;
	}

	@Selector()
	static getUserContext(state: UserContextModel) {
		return state;
	}

	/**
	 * Get the current Token for the CSRFT
	 * @param state
	 */
	@Selector()
	static getCSRFToken(state: UserContextModel) {
		return state.csrf;
	}

	constructor(
		private authService: AuthService,
		private permissionService: PermissionService,
		private postNoticesService: PostNoticesService,
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
					result['logged'] = true;
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
			ctx.setState({
				logged: false
			});
			return this.authService.logout().pipe(
				tap()
			);
		}
	}

	@Action(SessionExpired)
	sessionExpired(ctx: StateContext<UserContextModel>) {
		ctx.setState({
			logged: false
		});
	}

	@Action(GetPermissions)
	getPermissions(ctx: StateContext<UserContextModel>) {
		return this.permissionService.getPermissions().pipe(
			tap(result => {
				const state = ctx.getState();
				ctx.setState({
					...state,
					permissions: result
				});
			})
		);
	}

	@Action(GetLicense)
	getLicense(ctx: StateContext<UserContextModel>) {
		return this.userService.getLicense().pipe(
			tap(result => {
				const state = ctx.getState();
				ctx.setState({
					...state,
					license: result
				});
			}),
		);
	}

	@Action(PostNotices)
	postNotices(ctx: StateContext<UserContextModel>) {
		return this.postNoticesService.getPostNotices().pipe(
			tap(result => {
				const state = ctx.getState();
				ctx.setState({
					...state,
					postNotices: result
				});
			}),
		);
	}

	/**
	 * Removes one Notice from the List if it was Acknowledge
	 * @param ctx
	 */
	@Action(PostNoticeRemove)
	postNoticeRemove(ctx: StateContext<UserContextModel>, {payload}: PostNoticeRemove) {
		const state = ctx.getState();
		let postNotices = Object.assign([], state.postNotices);
		postNotices = postNotices.filter( (notice: NoticeModel) => notice.id !== payload.id);
		ctx.setState({
			...state,
			postNotices: postNotices
		});
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
		return this.userService.getLicense().pipe(
			tap(result => {
				const state = ctx.getState();
				ctx.setState({
					...state,
					license: result,
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
		let notices = state.notices;
		notices.redirectUrl = payload.path;
		ctx.setState({
			...state,
			notices: notices
		});
	}

	/**
	 * Set the timezone and dateFormat
	 * @param ctx
	 * @param payload
	 */
	@Action(SetTimeZoneAndDateFormat)
	setTimeZoneAndDateFormat(ctx: StateContext<UserContextModel>, {payload}: SetTimeZoneAndDateFormat) {
		const state = ctx.getState();
		ctx.setState({
			...state,
			dateFormat: payload.dateFormat,
			timezone: payload.timezone
		});
	}

	/**
	 * Set the User Context Person
	 * @param ctx
	 */
	@Action(SetUserContextPerson)
	setUserContext(ctx: StateContext<UserContextModel>) {
		const state = ctx.getState();
		return this.userService.getUserContext()
			.pipe(
				catchError(err => {
					ctx.setState({
						...state,
						error: err
					});
					return of(err);
				}))
			.subscribe(result => {
				const oldPerson = state && state.person;
				const update = result && result.person;
				if (oldPerson && update && (oldPerson.fullName !== update.fullName)) {
					ctx.patchState({ person: update});
				}
			});
	}
}
