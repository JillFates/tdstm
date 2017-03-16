/**
 * Created by Jorge Morayta on 3/16/2017.
 */

import {Component, OnInit} from '@angular/core';
import {Validators, FormGroup} from '@angular/forms';
import {FormlyFieldConfig} from 'ng-formly';

@Component({
    moduleId: module.id,
    selector: 'task-create',
    templateUrl: '../../tds/web-app/app-js/modules/taskManager/components/create/task-create.component.html'
})

export class TaskCreateComponent implements OnInit {

    private moduleName = '';

    public form: FormGroup = new FormGroup({});

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