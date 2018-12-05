// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {APIActionResolveService} from './resolve/api-action-resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {APIActionListComponent} from './components/list/api-action-list.component';

export class APIActionStates {
	public static readonly API_ACTION_LIST = {
		url: 'list'
	};
}

export const APIActionRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: APIActionStates.API_ACTION_LIST.url},
	{
		path: APIActionStates.API_ACTION_LIST.url,
		data: {
			page: {
				title: 'API_ACTION.API_ACTIONS',
				instruction: '',
				menu: ['GLOBAL.PROJECT', 'API_ACTION.API_ACTIONS']
			},
			requiresAuth: true,
		},
		component: APIActionListComponent,
		resolve: {
			apiActions: APIActionResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(APIActionRoute)]
})

export class APIActionRouteModule {
}