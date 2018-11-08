// /**
//  * Created by Jorge Morayta on 3/15/2017.
//  */
//
// import {Component, OnInit} from '@angular/core';
// import {StateService} from '@uirouter/angular';
// import {TaskStates} from '../../task-manager-routing.states';
//
// @Component({
// 	selector: 'task-list',
// 	templateUrl: '../tds/web-app/app-js/modules/taskManager/components/list/task-list.component.html'
// })
//
// export class TaskListComponent implements OnInit {
//
// 	private moduleName = '';
// 	private taskList = [];
//
// 	/**
// 	 * @constructor
// 	 * @param {StateService} stateService
// 	 */
// 	constructor(private stateService: StateService) {
// 		this.moduleName = 'Task Manager List';
//
// 		this.taskList.push({title: 'example 1'});
// 		this.taskList.push({title: 'example 2'});
// 	}
//
// 	/**
// 	 * Create a new Task
// 	 * @listens onEditCreateNotice
// 	 */
// 	onCreateTask(): void {
// 		this.stateService.go(TaskStates.CREATE.name);
// 	}
//
// 	/**
// 	 * Initiates the Notice Module
// 	 */
// 	ngOnInit(): void {
// 		console.log('Init');
// 	}
// }