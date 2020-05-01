// Angular
import {AfterViewInit, Component, ComponentFactoryResolver, Input, OnInit, ViewChild, ElementRef} from '@angular/core';
// Component
import {ViewHtmlComponent} from '../view-html/view-html.component';
// Service
import {NoticeService} from '../../service/notice.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {StringUtils} from '../../../../shared/utils/string.utils';
import {PreferenceService} from '../../../../shared/services/preference.service';
// Kendo
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
// Model
import {NOTICE_TYPE_MANDATORY, NOTICE_TYPE_POST_LOGIN, NoticeModel, NoticeTypes} from '../../model/notice.model';
import {Permission} from '../../../../shared/model/permission.model';
import {ActionType} from '../../../../shared/model/action-type.enum';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService, ModalSize} from 'tds-component-library';
import {ESCAPE_KEYCODE} from '../../../../shared/model/constants';
// Other
import * as R from 'ramda';
import {NgForm} from '@angular/forms';

declare var jQuery: any;

@Component({
	selector: 'tds-notice-view-edit',
	templateUrl: 'notice-view-edit.component.html',
})
export class NoticeViewEditComponent extends Dialog implements OnInit, AfterViewInit {
	@Input() data: any;
	@ViewChild('form', {read: NgForm, static: true}) form: NgForm;
	@ViewChild('typeIdField', {static: false}) typeId: DropDownListComponent;
	@ViewChild('noticeCreateTitle', {static: false}) noticeCreateTitle: ElementRef;
	public EnumActionType = ActionType;
	private dataSignature: string;
	protected model: NoticeModel;
	protected defaultItem: any = {
		typeId: null,
		name: 'Select a Type',
	};

	MANDATORY = NOTICE_TYPE_MANDATORY;
	noticeType: any;
	noticeIsLocked: boolean;
	typeDataSource = [...NoticeTypes];
	minDate = null;
	maxDate = null;
	typeName = '';
	userDateFormat = '';
	public action: ActionType = ActionType.View;

	public htmlText = '';

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private translatePipe: TranslatePipe,
		private noticeService: NoticeService,
		private dialogService: DialogService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService
	) {
		super();
	}

	ngOnInit() {
		this.model = R.clone(this.data.noticeModel);
		this.htmlText = R.clone(this.model.htmlText);
		this.action = this.data.actionType;

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			show: () => this.action === ActionType.Edit || this.action === ActionType.View,
			active: () => this.action === ActionType.Edit,
			type: DialogButtonType.ACTION,
			action: this.editNotice.bind(this)
		});

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => this.action === ActionType.Edit || this.action === ActionType.Create,
			disabled: () => !this.formValid() || !this.isCreateEditAvailable() || !this.isDirty(),
			type: DialogButtonType.ACTION,
			action: this.saveNotice.bind(this)
		});

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			show: () => this.action !== ActionType.Create,
			type: DialogButtonType.ACTION,
			action: this.deleteNotice.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			show: () => this.action === ActionType.View || this.action === ActionType.Create,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			show: () => this.action === ActionType.Edit,
			type: DialogButtonType.ACTION,
			action: this.cancelEditDialog.bind(this)
		});

		this.buttons.push({
			name: 'viewHTML',
			icon: 'code',
			text: 'View HTML',
			show: () => true,
			disabled: () => !this.model.htmlText || this.model.htmlText.length <= 0,
			type: DialogButtonType.CONTEXT,
			action: this.viewHTML.bind(this)
		});

		if (this.model.needAcknowledgement) {
			this.model.typeId = NOTICE_TYPE_MANDATORY;
		}
		this.noticeIsLocked = this.model.locked;
		this.model.active = StringUtils.stringToBoolean(this.model.active);
		const currentType = this.typeDataSource.find(
			typeData => typeData.typeId === this.model.typeId
		);
		if (currentType) {
			this.typeName = currentType.name;
		}

		this.noticeType = {typeId: (this.model && this.model.typeId) || null};

		if (this.model.expirationDate) {
			this.setMaxDate(this.model.expirationDate);
		}

		if (this.model.activationDate) {
			this.setMinDate(this.model.activationDate);
		}

		this.userDateFormat = this.preferenceService.getUserDateFormatForMomentJS();

		this.dataSignature = JSON.stringify(this.model);

		setTimeout(() => {
			this.setTitle(this.getModalTitle(this.action));
			super.onSetUpFocus(this.noticeCreateTitle);
		});
	}

	/**
	 * Editor is outside the Browser Main Frame
	 * This code helps to catch Escape Button coming from the Editor iFrame
	 */
	ngAfterViewInit(): void {
		setTimeout(() => {
			const editorFrame = jQuery('.k-iframe');
			if (editorFrame) {
				jQuery(jQuery(editorFrame).get(0).contentWindow.document).on('keyup', (event: any) => {
					if (event.keyCode === ESCAPE_KEYCODE) {
						this.cancelHTMLInputDialog();
					}
				});
			}
		});
	}

	public cancelEditDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM && !this.data.openFromList) {
					// Put back original model
					this.model = JSON.parse(this.dataSignature);
					this.dataSignature = JSON.stringify(this.model);
					this.action = ActionType.View;
					this.setTitle(this.getModalTitle(this.action));
				} else if (data.confirm === DialogConfirmAction.CONFIRM && this.data.openFromList) {
					this.onCancelClose();
				}
			});
		} else {
			if (!this.data.openFromList) {
				this.action = ActionType.View;
				this.setTitle(this.getModalTitle(this.action));
			} else {
				this.onCancelClose();
			}
		}
	}

	/**
	 * Required by the HTML Handler
	 */
	public cancelHTMLInputDialog(): void {
		if (this.action === ActionType.Create || this.action === ActionType.View) {
			this.cancelCloseDialog();
		} else if (this.action === ActionType.Edit) {
			this.cancelEditDialog();
		}
	}

	protected cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					this.onCancelClose();
				}
			});
		} else {
			this.onCancelClose();
		}
	}

	protected deleteNotice(): void {
		this.dialogService.confirm(
			this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
			'You are about to delete the selected notice. Do you want to proceed?'
		).subscribe((data: any) => {
			if (data.confirm === DialogConfirmAction.CONFIRM) {
				this.noticeService
					.deleteNotice(this.model.id.toString())
					.subscribe(
						res => this.onCancelClose()
					);
			}
		});
	}

	/**
	 * Get and clean the payload to be sent to the server to create or edit a notice
	 */
	private getPayloadFromModel(): any {
		this.model.typeId = this.noticeType && this.noticeType.typeId;

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
		payload.rawText = payload.htmlText.replace(/<[^>]*>/g, '');

		// use zero for empty sequences
		payload.sequence =
			payload.sequence === null || typeof payload.sequence === 'undefined'
				? 0
				: payload.sequence;
		// don't send '' for empty dates, instead use null
		payload.activationDate =
			payload.activationDate === '' ? null : payload.activationDate;
		payload.expirationDate =
			payload.expirationDate === '' ? null : payload.expirationDate;

		return payload;
	}

	/**
	 * Save the current status fo the Notice
	 */
	protected saveNotice(): void {
		const payload = this.getPayloadFromModel();

		if (payload.id) {
			this.noticeService.editNotice(payload).subscribe(
				notice => this.onAcceptSuccess(notice),
				error => console.error(error)
			);
		} else {
			this.noticeService.createNotice(payload).subscribe(
				notice => this.onAcceptSuccess(notice),
				error => console.error(error)
			);
		}
	}

	/**
	 * Opens the view to pre-render the HTML
	 */
	private async viewHTML(): Promise<void> {
		await this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: ViewHtmlComponent,
			data: {
				model: this.model
			},
			modalConfiguration: {
				title: 'View HTML',
				draggable: true,
				modalCustomClass: 'custom-notice-view-html-dialog',
				modalSize: ModalSize.CUSTOM
			}
		}).toPromise();
	}

	/**
	 * Determines if all the field forms comply with the validation rules
	 * @param {any} form  - Main form holding all the field
	 */
	protected formValid(): boolean {
		const noticeType = this.noticeType && this.noticeType.typeId;
		const isValid =
			this.model &&
			this.model.title &&
			this.isValidHtmlText() && (noticeType || noticeType === 0) && this.form.valid;

		const returnValue =
			noticeType === this.MANDATORY
				? isValid && this.model.acknowledgeLabel && this.model.acknowledgeLabel.trim() !== ''
				: isValid;

		return returnValue;
	}

	private isValidHtmlText(): boolean {
		return (
			this.model.htmlText && this.model.htmlText.trim().length > 0
		);
	}

	protected isCreateEditAvailable(): boolean {
		return this.action === 0
			? this.permissionService.hasPermission(Permission.NoticeEdit)
			: this.permissionService.hasPermission(Permission.NoticeEdit);
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
		this.setTitle(this.getModalTitle(this.action));
	}

	/**
	 * Grab the current html value emitted by rich text editor
	 */
	onValueChange(value: string) {
		this.model.htmlText = value;
	}

	/**
	 * Set the maximum value for the date range
	 */
	setMaxDate(value: any) {
		this.maxDate = value;
	}

	/**
	 * Set the minimu value for the date range
	 */
	setMinDate(value: any) {
		this.minDate = value;
	}

	/**
	 * Get a field from the form by control name
	 * @param {any} form  - Main form holding all the field
	 * @param {string} controlName - Name of th field to get
	 * @returns {any} - Returns the field or null if not found
	 */
	public getFormField(form: any, controlName: string): any {
		const field = R.pathOr(null, ['controls', controlName], form);

		return field === null
			? null
			: {
				valid: field.valid,
				touched: field.touched,
				dirty: field.dirty,
				errors: field.errors || {},
			};
	}

	/**
	 * Based on modalType action returns the corresponding title
	 * @param {ActionType} modalType
	 * @returns {string}
	 */
	getModalTitle(modalType: ActionType): string {
		if (modalType === ActionType.Edit) {
			return this.translatePipe.transform('NOTICE.EDIT_NOTICE');
		}

		if (modalType === ActionType.Create) {
			return this.translatePipe.transform('NOTICE.NOTICE_CREATE');
		}

		if (modalType === ActionType.View) {
			return this.translatePipe.transform('NOTICE.SHOW_NOTICE');
		}

		return '';
	}

	/**
	 * On change the notice type unpdate get the typeid
	 */
	onChangeNoticeType(value: any) {
		this.model.typeId = value && value.typeId;
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
