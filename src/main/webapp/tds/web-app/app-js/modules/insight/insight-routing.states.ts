// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// import {EventsResolveService} from './resolve/events-resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
// Components
import {InsightDashboardComponent} from './components/dashboard/insight-dashboard.component';

export class InsightStates {
	public static readonly EVENT_LIST = {
		url: 'list'
	};
	public static readonly INSIGHT_DASHBOARD = {
		url: 'dashboard'
	};
}

export const InsightRoute: Routes = [
	// {path: '', pathMatch: 'full', redirectTo: InsightStates.EVENT_LIST.url},
	// {
	// 	path: InsightStates.EVENT_LIST.url,
	// 	data: {
	// 		page: {
	// 			title: 'EVENT.LIST',
	// 			instruction: '',
	// 			menu: ['PLANNING.PLANNING', 'EVENT.LIST']
	// 		},
	// 		requiresAuth: true,
	// 	},
	// 	component: EventListComponent,
	// 	resolve: {
	// 		events: EventsResolveService
	// 	},
	// 	canActivate: [AuthGuardService, ModuleResolveService]
	// },
	// {path: '', pathMatch: 'full', redirectTo: InsightStates.INSIGHT_DASHBOARD.url},
	{
		path: InsightStates.INSIGHT_DASHBOARD.url,
		data: {
			page: {
				title: 'EVENT.TITLE_DASHBOARD',
				instruction: '',
				menu: ['EVENT.DASHBOARD', 'EVENT.EVENT']
			},
			requiresAuth: true,
			requiresLicense: true
		},
		component: InsightDashboardComponent,
		// canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(InsightRoute)]
})

export class InsightRouteModule {
}