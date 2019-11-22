// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
// Components
import {UserListComponent} from './components/list/user-list.component';
import {UserDashboardComponent} from './components/dashboard/user-dashboard.component';

/**
 * Top menu parent section class.
 * @type {string}
 */
const TOP_MENU_PARENT_SECTION = 'menu-parent-dashboard';

export class UserStates {
	public static readonly USER_LIST = {
		url: 'list'
	};
	public static readonly USER_DASHBOARD = {
		url: 'dashboard'
	};
}

export const UserRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: UserStates.USER_LIST.url},
	{
		path: UserStates.USER_LIST.url,
		data: {
			page: {
				title: 'User List',
				instruction: ''
			},
			requiresAuth: true,
		},
		component: UserListComponent,
		canActivate: [AuthGuardService, ModuleResolveService]
	},
	{
		path: UserStates.USER_DASHBOARD.url,
		data: {
			page: {
				title: 'User Dashboard',
				instruction: '',
				menu: ['Dashboard', 'User'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-dashboard-user-dashboard', subMenu: true}
			},
			requiresAuth: true,
		},
		component: UserDashboardComponent,
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(UserRoute)]
})

export class UserRouteModule {
}
