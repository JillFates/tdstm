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
    public userFields: FormlyFieldConfig[];

    public user = {
        email: 'email@gmail.com',
        checked: false
    };

    /**
     * @constructor
     */
    constructor() {
        this.moduleName = 'Task Manager List';
    }


    prepareUserFields(): void {
        this.userFields = [{
            className: 'row',
            fieldGroup: [{
                className: 'col-xs-6',
                key: 'email',
                type: 'input',
                templateOptions: {
                    type: 'email',
                    label: 'Email address',
                    placeholder: 'Enter email'
                },
                validators: {
                    validation: Validators.compose([Validators.required])
                }
            }, {
                className: 'col-xs-6',
                key: 'password',
                type: 'input',
                templateOptions: {
                    type: 'password',
                    label: 'Password',
                    placeholder: 'Password',
                    pattern: ''
                },
                validators: {
                    validation: Validators.compose([Validators.required])
                }
            }]
        }];
    }

    /**
     * Initiates the Notice Module
     */
    ngOnInit(): void {
        console.log('Init');
        this.prepareUserFields();
    }

}