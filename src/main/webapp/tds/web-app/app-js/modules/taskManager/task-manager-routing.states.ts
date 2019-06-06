// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
import {TaskListComponent} from './components/list/task-list.component';
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
		},
		component: TaskListComponent,
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
