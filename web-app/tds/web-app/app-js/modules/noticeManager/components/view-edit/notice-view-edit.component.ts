// Angular
import {Component, ViewChild, OnInit, AfterViewInit } from '@angular/core';
import {FormControl} from '@angular/forms';
// Component
import {RichTextEditorComponent} from '../../../../shared/modules/rich-text-editor/rich-text-editor.component';
import {ViewHtmlComponent} from '../view-html/view-html.component';
// Service
import {NoticeService} from '../../service/notice.service';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {StringUtils} from '../../../../shared/utils/string.utils';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
// Kendo
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
// Model
// import {NoticeModel, NoticeTypes, NoticeType} from '../../model/notice.model';
import {NoticeModel, NoticeTypes, NOTICE_TYPE_MANDATORY, NOTICE_TYPE_POST_LOGIN} from '../../model/notice.model';
import {Permission} from '../../../../shared/model/permission.model';
import {ActionType} from '../../../../shared/model/action-type.enum';
@Component({
	selector: 'tds-notice-view-edit',
	templateUrl: '../tds/web-app/app-js/modules/noticeManager/components/view-edit/notice-view-edit.component.html'
})
export class NoticeViewEditComponent implements OnInit, AfterViewInit {
	@ViewChild('htmlTextField') htmlText: RichTextEditorComponent;
	@ViewChild('typeIdField') typeId: DropDownListComponent;

	public EnumActionType = ActionType;
	private dataSignature: string;
	protected model: NoticeModel;
	protected defaultItem: any = {
		typeId: null, name: 'Select a Type'
	};
	MANDATORY = NOTICE_TYPE_MANDATORY;
	noticeType: any;
	noticeIsLocked: boolean;
	typeDataSource = [...NoticeTypes];
	minDate = null;
	maxDate = null;
	typeName = '';
	userDateFormat = '';
	constructor(
		private translate: TranslatePipe,
		private originalModel: NoticeModel,
		public action: Number,
		public activeDialog: UIActiveDialogService,
		private dialogService: UIDialogService,
		private noticeService: NoticeService,
		private promptService: UIPromptService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService) {
	}

	ngOnInit() {
		this.model = {...this.originalModel};
		this.model.typeId = this.model.typeId;
		this.noticeIsLocked = this.model.locked;
		this.model.active = StringUtils.stringToBoolean(this.model.active);
		const currentType = this.typeDataSource
						.find((typeData) => typeData.typeId === this.model.typeId);
		if (currentType) {
			this.typeName = currentType.name;
		}

		if (this.model.needAcknowledgement) {
			this.model.typeId = NOTICE_TYPE_MANDATORY;
		}
		this.noticeType = {typeId: this.model.typeId};

		if (this.model.expirationDate) {
			this.setMaxDate(this.model.expirationDate);
		}

		if (this.model.activationDate) {
			this.setMinDate(this.model.activationDate);
		}

		this.userDateFormat = this.preferenceService.getUserDateFormatForMomentJS();

		this.dataSignature = JSON.stringify(this.model);
	}

	ngAfterViewInit() {
		setTimeout(() => this.htmlText.editor.editorContainer.title = this.translate.transform('NOTICE.TOOLTIP_MESSAGE'));
	}

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
							error => console.error(error));
				}
			});
	}

	/**
	 * Get and clean the payload to be sent to the server to create or edit a notice
	*/
	private getPayloadFromModel(): any {
		this.model.typeId = (this.noticeType && this.noticeType.typeId);

		const payload = {...this.model};

		if (payload.typeId === this.MANDATORY) {
			payload.typeId = NOTICE_TYPE_POST_LOGIN;
			payload.needAcknowledgement = true;
		} else {
			payload.needAcknowledgement = false;
			delete payload['acknowledgeLabel'];
		}
		payload.locked = payload.locked || false;
		// remove esc sequences
		payload.htmlText = payload.htmlText.replace(new RegExp('\\n', 'g'), '');

		// use zero for empty sequences
		payload.sequence = payload.sequence === null || typeof payload.sequence === 'undefined' ? 0 : payload.sequence;
		// don't send '' for empty dates, instead use null
		payload.activationDate = payload.activationDate === '' ? null : payload.activationDate;
		payload.expirationDate = payload.expirationDate === '' ? null : payload.expirationDate;

		return payload;
	}

	/**
	 * Save the current status fo the Notice
	 */
	protected saveNotice(): void {
		const payload = this.getPayloadFromModel();

		if (payload.id) {
			this.noticeService.editNotice(payload)
				.subscribe(
					notice => this.activeDialog.close(notice),
					error => console.error(error));
		} else {
			this.noticeService.createNotice(payload)
				.subscribe(
					notice => this.activeDialog.close(notice),
					error => console.error(error));
		}
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

	/**
	 * Determines if all the field forms comply with the validation rules
	*/
	protected formValid(): boolean {
		const noticeType = this.noticeType && this.noticeType.typeId;
		const isValid =  this.model && this.model.title &&
				this.isValidHtmlText() && (noticeType || noticeType === 0);

		const returnValue =  (noticeType === this.MANDATORY) ? (isValid && (this.model.acknowledgeLabel && this.model.acknowledgeLabel.trim() !== '')) : isValid;

		return returnValue;
	}

	private isValidHtmlText(): boolean {
		return this.htmlText &&
				this.htmlText.value &&
				this.htmlText.value.trim()  &&
				this.htmlText.valid();

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

	/**
	 * Change to edit mode
	 */
	protected editNotice(): void {
		this.action = ActionType.Edit;
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

	/**
	 * Set the maximum value for the date range
	 */
	setMaxDate(value: any) {
		this.maxDate = value;
		if (this.model.activationDate && value <  this.convertToDate(this.model.activationDate)) {
			this.model.expirationDate = '';
		}
	}

	/**
	 * Set the minimu value for the date range
	*/
	setMinDate(value: any) {
		this.minDate = value;
		if (this.model.expirationDate && value >  this.convertToDate(this.model.expirationDate)) {
			this.model.activationDate = '';
		}
	}

	/**
	 * Could receive a string or date, based in the type make sure returns a date  object
	 * @param {any} value:  String or Date to cast
	 * @returns {date}
	 */
	private convertToDate(value: any): any {
		return (value && value.toDateString) ? value : new Date(DateUtils.getDateFromGMT(value));
	}

	/**
	 * Based on modalType action returns the corresponding title
	 * @param {ActionType} modalType
	 * @returns {string}
	 */
	getModalTitle(modalType: ActionType): string {
		if (modalType === ActionType.Edit) {
			return this.translate.transform('NOTICE.EDIT_NOTICE');
		}

		if (modalType === ActionType.Create) {
			return this.translate.transform('NOTICE.CREATE_NOTICE');
		}

		if (modalType === ActionType.View) {
			return this.translate.transform('NOTICE.SHOW_NOTICE');
		}

		return '';
	}
}
