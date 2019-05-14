// Angular
import {Component, ViewChild, OnInit} from '@angular/core';
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
// import {NoticeModel, NoticeTypes, NoticeType} from '../../model/notice.model';
import {NoticeModel, NoticeTypes} from '../../model/notice.model';
import {Permission} from '../../../../shared/model/permission.model';

@Component({
	selector: 'tds-notice-view-edit',
	templateUrl: '../tds/web-app/app-js/modules/noticeManager/components/view-edit/notice-view-edit.component.html'
})
export class NoticeViewEditComponent {
	@ViewChild('htmlTextField') htmlText: RichTextEditorComponent;
	@ViewChild('typeIdField') typeId: DropDownListComponent;
	@ViewChild('noticeForm') noticeForm: FormControl;

	private dataSignature: string;
	protected model: NoticeModel;
	protected defaultItem: any = {
		typeId: null, name: 'Select a Type'
	};
/*
	protected typeDataSource: Array<any> = [].concat(NoticeTypes);
	protected EnumNoticeType = NoticeType;
	protected noticeType: any;
	noticeIsLocked: boolean;
*/
	typeDataSource = [...NoticeTypes];
	constructor(
		model: NoticeModel,
		public action: Number,
		public activeDialog: UIActiveDialogService,
		private dialogService: UIDialogService,
		private noticeService: NoticeService,
		private promptService: UIPromptService,
		private permissionService: PermissionService) {
		this.model = {...model};
		this.model.typeId = this.model.typeId;
		this.dataSignature = JSON.stringify(this.model);
	}

	/*
	ngOnInit() {
		this.noticeIsLocked = this.model.locked;

		this.model.typeId = this.model.typeId ?  parseInt(this.model.typeId, 10) : null;
		this.model.active = this.mapActiveField(this.model.active);

		if (this.model.needAcknowledgement) {
			this.model.typeId = NoticeType.Mandatory;
		}
		this.noticeType = {typeId: this.model.typeId};
		}
	*/

	protected cancelCloseDialog(): void {
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
		this.promptService.open('Confirmation Required', 'You are about to delete the selected notice. Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.noticeService.deleteNotice(this.model.id.toString())
						.subscribe(
							res => this.activeDialog.close(),
							error => this.activeDialog.dismiss(error));
				}
			});
	}

	/**
	 * Save the current status fo the Notice
	 */
	protected saveNotice(): void {
		/*
		this.model.typeId = (this.noticeType && this.noticeType.typeId);

		const model = {...this.model};

		if (model.typeId === NoticeType.Mandatory) {
			model.typeId = NoticeType.PostLogin;
			model.needAcknowledgement = true;
		}

		if (model.id) {
			this.noticeService.editNotice(model)
				.subscribe(
					notice => this.activeDialog.close(notice),
					error => this.activeDialog.dismiss(error));
		} else {
			this.noticeService.createNotice(model)
				.subscribe(
					notice => this.activeDialog.close(notice),
					error => this.activeDialog.dismiss(error));
		}
		*/
	}

	/**
	 * Opens the view to pre-render the HTML
	 */
	protected viewHTML(): void {
		this.dialogService.extra(ViewHtmlComponent,
			[{provide: NoticeModel, useValue: this.model}],
			false, false)
			.then((result) => {
				//
			})
			.catch(error => console.log('View HTML Closed'));
	}

	protected formValid(): boolean {
/*
		const noticeType = this.noticeType && this.noticeType.typeId;
		const isValid =  this.model.title &&
				(this.htmlText.value && this.htmlText.value.trim())  &&
				this.htmlText.valid() && (noticeType || noticeType === 0);

		const returnValue =  (noticeType === NoticeType.Mandatory) ? (isValid && (this.model.postMessageText && this.model.postMessageText.trim() !== '')) : isValid;

		return returnValue;
*/
		console.log('TypeId:', !!this.model.typeId);
		console.log('Html', this.htmlText.valid());
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

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.model);
	}

	private mapActiveField(active: any): any {
		if (active === 'Yes') {
			return true;
		}

		if (active === 'No') {
			return false;
		}

		return active;
	}
	/**
	 * Grab the current html value emitted by rich text editor
	 */
	onValueChange(value: string) {
		this.model.htmlText = value;
	}

	/**
	 * Grab the current raw value emitted by rich text editor
	 */
	onRawValueChange(value: string) {
		this.model.rawText = value;
	}
}
