// Angular
import {Component} from '@angular/core';

// Service
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {EventsService} from '../../service/events.service';

// Model
import {NewsDetailModel} from '../../model/news.model';
import {Permission} from '../../../../shared/model/permission.model';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'tds-news-create-edit',
	templateUrl: 'news-create-edit.component.html'
})
export class NewsCreateEditComponent {
	constructor(
		public model: NewsDetailModel,
		public activeDialog: UIActiveDialogService,
		private promptService: UIPromptService,
		private permissionService: PermissionService,
		private translatePipe: TranslatePipe,
		private eventsService: EventsService) {
	}

	/**
	 * On cancel edition show a prompt to the user, this action will loose the changes
	*/
	protected cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.promptService.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'),
			)
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

	/**
	 * On delete news shows the confirmation dialog
	 * if the user decides continue call the endpoint to delete the record
	*/
	public onDelete(): void {
		this.promptService.open('Confirmation Required', 'You are about to delete the selected item. Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.eventsService.deleteNews(this.getPayloadFromModel())
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
		const payload = {
			message: this.model.commentObject.message,
			isArchived: this.model.commentObject.isArchived ? 1 : 0,
			resolution: this.model.commentObject.resolution
		};

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
				this.activeDialog.close();
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
		return false;
	}

	/**
	 * Determine based on the id if the model is
	 * intented to be used for a create operation
	 * @returns {boolean}
	 */
	public isCreate(): boolean {
		return !Boolean(this.model.commentObject.id);
	}
}
