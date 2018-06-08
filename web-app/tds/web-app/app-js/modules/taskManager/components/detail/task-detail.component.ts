import {Component} from '@angular/core';
import {ModalType} from '../../../../shared/model/constants';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: `task-detail`,
	templateUrl: '../tds/web-app/app-js/modules/taskManager/components/detail/task-detail.component.html',
	styles: []
})
export class TaskDetailComponent extends UIExtraDialog {

	public modalType = ModalType;
	public dateFormatTime: string;

	constructor() {
		super('#task-detail-component');
	}
}