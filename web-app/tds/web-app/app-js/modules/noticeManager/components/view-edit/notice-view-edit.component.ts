// Angular
import {Component, ViewChild} from '@angular/core';
import {FormControl} from '@angular/forms';
// Component
import {RichTextEditorComponent} from '../../../../shared/modules/rich-text-editor/rich-text-editor.component';
// Service
import {NoticeService} from '../../service/notice.service';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
// Kendo
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
// Model
import {NoticeModel} from '../../model/notice.model';
import {Permission} from '../../../../shared/model/permission.model';

@Component({
	selector: 'tds-notice-view-edit',
	templateUrl: '../tds/web-app/app-js/modules/noticeManager/components/view-edit/notice-view-edit.component.html'
})
export class NoticeViewEditComponent {
	@ViewChild('htmlTextField') htmlText: RichTextEditorComponent;
	@ViewChild('typeIdField') typeId: DropDownListComponent;
	@ViewChild('noticeForm') noticeForm: FormControl;

	model: NoticeModel;
	defaultItem: any = {
		typeId: null, name: 'Select a Type'
	};
	typeDataSource: Array<any> = [
		{typeId: 1, name: 'Prelogin'},
		{typeId: 2, name: 'Postlogin'}
	];

	constructor(
		model: NoticeModel,
		public action: Number,
		public activeDialog: UIActiveDialogService,
		private noticeService: NoticeService,
		private permissionService: PermissionService) {

		this.model = {...model};
	}

	cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

	deleteNotice(): void {
		this.noticeService.deleteNotice(this.model)
			.subscribe(
				res => this.activeDialog.close(),
				error => this.activeDialog.dismiss(error));
	}

	saveNotice(): void {
		if (this.model.id) {
			this.noticeService.editNotice(this.model)
				.subscribe(
					notice => this.activeDialog.close(notice),
					error => this.activeDialog.dismiss(error));
		} else {
			this.noticeService.createNotice(this.model)
				.subscribe(
					notice => this.activeDialog.close(notice),
					error => this.activeDialog.dismiss(error));

		}

	}

	formValid(): boolean {
		return this.noticeForm.valid && this.htmlText.valid() && !!this.model.typeId;
	}

	protected isCreateEditAvailable(): boolean {
		return this.action === 0 ?
			this.permissionService.hasPermission(Permission.NoticeEdit) :
			this.permissionService.hasPermission(Permission.NoticeEdit);
	}

	protected isDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.NoticeDelete);
	}
}
