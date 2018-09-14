/**
 * Jorge Morayta 09/13/2018
 * Refactored to use Native Angular routing
 */

/*
import { TDSAppComponent } from './tds-app.component';
import { AuthService } from '../shared/services/auth.service';

import { PermissionService } from '../shared/services/permission.service';
import { PreferenceService, PREFERENCES_LIST} from '../shared/services/preference.service';
import { UILoaderService } from '../shared/services/ui-loader.service';
import { UIPromptService } from '../shared/directives/ui-prompt.directive';
import { SharedStates } from '../shared/shared-routing.states';
// Services
import { TaskService } from '../modules/taskManager/service/task.service';
import { DictionaryService } from '../shared/services/dictionary.service';
import { LAST_VISITED_PAGE } from '../shared/model/constants';

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

			resolveFn: (service: PreferenceService) => service.getPreferences(
				PREFERENCES_LIST.CURR_TZ,
				PREFERENCES_LIST.CURRENT_DATE_FORMAT
			)
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

export function MiscConfig(router: UIRouter) {
	router.stateService.defaultErrorHandler((error) => {
		console.log(error);
		router.stateService.go(SharedStates.ERROR.name);
	});

	const transitionService = router.transitionService;
	transitionService.onStart({
		to: (state) => state.data,
		exiting: (state) => state.data && !state.data.hasPendingChanges
	}, (transition) => {
		const loaderService = transition.injector().get(UILoaderService) as UILoaderService;
		const dictionaryService = transition.injector().get(DictionaryService) as DictionaryService;
		dictionaryService.set(LAST_VISITED_PAGE, transition.from().name);
		loaderService.show();
	}, { priority: 10 });
	transitionService.onFinish({
		to: (state) => state.data
	}, (transition) => {
		const loaderService = transition.injector().get(UILoaderService) as UILoaderService;
		setTimeout(() => loaderService.hide());
	}, { priority: 10 });

	transitionService.onExit({
		exiting: (state) => state.data && state.data.hasPendingChanges
	}, (transition) => {
		const promptService = transition.injector().get(UIPromptService) as UIPromptService;
		let target = transition.to();
		const params = transition['_targetState']['_params'];
		const $state = transition.router.stateService;
		promptService.open(
			'Confirmation Required',
			'You have changes that have not been saved. Do you want to continue and lose those changes?',
			'Confirm', 'Cancel').then(result => {
				if (result) {
					$state.$current.data.hasPendingChanges = false;
					$state.go(target.name, params);
				}
			});
		return false;
	}, { priority: 10 });
}
export const TdsAppRoute = [
	tdsRoot
];
*/

import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

export const TDSAppRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: 'dashboard'},
	{path: 'dashboard', loadChildren: '../modules/lazyTestModule/lazy-test.module#LazyTestModule'},
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forRoot(TDSAppRoute)]
})

export class TDSAppRouteModule {
}