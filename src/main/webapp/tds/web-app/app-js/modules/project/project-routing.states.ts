// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
// Components
import {ProjectListComponent} from './components/list/project-list.component';
import {ProjectResolveService} from './resolve/project-resolve.service';

/**
 * Asset Explorer Route States
 * @class
 * @classdesc To use externally to reference possible state of the Asset Explorer Module
 */
export class ProjectStates {
	public static readonly PROJECT_LIST = {
		url: 'list'
	}
}

export const ProjectRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: ProjectStates.PROJECT_LIST.url},
	{
		path: ProjectStates.PROJECT_LIST.url,
		data: {
			page: {
				title: 'PROJECTS.LIST',
				instruction: '',
				menu: ['GLOBAL.PROJECTS', 'GLOBAL.LIST'],
			},
			requiresAuth: true
		},
		component: ProjectListComponent,
		resolve: {
			projects: ProjectResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService],
		runGuardsAndResolvers: 'always'
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(ProjectRoute)]
})

export class ProjectRouteModule {
}
