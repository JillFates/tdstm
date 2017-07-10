/**
 * Jorge Morayta
 * Defines the top-level states such as home, welcome, and login
 */
import { Injector } from '@angular/core';
import { TDSAppComponent } from './tds-app.component';
import { UIRouter, TransitionService } from '@uirouter/core';
import { AuthService } from '../shared/services/auth.service';
import { PermissionService } from '../shared/services/permission.service';
import { PreferenceService } from '../shared/services/preference.service';
import { UILoaderService } from '../shared/services/ui-loader.service';
import { SharedStates } from '../shared/shared-routing.states';
// Services
import { TaskService } from '../modules/taskManager/service/task.service';

export const tdsRoot = {
	name: 'tds',
	url: '',
	component: TDSAppComponent,
	resolve: [
		{
			token: 'taskCount',
			policy: { async: 'RXWAIT' },
			deps: [TaskService],
			resolveFn: (service: TaskService) => service.retrieveUserToDoCount()
		},
		{
			token: 'permissions',
			policy: { async: 'RXWAIT', when: 'EAGER' },
			deps: [PermissionService],
			resolveFn: (service: PermissionService) => service.getPermissions()
		},
		{
			token: 'preferences',
			policy: { async: 'RXWAIT', when: 'EAGER' },
			deps: [PreferenceService],
			resolveFn: (service: PreferenceService) => service.getPreference('CURR_DT_FORMAT')
		}
	]
};

function requiresAuthHook(transitionService: TransitionService) {
	// HookMatchCriteria
	const requiresAuthCriteria = {
		to: (state) => state.data && state.data.requiresAuth
	};

	const redirectToLogin = (transition) => {
		const authService: AuthService = transition.injector().get(AuthService);
		const $state = transition.router.stateService;
		if (!authService.isAuthenticated()) {
			return $state.target('login', undefined, { location: false });
		}
	};

	transitionService.onBefore(requiresAuthCriteria, redirectToLogin, { priority: 10 });
}

function requiresPermissionHook(transitionService: TransitionService) {
	const requiresPermissionCriteria = {
		to: (state) => state.data && state.data.requiresPermission
	};

	const redirectToUnauthorized = (transition) => {
		const permissionService = transition.injector().get(PermissionService) as PermissionService;
		const $state = transition.router.stateService;
		const reqPermission = transition.to().data.requiresPermission;
		const permited = typeof (reqPermission) === 'string' ?
			permissionService.hasPermission(reqPermission) :
			reqPermission.reduce((p, c) => p && permissionService.hasPermission(c), true);
		if (!permited) {
			return $state.target(SharedStates.UNAUTHORIZED.name, undefined, { location: false });
		}
	};

	transitionService.onStart(requiresPermissionCriteria, redirectToUnauthorized, { priority: 10 });
}

export function AuthConfig(router: UIRouter) {
	const transitionService = router.transitionService;
	requiresAuthHook(transitionService);
}

export function PermissionConfig(router: UIRouter) {
	const transitionService = router.transitionService;
	requiresPermissionHook(transitionService);
}

export function LoadingConfig(router: UIRouter) {
	const transitionService = router.transitionService;
	transitionService.onStart({
		to: (state) => state.data
	}, (transition) => {
		const loaderService = transition.injector().get(UILoaderService) as UILoaderService;
		loaderService.show();
	}, { priority: 10 });
	transitionService.onFinish({
		to: (state) => state.data
	}, (transition) => {
		const loaderService = transition.injector().get(UILoaderService) as UILoaderService;
		setTimeout(() => loaderService.hide());
	}, { priority: 10 });

	transitionService.onError({
		to: (state) => state.data
	}, (transition) => {
		const $state = transition.router.stateService;
		return $state.target(SharedStates.ERROR.name, undefined, { location: false });
	}, { priority: 10 });
}
export const TDSRoutingStates = [
	tdsRoot
];