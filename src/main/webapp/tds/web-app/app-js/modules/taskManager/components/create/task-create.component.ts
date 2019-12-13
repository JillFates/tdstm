import {Component, OnInit, ViewChild} from '@angular/core';

import {TaskEditCreateCommonComponent} from '../common/task-edit-create-common.component';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TaskDetailModel} from '../../model/task-detail.model';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TaskService} from '../../service/task.service';

declare var jQuery: any;

@Component({
	selector: `tds-task-create`,
	templateUrl: 'task-create.component.html',
	styles: []
})
export class TaskCreateComponent extends TaskEditCreateCommonComponent implements OnInit {

	@ViewChild('dueDatePicker') dueDatePicker;

	constructor(
		taskDetailModel: TaskDetailModel,
		taskManagerService: TaskService,
		dialogService: UIDialogService,
		promptService: UIPromptService,
		userPreferenceService: PreferenceService,
		permissionService: PermissionService,
		translatePipe: TranslatePipe) {

		super(taskDetailModel, taskManagerService, dialogService, promptService, userPreferenceService, permissionService, translatePipe);

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
