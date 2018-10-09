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

@NgModule({
	imports: [
		CommonModule,
		SharedModule,
		FormsModule,
	],
	declarations: [
	],
	providers: [TaskService],
	entryComponents: []
})

export class TaskManagerModule {
}