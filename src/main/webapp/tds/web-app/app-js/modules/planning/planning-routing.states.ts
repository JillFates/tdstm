// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
// Components
import {PlanningDashboardComponent} from './components/dashboard/planning-dashboard.component';

/**
 * Top menu parent section class.
 * @type {string}
 */
const TOP_MENU_PARENT_SECTION = 'menu-parent-dashboard';

export class PlanningStates {
	public static readonly PLANNING_DASHBOARD = {
		url: 'dashboard'
	};
}

export const PlanningRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: PlanningStates.PLANNING_DASHBOARD.url},
	{
		path: PlanningStates.PLANNING_DASHBOARD.url,
		data: {
			page: {
				title: 'Planning Dashboard',
				menu: ['PLANNING.DASHBOARD', 'PLANNING.PLANNING'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-dashboard-planning-dashboard', subMenu: true}
			},
			requiresAuth: true,
		},
		component: PlanningDashboardComponent,
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(PlanningRoute)]
})

export class PlanningRouteModule {
}
