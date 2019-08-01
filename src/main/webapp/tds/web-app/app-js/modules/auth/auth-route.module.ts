// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Components
import {LoginComponent} from './components/login/login.component';
import {ForgotPasswordComponent} from './components/forgot-password/forgot-password.component';
import {ChangePasswordComponent} from './components/change-password/change-password.component';

/**
 * Auth Route States
 * @class
 * @classdesc To use externally to reference possible state of the Auth Module
 */
export class AuthRouteStates {
	public static readonly LOGIN = {
		url: 'auth/login'
	};
	public static readonly FORGOT_PASSWORD = {
		url: 'auth/forgot'
	};
	public static readonly CHANGE_PASSWORD = {
		url: 'auth/changePassword'
	};
}

export const LoginRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: AuthRouteStates.LOGIN.url},
	{
		path: AuthRouteStates.LOGIN.url,
		data: {
			page: {
				hideTopNav: true,
				title: 'LOGIN.LOGIN', instruction: '', menu: []
			}
		},
		component: LoginComponent
	},
	{
		path: AuthRouteStates.FORGOT_PASSWORD.url,
		data: {
			page: {
				hideTopNav: true,
				title: 'LOGIN.FORGOT_PASSWORD', instruction: '', menu: []
			}
		},
		component: ForgotPasswordComponent
	},
	{
		path: AuthRouteStates.CHANGE_PASSWORD.url,
		data: {
			page: {
				hideTopNav: true,
				title: 'LOGIN.CHANGE_PASSWORD', instruction: '', menu: []
			}
		},
		component: ChangePasswordComponent
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(LoginRoute)]
})

export class AuthRouteModule {
}
