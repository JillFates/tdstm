/**
 * Created by Jorge Morayta on 3/15/2017.
 */

import {Component, OnInit} from '@angular/core';
import { StateService } from 'ui-router-ng2';
import { TaskStates } from '../../task-manager-routing.states';

@Component({
    moduleId: module.id,
    selector: 'task-list',
    templateUrl: '../../tds/web-app/app-js/modules/taskManager/components/list/task-list.component.html'
})

export class TaskListComponent implements OnInit {

    private moduleName = '';

    /**
     * @constructor
     * @param {StateService} stateService
     */
    constructor(private stateService: StateService) {
        this.moduleName = 'Task Manager List';
    }

    /**
     * Create a new Task
     * @listens onEditCreateNotice
     */
    onCreateTask(): void {
        this.stateService.go(TaskStates.CREATE);
    }

    /**
     * Initiates the Notice Module
     */
    ngOnInit(): void {
        console.log('Init');
    }

}