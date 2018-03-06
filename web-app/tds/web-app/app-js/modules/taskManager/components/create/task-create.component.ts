/**
 * Created by Jorge Morayta on 3/16/2017.
 */

import {Component, OnInit} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {StateService} from '@uirouter/angular';
import {TaskStates} from '../../task-manager-routing.states';

@Component({
	selector: 'task-create',
	templateUrl: '../tds/web-app/app-js/modules/taskManager/components/create/task-create.component.html'
})

export class TaskCreateComponent implements OnInit {

	private moduleName = '';

	public form: FormGroup = new FormGroup({});

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
	}
}