import {Component, OnInit } from '@angular/core';

import {TaskCommonComponent} from '../common/task-common.component';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TaskDetailModel} from '../../model/task-detail.model';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TaskService} from '../../service/task.service';

declare var jQuery: any;

@Component({
	selector: `tds-task-edit`,
	templateUrl: '../tds/web-app/app-js/modules/taskManager/components/edit/task-edit.component.html',
	styles: []
})
export class TaskEditComponent extends TaskCommonComponent  implements OnInit {
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
}