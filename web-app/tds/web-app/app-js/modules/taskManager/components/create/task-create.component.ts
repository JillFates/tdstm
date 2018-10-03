// /**
//  * Created by Jorge Morayta on 3/16/2017.
//  */
//
// import {Component, OnInit, ViewEncapsulation} from '@angular/core';
// import {FormGroup} from '@angular/forms';
// // import {DynamicFormService, DynamicFormControlModel, DynamicFormLayout} from '@ng-dynamic-forms/core';
// import {StateService} from '@uirouter/angular';
// import {TaskStates} from '../../task-manager-routing.states';
// // import {TaskFormModel} from '../../model/task-form.model';
// import {TaskFormLayoutModel} from '../../model/task-form-layout.model';
//
// @Component({
// 	selector: 'task-create',
// 	templateUrl: '../tds/web-app/app-js/modules/taskManager/components/create/task-create.component.html'
// })
//
// export class TaskCreateComponent implements OnInit {
//
// 	private moduleName = '';
//
// 	// public formModel: /*DynamicFormControlModel[] =*/ any = TaskFormModel;
// 	public formGroup: FormGroup;
// 	formLayout: /* DynamicFormLayout =*/ any = TaskFormLayoutModel;
//
// 	public user = {
// 		email: 'email@gmail.com',
// 		checked: false,
// 		taskName: ''
// 	};
//
// 	/**
// 	 * @constructor
// 	 */
// 	constructor(private stateService: StateService, private formService: any /* DynamicFormService */) {
// 		this.moduleName = 'Task Manager List';
// 	}
//
// 	createTask(): void {
// 		console.log(this.user);
// 	}
//
// 	/**
// 	 * On Cancel new Creation
// 	 * @listens onEditCreateNotice
// 	 */
// 	onCancelCreateTask(): void {
// 		this.stateService.go(TaskStates.LIST.name);
// 	}
//
// 	/**
// 	 * Initiates the Notice Module
// 	 */
// 	ngOnInit(): void {
// 		console.log('Init');
// 		// this.formGroup = this.formService.createFormGroup(this.formModel);
// 	}
// }