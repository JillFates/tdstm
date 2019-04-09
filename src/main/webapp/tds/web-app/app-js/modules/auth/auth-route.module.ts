// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Components
import {LoginComponent} from './components/login/login.component';

/**
 * Auth Route States
 * @class
 * @classdesc To use externally to reference possible state of the Auth Module
 */
export class AuthRouteStates {
	public static readonly LOGIN = {
		url: 'login'
	};
}

export const LoginRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: AuthRouteStates.LOGIN.url},
	{
		path: AuthRouteStates.LOGIN.url,
		data: {
			page: {
				title: '', instruction: '', menu: []
			}
		},
		component: LoginComponent
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(LoginRoute)]
})

export class AuthRouteModule {
}