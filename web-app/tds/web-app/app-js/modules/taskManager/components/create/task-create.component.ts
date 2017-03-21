/**
 * Created by Jorge Morayta on 3/16/2017.
 */

import {Component, OnInit} from '@angular/core';
import {FormlyFieldConfig} from 'ng-formly';
import {FormGroup} from '@angular/forms';
import {StateService} from 'ui-router-ng2';
import {TaskStates} from '../../task-manager-routing.states';

@Component({
    moduleId: module.id,
    selector: 'task-create',
    templateUrl: '../../tds/web-app/app-js/modules/taskManager/components/create/task-create.component.html'
})

export class TaskCreateComponent implements OnInit {

    private moduleName = '';

    public form: FormGroup = new FormGroup({});
    public userFields: FormlyFieldConfig[];

    public user = {
        email: 'email@gmail.com',
        checked: false,
        taskName: ''
    };

    /**
     * @constructor
     */
    constructor(private stateService: StateService) {
        this.moduleName = 'Task Manager List';
    }

    prepareUserFields(): void {
        this.userFields = [
        {
            id: 'taskName',
            key: 'taskName',
            type: 'formlyInputHorizontalWrapper',
            templateOptions: {
                label: 'Task',
                placeholder: 'Task Name',
                validate: true,
                required: true,
                options: [{maxWidth: 500}]
            },
            validation: {
                show: true
            }
        },
        {
            fieldGroup: [
                {
                    id: 'personTeam',
                    key: 'personTeam',
                    type: 'formlySelectHorizontalWrapper',
                    templateOptions: {
                        label: 'Person/Team',
                        options: [{maxWidth: 300}]
                    }
                }
            ]
        }];
    }
    createTask(): void {
        console.log(this.user);
    }

    /**
     * On Cancel new Creation
     * @listens onEditCreateNotice
     */
    onCancelCreateTask(): void {
        this.stateService.go(TaskStates.LIST.name);
    }

    /**
     * Initiates the Notice Module
     */
    ngOnInit(): void {
        console.log('Init');
        this.prepareUserFields();
    }
}