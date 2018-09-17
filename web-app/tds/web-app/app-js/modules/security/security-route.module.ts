import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Components
import {ErrorPageComponent} from './errorPage/error-page.component';
import {UnauthorizedPageComponent} from './unauthorizedPage/unauthorized-page.component';
import {NotFoundPageComponent} from './notFoundPage/not-found-page.component';

/**
 * Task States
 * @class
 * @classdesc Represent the possible states and access on Task Routing
 */
export class SecurityRouteStates {
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

// routes
export const SecurityRoute: Routes = [
	{
		path: SecurityRouteStates.ERROR.url,
		data: {
			page: {
				title: '', instruction: '', menu: []
			},
			requiresAuth: true
		},
		component: ErrorPageComponent
	},
	{
		path: SecurityRouteStates.UNAUTHORIZED.url,
		data: {
			page: {
				title: '', instruction: '', menu: []
			},
			requiresAuth: true
		},
		component: UnauthorizedPageComponent
	},
	{
		path: SecurityRouteStates.NOT_FOUND.url,
		data: {
			page: {
				title: '', instruction: '', menu: []
			},
			requiresAuth: true
		},
		component: NotFoundPageComponent
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(SecurityRoute)]
})

export class SecurityRouteModule {
}