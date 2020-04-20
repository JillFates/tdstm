// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
import {TaskListComponent} from './components/list/task-list.component';
import {NeighborhoodComponent} from './components/neighborhood/neighborhood.component';
// Components

/**
 * Top menu parent section class for all Reports module.
 * @type {string}
 */
const TOP_MENU_PARENT_SECTION = 'menu-parent-tasks';

export class TaskManagerRoutingStates {
	public static readonly TASK_MANAGER_LIST = {
		url: 'list'
	};
	public static readonly TASK_NEIGHBORHOOD = {
		url: 'task-graph'
	};
}

export const TaskManagerRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: TaskManagerRoutingStates.TASK_MANAGER_LIST.url},
	{
		path: TaskManagerRoutingStates.TASK_MANAGER_LIST.url,
		data: {
			page: {
				title: 'Task Manager',
				instruction: '',
				menu: ['Task', 'Task Manager'],
				topMenu: {parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-tasks-task-manager', subMenu: true}
			},
			requiresAuth: true,
			requiresLicense: true
		},
		component: TaskListComponent,
		canActivate: [
			AuthGuardService,
			ModuleResolveService
		],
		resolve: {},
		runGuardsAndResolvers: 'always'
	},
	{
		path: TaskManagerRoutingStates.TASK_NEIGHBORHOOD.url,
		data: {
			page: {
				title: 'GoJS Task Graph',
				instruction: '',
				menu: ['Task', 'GoJS Task Graph'],
				topMenu: {parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-tasks-gojs-task-graph', subMenu: true}
			},
			requiresAuth: true,
		},
		component: NeighborhoodComponent,
		canActivate: [
			AuthGuardService,
			ModuleResolveService
		],
		resolve: {},
		runGuardsAndResolvers: 'always'
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(TaskManagerRoute)]
})
export class TaskManagerRouteModule {
}
