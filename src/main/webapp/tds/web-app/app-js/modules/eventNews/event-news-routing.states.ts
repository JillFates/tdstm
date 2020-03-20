// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
// Components
import {EventNewsListComponent} from './components/list/event-news-list.component';
import {EventsResolveService} from '../event/resolve/events-resolve.service';

/**
 * Top menu parent section class.
 * @type {string}
 */
const TOP_MENU_PARENT_PROJECT = 'menu-parent-project';

export class EventNewsStates {
	public static readonly EVENT_LIST = {
		url: 'list'
	};
}

export const EventNewsRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: EventNewsStates.EVENT_LIST.url},
	{
		path: EventNewsStates.EVENT_LIST.url,
		data: {
			page: {
				title: 'EVENT.NEWS',
				instruction: '',
				menu: ['PLANNING.PLANNING', 'EVENT.NEWS'],
				topMenu: { parent: TOP_MENU_PARENT_PROJECT, child: 'menu-parent-planning-event-news', subMenu: true}
			},
			requiresAuth: true,
		},
		component: EventNewsListComponent,
		resolve: {
			events: EventsResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(EventNewsRoute)]
})

export class EventNewsRouteModule {
}
