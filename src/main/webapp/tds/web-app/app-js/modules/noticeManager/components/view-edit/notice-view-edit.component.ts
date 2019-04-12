// Angular
import {Component, ElementRef, ViewChild} from '@angular/core';
import {FormControl} from '@angular/forms';
// Component
import {RichTextEditorComponent} from '../../../../shared/modules/rich-text-editor/rich-text-editor.component';
import {ViewHtmlComponent} from '../view-html/view-html.component';
// Service
import {NoticeService} from '../../service/notice.service';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
// Kendo
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
// Model
import {NoticeModel, NoticeTypes, NOTICE_TYPE_PRE_LOGIN, NOTICE_TYPE_POST_LOGIN} from '../../model/notice.model';
import {Permission} from '../../../../shared/model/permission.model';
import {ModalType} from '../../../../shared/model/constants';

@Component({
	selector: 'tds-notice-view-edit',
	templateUrl: 'notice-view-edit.component.html'
})
export class NoticeViewEditComponent {
	@ViewChild('noticeViewEditContainerElement', {read: ElementRef}) noticeViewEditContainerElement: ElementRef;
	@ViewChild('htmlTextField') htmlText: RichTextEditorComponent;
	@ViewChild('typeIdField') typeId: DropDownListComponent;
	@ViewChild('noticeForm') noticeForm: FormControl;

	private dataSignature: string;
	public defaultItem: any = {
		typeId: null, name: 'Select a Type'
	};

	public typeDataSource = [...NoticeTypes];
	public model: NoticeModel;

	constructor(
		model: NoticeModel,
		public modalType: ModalType,
		public activeDialog: UIActiveDialogService,
		private dialogService: UIDialogService,
		private noticeService: NoticeService,
		private promptService: UIPromptService,
		private permissionService: PermissionService) {

		this.model = {...model};
		this.model.typeId = this.model.typeId || null;
		this.dataSignature = JSON.stringify(this.model);
	}

	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
				.then(confirm => {
					if (confirm) {
						this.activeDialog.dismiss();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.activeDialog.dismiss();
		}
	}

	protected deleteNotice(): void {
		this.noticeService.deleteNotice(this.model.id.toString())
			.subscribe(
				res => this.activeDialog.close(),
				error => this.activeDialog.dismiss(error));
	}

	/**
	 * Save the current status fo the Notice
	 */
	public saveNotice(): void {
		const payload = {...this.model};
		payload.typeId = payload.typeId === 1 ? NOTICE_TYPE_PRE_LOGIN : NOTICE_TYPE_POST_LOGIN;

		if (payload.id) {
			this.noticeService.editNotice(payload)
				.subscribe(
					notice => this.activeDialog.close(notice),
					error => this.activeDialog.dismiss(error));
		} else {
			this.noticeService.createNotice(payload)
				.subscribe(
					notice => this.activeDialog.close(notice),
					error => this.activeDialog.dismiss(error));

		}

	}

	/**
	 * Opens the view to pre-render the HTML
	 */
	public viewHTML(): void {
		this.dialogService.extra(ViewHtmlComponent,
			[{provide: NoticeModel, useValue: this.model}],
			false, false)
			.then((result) => {
				this.noticeViewEditContainerElement.nativeElement.focus();
			})
			.catch(error => console.log('View HTML Closed'));
	}

	public formValid(): boolean {
		return (this.noticeForm.valid && this.htmlText.valid() && !!this.model.typeId
			&& this.permissionService.hasPermission(Permission.NoticeEdit));
	}

	protected isDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.NoticeDelete);
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.model);
	}
}
