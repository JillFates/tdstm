// Angular
import {Component, ViewChild, OnInit, Output, EventEmitter} from '@angular/core';
import {FormControl} from '@angular/forms';
// Component
// Service
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {StringUtils} from '../../../../shared/utils/string.utils';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import {EventsService} from '../../service/events.service';

// Kendo
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
// Model
import {NewsDetailModel} from '../../model/news.model';
import {Permission} from '../../../../shared/model/permission.model';
@Component({
	selector: 'tds-news-create-edit',
	templateUrl: 'news-create-edit.component.html'
})
export class NewsCreateEditComponent implements OnInit {
	constructor(
		public model: NewsDetailModel,
		public activeDialog: UIActiveDialogService,
		private promptService: UIPromptService,
		private permissionService: PermissionService,
		private eventsService: EventsService) {
	}

	ngOnInit() {
		console.log('On init');
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

	protected onDelete(): void {
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
	protected onSave(): void {
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
	protected formValid(): boolean {
		return true;
	}

	protected isCreateEditAvailable(): boolean {
		return	this.permissionService.hasPermission(Permission.NewsEdit);
	}

	protected isDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.NewsDelete);
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return false;
	}

	isCreate(): boolean {
		return !Boolean(this.model.commentObject.id);
	}
}
