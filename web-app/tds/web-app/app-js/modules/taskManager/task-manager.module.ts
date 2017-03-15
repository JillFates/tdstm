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
import { SharedModule } from '../../shared/shared.module';
// Import Kendo Modules
import { GridModule } from '@progress/kendo-angular-grid';

@NgModule({
    imports: [
        CommonModule,
        SharedModule,
        GridModule,
        UIRouterModule.forChild({ states: TASK_MANAGER_STATES }), // Same as { states: [state1, state2 ] }
    ],
    declarations: [TaskListComponent],
    exports: [TaskListComponent]
})

export class TaskManagerModule {
}