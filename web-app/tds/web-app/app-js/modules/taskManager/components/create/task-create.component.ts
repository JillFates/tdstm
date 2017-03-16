/**
 * Created by Jorge Morayta on 3/16/2017.
 */

import {Component, OnInit} from '@angular/core';

@Component({
    moduleId: module.id,
    selector: 'task-list',
    templateUrl: '../../tds/web-app/app-js/modules/taskManager/components/create/task-create.component.html'
})

export class TaskCreateComponent implements OnInit {

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