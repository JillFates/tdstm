// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Components
import {ErrorPageComponent} from './components/error-page/error-page.component';
import {UnauthorizedPageComponent} from './components/unauthorized-page/unauthorized-page.component';
import {NotFoundPageComponent} from './components/not-found-page/not-found-page.component';

/**
 * Security Route States
 * @class
 * @classdesc To use externally to reference possible state of the Security Model
 */
export class SecurityRouteStates {
	public static readonly PARENT = 'security';
	public static readonly ERROR = {
		url: 'error'
	};
	public static readonly UNAUTHORIZED = {
		url: 'unauthorized'
	};
	public static readonly NOT_FOUND = {
		url: 'notfound'
	};
}

// routes
export const SecurityRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: SecurityRouteStates.NOT_FOUND.url},
	{
		path: SecurityRouteStates.NOT_FOUND.url,
		data: {
			page: {
				title: '', instruction: '', menu: []
			}
		},
		component: NotFoundPageComponent
	},
	{
		path: SecurityRouteStates.ERROR.url,
		data: {
			page: {
				title: '', instruction: '', menu: []
			}
		},
		component: ErrorPageComponent
	},
	{
		path: SecurityRouteStates.UNAUTHORIZED.url,
		data: {
			page: {
				title: '', instruction: '', menu: []
			}
		},
		component: UnauthorizedPageComponent
	},
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(SecurityRoute)]
})

export class SecurityRouteModule {
}