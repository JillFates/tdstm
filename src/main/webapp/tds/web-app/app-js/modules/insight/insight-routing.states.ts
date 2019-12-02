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

	public static readonly INSIGHT_DASHBOARD = {
		url: 'dashboard'
	};
}

export const InsightRoute: Routes = [

	{
		path: InsightStates.INSIGHT_DASHBOARD.url,
		data: {
			page: {
				title: 'INSIGHT.TITLE_DASHBOARD',
				instruction: '',
				menu: ['INSIGHT.DASHBOARD', 'INSIGHT.INSIGHT']
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