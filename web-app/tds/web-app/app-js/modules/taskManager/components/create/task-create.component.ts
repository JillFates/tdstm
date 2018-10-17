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
	selector: `tds-task-create`,
	templateUrl: '../tds/web-app/app-js/modules/taskManager/components/create/task-create.component.html',
	styles: []
})
export class TaskCreateComponent extends TaskCommonComponent  implements OnInit {
	constructor(
		public taskDetailModel: TaskDetailModel,
		public taskManagerService: TaskService,
		public dialogService: UIDialogService,
		public promptService: UIPromptService,
		public userPreferenceService: PreferenceService,
		public permissionService: PermissionService,
		public translatePipe: TranslatePipe) {

		super(taskDetailModel, taskManagerService, dialogService, promptService, userPreferenceService, permissionService, translatePipe);

	}

}