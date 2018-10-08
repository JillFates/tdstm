/**
 * Created by Jorge Morayta on 3/15/2017.
 */

// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Components
import {SharedModule} from '../../shared/shared.module';
// Services
import {TaskService} from './service/task.service';
import {TaskCreateComponent} from './components/create/task-create.component';
import {TaskEditComponent} from './components/edit/task-edit.component';
import {TaskDetailComponent} from './components/detail/task-detail.component';

@NgModule({
	imports: [
		CommonModule,
		SharedModule,
		FormsModule,
	],
	declarations: [
		TaskCreateComponent,
		TaskEditComponent,
		TaskDetailComponent
	],
	providers: [TaskService],
	entryComponents: [
		TaskCreateComponent,
		TaskEditComponent,
		TaskDetailComponent
	]
})

export class TaskManagerModule {
}