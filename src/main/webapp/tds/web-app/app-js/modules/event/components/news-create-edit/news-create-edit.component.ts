// Angular
import {Component, Input, OnInit, ViewChild} from '@angular/core';
// Service
import {PermissionService} from '../../../../shared/services/permission.service';
import {EventsService} from '../../service/events.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Model
import {DisplayOptionGeneric, DisplayOptionUser} from '../../model/news.model';
import {NewsDetailModel, CommentType} from '../../model/news.model';
import {Permission} from '../../../../shared/model/permission.model';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
import * as R from 'ramda';
import {NgForm} from '@angular/forms';

@Component({
	selector: 'tds-news-create-edit',
	templateUrl: 'news-create-edit.component.html'
})
export class NewsCreateEditComponent extends Dialog implements OnInit {
	@Input() data: any;

	@ViewChild('newsForm', {read: NgForm, static: true}) newsForm: NgForm;

	public commentType: string;
	public optionGeneric = DisplayOptionGeneric;
	public optionUser = DisplayOptionUser;
	public model: NewsDetailModel;
	private dataSignature: string;

	constructor(
		public dialogService: DialogService,
		private permissionService: PermissionService,
		private translatePipe: TranslatePipe,
		private eventsService: EventsService) {
		super();
	}

	ngOnInit(): void {
		this.model = R.clone(this.data.newsDetailModel);
		this.commentType = CommentType[this.model.commentType];

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => true,
			disabled: () => !this.newsForm.form.valid || this.newsForm.form.pristine,
			type: DialogButtonType.ACTION,
			action: this.onSave.bind(this)
		});

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			show: () => !this.isCreate(),
			disabled: () => !this.isDeleteAvailable(),
			type: DialogButtonType.ACTION,
			action: this.onDelete.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		setTimeout(() => {
			let title = this.translatePipe.transform('NEWS.EDIT_NEWS');
			if (this.isCreate()) {
				title = this.translatePipe.transform('NEWS.CREATE_NEWS');
			}
			this.dataSignature = JSON.stringify(this.model);
			this.setTitle(title);
		});
	}

	/**
	 * On cancel edition show a prompt to the user, this action will loose the changes
	*/
	protected cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			)
				.subscribe((data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM) {
						this.onCancelClose();
					}
				});
		} else {
			this.onCancelClose();
		}
	}

	/**
	 * On delete news shows the confirmation dialog
	 * if the user decides continue call the endpoint to delete the record
	*/
	public onDelete(): void {
		this.dialogService.confirm(
			this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
			'You are about to delete the selected item. Do you want to proceed?'
		)
			.subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					this.eventsService.deleteNews(this.getPayloadFromModel())
						.subscribe(
							res => this.onCancelClose(),
							error => console.error(error));
				}
			});
	}

	/**
	 * Get and clean the payload to be sent to the server to create or edit a notice
	*/
	private getPayloadFromModel(): any {
		const payload = {
			message: this.model.commentObject.message,
			isArchived: this.model.commentObject.isArchived ? 1 : 0,
			resolution: this.model.commentObject.resolution
		};

		if (this.model.commentObject.displayOption) {
			payload['displayOption'] = this.model.commentObject.displayOption;
		}

		if (this.isCreate()) {
			payload['moveEventId'] = this.model.commentObject.moveEvent.id;
		} else {
			payload['id'] = this.model.commentObject.id;
		}

		return payload;
	}

	/**
	 * Save the changes to the news
	 */
	public onSave(): void {
		const payload = this.getPayloadFromModel();

		const updateMethod = payload.id ? this.eventsService.updateNews(payload) : this.eventsService.saveNews(payload);

		updateMethod
			.subscribe((val) => {
				this.onCancelClose();
			}, (error) => {
				console.error('Error:', error);
			});
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
	public isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.model);
	}

	/**
	 * Determine based on the id if the model is
	 * intented to be used for a create operation
	 * @returns {boolean}
	 */
	public isCreate(): boolean {
		return !Boolean(this.model.commentObject.id);
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
