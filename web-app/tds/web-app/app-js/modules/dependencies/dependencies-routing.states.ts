// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {DependenciesListComponent} from './components/list/dependencies-list.component';

export class DependenciesStates {
	public static readonly DEPENDENCIES_LIST = {
		url: 'list'
	};
}

export const DependenciesRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: DependenciesStates.DEPENDENCIES_LIST.url},
	{
		path: DependenciesStates.DEPENDENCIES_LIST.url,
		data: {
			page: {
				title: 'DEPENDENCIES.LIST_TITLE',
				instruction: '',
				menu: []
			},
			requiresAuth: true,
		},
		component: DependenciesListComponent,
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(DependenciesRoute)]
})
export class DependenciesRouteModule {
}
