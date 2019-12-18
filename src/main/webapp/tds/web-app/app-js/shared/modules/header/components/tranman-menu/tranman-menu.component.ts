// Angular
import { Component, OnInit } from '@angular/core';
// Model
import {UserContextModel} from '../../../../../modules/auth/model/user-context.model';
import {Permission} from '../../../../model/permission.model';
// Service
import {UserContextService} from '../../../../../modules/auth/service/user-context.service';
import {PermissionService} from '../../../../services/permission.service';
import { TaskService } from '../../../../../modules/taskManager/service/task.service';

@Component({
	selector: 'tds-tranman-menu',
	templateUrl: 'tranman-menu.component.html',
})

export class TranmanMenuComponent implements OnInit {
	protected permission: Permission = Permission;
	public userContext: UserContextModel;
	taskTodoCount: number;

	constructor(
		private appSettingsService: UserContextService,
		private permissionService: PermissionService,
		private taskService: TaskService) {
		this.taskTodoCount = 0;
		this.getUserContext();
	}

	/**
	 * Set My Task count
	 */
	ngOnInit(): void {
		if (this.userContext.project) {
			this.taskService.retrieveUserToDoCount()
				.subscribe(result => {
					this.taskTodoCount = result.count;
				});
		}
	}

	protected getUserContext(): void {
		this.appSettingsService.getUserContext().subscribe( (userContext: UserContextModel) => {
			this.userContext = userContext;
		});
	}

	/**
	 * Validate if the User has the proper permission to access the Menu Item
	 * @param value
	 */
	protected hasPermission(value: string): boolean {
		return this.permissionService.hasPermission(value);
	}
}
