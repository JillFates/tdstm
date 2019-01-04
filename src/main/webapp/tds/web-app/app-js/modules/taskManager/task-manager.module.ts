/**
 * Created by Jorge Morayta on 3/15/2017.
 */

// Angular
import {NgModule} from '@angular/core';
// Services
import {TaskService} from './service/task.service';

@NgModule({
	providers: [TaskService],
})

export class TaskManagerModule {
}