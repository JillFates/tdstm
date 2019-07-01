// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {EventsResolveService} from './resolve/events-resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {EventListComponent} from './components/list/event-list.component';

export class EventStates {
	public static readonly EVENT_LIST = {
		url: 'list'
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
				menu: ['EVENT.EVENTS', 'GLOBAL.LIST']
			},
			requiresAuth: true,
		},
		component: EventListComponent,
		resolve: {
			events: EventsResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(EventRoute)]
})

export class EventRouteModule {
}