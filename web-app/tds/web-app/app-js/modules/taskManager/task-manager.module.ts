/**
 * Created by Jorge Morayta on 3/15/2017.
 */

// Angular
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
// Routing Logic
import { UIRouterModule } from 'ui-router-ng2';
import { TASK_MANAGER_STATES } from './task-manager-routing.states';
// Components
import { TaskListComponent } from './components/list/task-list.component';
import {TaskCreateComponent} from './components/create/task-create.component';
import { SharedModule } from '../../shared/shared.module';
// Import Kendo Modules
import { GridModule } from '@progress/kendo-angular-grid';
// Formly
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

@NgModule({
    imports: [
        CommonModule,
        SharedModule,
        GridModule,
        FormsModule,
        ReactiveFormsModule,
        UIRouterModule.forChild({ states: TASK_MANAGER_STATES }), // Same as { states: [state1, state2 ] }
    ],
    declarations: [
        TaskListComponent,
        TaskCreateComponent
    ],
    exports: [
        TaskListComponent,
        TaskCreateComponent
    ]
})

export class TaskManagerModule {
}