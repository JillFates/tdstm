// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';

// Components
import {EventDashboardComponent} from './components/dashboard/event-dashboard.component';

export class EventStates {
	public static readonly EVENT_DASHBOARD = {
		url: 'dashboard'
	};
}

export const UserRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: EventStates.EVENT_DASHBOARD.url},
	{
		path: EventStates.EVENT_DASHBOARD.url,
		data: {
			page: {
				title: 'EVENT.TITLE_DASHBOARD',
				instruction: '',
				menu: ['EVENT.DASHBOARD', 'EVENT.EVENT']
			},
			requiresAuth: true,
		},
		component: EventDashboardComponent,
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(UserRoute)]
})

export class EventRouteModule {
}