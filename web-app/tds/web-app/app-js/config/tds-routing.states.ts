/**
 * Jorge Morayta
 * Defines the top-level states such as home, welcome, and login
 */
import {TDSAppComponent} from './tds-app.component';
import {UIRouter, TransitionService} from '@uirouter/core';
import {AuthService} from '../shared/services/auth.service';
// Services
import {TaskService} from '../modules/taskManager/service/task.service';

export const tdsRoot = {
	name: 'tds',
	url: '',
	component: TDSAppComponent,
	resolve: [
		{
			token: 'taskCount',
			policy: {async: 'RXWAIT'},
			deps: [TaskService],
			resolveFn: (service: TaskService) => service.retrieveUserToDoCount()
		}
	]
};

export function requiresAuthHook(transitionService: TransitionService) {
	// HookMatchCriteria
	const requiresAuthCriteria = {
		to: (state) => state.data && state.data.requiresAuth
	};

	const redirectToLogin = (transition) => {
		const authService: AuthService = transition.injector().get(AuthService);
		const $state = transition.router.stateService;
		if (!authService.isAuthenticated()) {
			return $state.target('login', undefined, {location: false});
		}
	};

	transitionService.onBefore(requiresAuthCriteria, redirectToLogin, {priority: 10});
}

export function AuthConfig(router: UIRouter) {
	const transitionService = router.transitionService;
	requiresAuthHook(transitionService);
}

export const TDSRoutingStates = [
	tdsRoot
];