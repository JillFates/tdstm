/**
 * Created by Jorge Morayta on 3/15/2017.
 */

// Angular
import {NgModule} from '@angular/core';
// import {CommonModule} from '@angular/common';
// import {FormsModule, ReactiveFormsModule} from '@angular/forms';
// Routing Logic
import {UIRouterModule} from '@uirouter/angular';
import {TASK_MANAGER_STATES} from './task-manager-routing.states';
// Components
// import {TaskListComponent} from './components/list/task-list.component';
// import {TaskCreateComponent} from './components/create/task-create.component';
// import {SharedModule} from '../../shared/shared.module';
// Services
import {TaskService} from './service/task.service';
// Import Kendo Modules
// import {GridModule} from '@progress/kendo-angular-grid';
// import {DynamicFormsCoreModule} from '@ng-dynamic-forms/core';
// import {DynamicFormsKendoUIModule} from '@ng-dynamic-forms/ui-kendo';

@NgModule({
	/*imports: [
		CommonModule,
		SharedModule,
		GridModule,
		FormsModule,
		ReactiveFormsModule,
		DynamicFormsCoreModule.forRoot(),
		DynamicFormsKendoUIModule,
		UIRouterModule.forChild({states: TASK_MANAGER_STATES}), // Same as { states: [state1, state2 ] }
	],*/
	providers: [TaskService],
	/*declarations: [
		TaskListComponent,
		TaskCreateComponent
	],
	exports: [
		TaskListComponent,
		TaskCreateComponent
	]*/
})

export class TaskManagerModule {
}