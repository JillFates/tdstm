import { Ng2StateDeclaration } from '@uirouter/angular';

import { ErrorPageComponent } from '../modules/security/errorPage/error-page.component';
import { UnauthorizedPageComponent } from '../modules/security/unauthorizedPage/unauthorized-page.component';
import { NotFoundPageComponent } from '../modules/security/notFoundPage/not-found-page.component';
import { HeaderComponent } from './modules/header/header.component';

/**
 * Task States
 * @class
 * @classdesc Represent the possible states and access on Task Routing
 */
export class SharedStates {
	public static readonly ERROR = {
		name: 'tds.error',
		url: '/pages/error'
	};
	public static readonly UNAUTHORIZED = {
		name: 'tds.unauthorized',
		url: '/pages/unauthorized'
	};
	public static readonly NOT_FOUND = {
		name: 'tds.notfound',
		url: '/pages/notfound'
	};
}

export const errorPageState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: SharedStates.ERROR.name,
	url: SharedStates.ERROR.url,
	data: {
		page: {
			title: '', instruction: '', menu: []
		},
		requiresAuth: true
	},
	views: {
		'headerView@tds': { component: HeaderComponent },
		'containerView@tds': { component: ErrorPageComponent }
	}
};

export const unauthorizedPageState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: SharedStates.UNAUTHORIZED.name,
	url: SharedStates.UNAUTHORIZED.url,
	data: {
		page: {
			title: '', instruction: '', menu: []
		},
		requiresAuth: true
	},
	views: {
		'headerView@tds': { component: HeaderComponent },
		'containerView@tds': { component: UnauthorizedPageComponent }
	}
};

export const notFoundPageState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: SharedStates.NOT_FOUND.name,
	url: SharedStates.NOT_FOUND.url,
	data: {
		page: {
			title: '', instruction: '', menu: []
		},
		requiresAuth: true
	},
	views: {
		'headerView@tds': { component: HeaderComponent },
		'containerView@tds': { component: NotFoundPageComponent }
	}
};

export const SHARED_STATES = [
	errorPageState,
	unauthorizedPageState,
	notFoundPageState
];