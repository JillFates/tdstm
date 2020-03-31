// Angular
import {Component, ComponentFactoryResolver, OnInit, ViewChild} from '@angular/core';
// Component
import {TaskEditCreateCommonComponent} from '../common/task-edit-create-common.component';
// Service
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {TaskService} from '../../service/task.service';
import {DialogService} from 'tds-component-library';

@Component({
	selector: `tds-task-edit-create`,
	templateUrl: 'task-edit-create.component.html',
	styles: []
})
export class TaskEditCreateComponent extends TaskEditCreateCommonComponent implements OnInit {

	@ViewChild('dueDatePicker', {static: false}) dueDatePicker;

	constructor(
		componentFactoryResolver: ComponentFactoryResolver,
		taskManagerService: TaskService,
		dialogService: DialogService,
		userPreferenceService: PreferenceService,
		permissionService: PermissionService,
		translatePipe: TranslatePipe) {

		super(componentFactoryResolver, taskManagerService, dialogService, userPreferenceService, permissionService, translatePipe);

	}

	/**
	 * Forces kendo datepicker to open it's calendar.
	 * @param event: any
	 */
	public onOpenDueDatePicker(event): void {
		event.preventDefault();
		this.dueDatePicker.toggle();
	}

}
