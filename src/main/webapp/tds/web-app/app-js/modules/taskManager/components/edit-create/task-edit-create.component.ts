import {Component, ComponentFactoryResolver, OnInit, ViewChild} from '@angular/core';
import { TaskEditCreateCommonComponent } from '../common/task-edit-create-common.component';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import { PermissionService } from '../../../../shared/services/permission.service';
import { TaskDetailModel } from '../../model/task-detail.model';
import { PreferenceService } from '../../../../shared/services/preference.service';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { TaskService } from '../../service/task.service';
import {DialogService} from 'tds-component-library';

declare var jQuery: any;

@Component({
	selector: `tds-task-edit-create`,
	templateUrl: 'task-edit-create.component.html',
	styles: []
})
export class TaskEditCreateComponent extends TaskEditCreateCommonComponent implements OnInit {

	@ViewChild('dueDatePicker', { static: false}) dueDatePicker;
	constructor(
		componentFactoryResolver: ComponentFactoryResolver,
		taskDetailModel: TaskDetailModel,
		taskManagerService: TaskService,
		dialogService: DialogService,
		promptService: UIPromptService,
		userPreferenceService: PreferenceService,
		permissionService: PermissionService,
		translatePipe: TranslatePipe) {

		super(componentFactoryResolver, taskDetailModel, taskManagerService, dialogService, promptService, userPreferenceService, permissionService, translatePipe);

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
