/**
 * Created by Jorge Morayta on 3/15/2017.
 */

import {Component, OnInit} from '@angular/core';

@Component({
    moduleId: module.id,
    selector: 'task-list',
    templateUrl: '../../tds/web-app/app-js/modules/taskManager/components/list/task-list.component.html'
})

export class TaskListComponent implements OnInit {

    private moduleName = '';
    private title = '';

    /**
     * @constructor
     */
    constructor() {
        this.moduleName = 'Task Manager List';
    }

    /**
     * Initiates the Notice Module
     */
    ngOnInit(): void {
        console.log('Init');
    }

}