// Angular
import {Component, ComponentFactoryResolver, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
// Service
import {PermissionService} from '../../../../shared/services/permission.service';
import {EventsService} from '../../../event/service/events.service';
import {UserContextService} from '../../../auth/service/user-context.service';
// Model
import {Permission} from '../../../../shared/model/permission.model';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {NgForm} from '@angular/forms';
import {ActionType} from '../../../dataScript/model/data-script.model';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
import {EventNewsModel} from '../../model/event-news.model';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {UserContextModel} from '../../../auth/model/user-context.model';

@Component({
	selector: 'tds-news-create-edit',
	templateUrl: 'event-news-view-edit.component.html'
})
export class EventNewsViewEditComponent extends Dialog implements OnInit {
	@Input() data: any;
	@ViewChild('eventNewsForm', {read: NgForm, static: true}) eventNewsForm: NgForm;
	@ViewChild('commentTextArea', {static: false}) commentTextArea: ElementRef;
	public eventNewsModel: EventNewsModel;
	public modalTitle: string;
	public actionTypes = ActionType;
	public modalType = ActionType.VIEW;
	public dateFormat = DateUtils.DEFAULT_FORMAT_DATE;
	private dataSignature: string;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private permissionService: PermissionService,
		private translatePipe: TranslatePipe,
		private eventsService: EventsService,
		private userContext: UserContextService) {
		super();
	}

	ngOnInit(): void {
		this.userContext.getUserContext().subscribe((userContext: UserContextModel) => {
			this.dateFormat = DateUtils.translateDateFormatToKendoFormat(userContext.dateFormat);
		});
		this.eventNewsModel = Object.assign({}, this.data.eventNewsModel);
		this.modalType = this.data.actionType;
		this.modalTitle = this.getModalTitle(this.modalType);
		this.dataSignature = JSON.stringify(this.eventNewsModel);

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			tooltipText: 'Edit',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.VIEW,
			disabled: () => !this.permissionService.hasPermission(Permission.ProviderUpdate),
			active: () => this.modalType === this.actionTypes.EDIT,
			type: DialogButtonType.ACTION,
			action: this.changeToEditEventNews.bind(this)
		});

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			tooltipText: 'Save',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.CREATE,
			disabled: () => !this.eventNewsForm.form.valid || !this.eventNewsForm.form.dirty,
			type: DialogButtonType.ACTION,
			action: this.onSaveEventNews.bind(this)
		});

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			tooltipText: 'Delete',
			show: () => this.modalType !== this.actionTypes.CREATE,
			disabled: () => !this.isDeleteAvailable(),
			type: DialogButtonType.ACTION,
			action: this.onDelete.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			tooltipText: ((this.modalType === this.actionTypes.VIEW) ? 'Close' : 'Cancel'),
			show: () => this.modalType === this.actionTypes.VIEW || this.modalType === this.actionTypes.CREATE,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			tooltipText: 'Cancel',
			show: () => this.modalType === this.actionTypes.EDIT,
			type: DialogButtonType.ACTION,
			action: this.cancelEditDialog.bind(this)
		});
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((result: any) => {
				if (result.confirm === DialogConfirmAction.CONFIRM) {
					this.onCancelClose();
				}
			});
		} else {
			this.onCancelClose();
		}
	}

	public cancelEditDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((result: any) => {
				if (result.confirm === DialogConfirmAction.CONFIRM && !this.data.openFromList) {
					// Put back original model
					this.eventNewsModel = JSON.parse(this.dataSignature);
					this.dataSignature = JSON.stringify(this.eventNewsModel);
					this.modalType = this.actionTypes.VIEW;
					this.setTitle(this.getModalTitle(this.modalType));
				} else {
					this.onCancelClose();
				}
			});
		} else {
			if (!this.data.openFromList) {
				this.modalType = this.actionTypes.VIEW;
				this.setTitle(this.getModalTitle(this.modalType));
			} else {
				this.onCancelClose();
			}
		}
	}

	/**
	 * Create Edit a Provider
	 */
	protected onSaveEventNews(): void {
		if (this.eventNewsModel.newsId) {
			this.eventsService.updateNews(this.eventNewsModel).subscribe(
				(result: any) => {
					this.onAcceptSuccess(result);
				},
				err => console.log(err)
			);
		} else {
			this.eventsService.saveNews(this.eventNewsModel).subscribe(
				(result: any) => {
					this.onAcceptSuccess(result);
				},
				err => console.log(err)
			);
		}
	}

	/**
	 * Change the View Mode to Edit Mode
	 */
	protected changeToEditEventNews(): void {
		this.modalType = this.actionTypes.EDIT;
		this.setTitle(this.getModalTitle(this.modalType));
	}

	/**
	 * Based on modalType action returns the corresponding title
	 * @param {ActionType} modalType
	 * @returns {string}
	 */
	private getModalTitle(modalType: ActionType): string {
		if (modalType === ActionType.EDIT) {
			setTimeout(() => {
				this.onSetUpFocus(this.commentTextArea);
			});
		}
		return modalType === ActionType.EDIT
			? 'Event News Edit'
			: 'Event News Detail';
	}

	/**
	 * Delete the selected Event News
	 * @param dataItem
	 */
	private onDelete = async (): Promise<void> => {
		try {
			if (this.isDeleteAvailable()) {

				const confirmation = await this.dialogService
					.confirm(
						this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_TITLE'),
						this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.DELETE_ITEM_CONFIRMATION')
					).toPromise();
				if (confirmation) {
					if (confirmation.confirm === DialogConfirmAction.CONFIRM) {
						await this.eventsService.deleteNews({id: this.eventNewsModel.newsId}).toPromise();
						this.onCancelClose();
					}
				}
			}
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}

	/**
	 * Determines if all the field forms comply with the validation rules
	 */
	public formValid(): boolean {
		return true;
	}

	/**
	 * Based on the permissions determine if the user
	 * has create/edit permissions
	 * @returns {boolean}
	 */
	public isCreateEditAvailable(): boolean {
		return	this.permissionService.hasPermission(Permission.NewsEdit);
	}

	/**
	 * Based on the permissions determine if the user
	 * has delete permissions
	 * @returns {boolean}
	 */
	public isDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.NewsDelete);
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.eventNewsModel);
	}
}
