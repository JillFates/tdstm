// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {EventsResolveService} from './resolve/events-resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
// Components
import {EventListComponent} from './components/list/event-list.component';
import {EventDashboardComponent} from './components/dashboard/event-dashboard.component';
import {UserRoute} from '../user/user-routing.states';

export class EventStates {
	public static readonly EVENT_LIST = {
		url: 'list'
	};
	public static readonly EVENT_DASHBOARD = {
		url: 'dashboard'
	};
}

export const EventRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: EventStates.EVENT_LIST.url},
	{
		path: EventStates.EVENT_LIST.url,
		data: {
			page: {
				title: 'EVENT.LIST',
				instruction: '',
				menu: ['PLANNING.PLANNING', 'EVENT.LIST']
			},
			requiresAuth: true,
		},
		component: EventListComponent,
		resolve: {
			events: EventsResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService]
	},
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
			requiresLicense: true
		},
		component: EventDashboardComponent,
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(EventRoute)]
})

export class EventRouteModule {
}