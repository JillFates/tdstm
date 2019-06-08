// Angular
import {Component, ViewChild, OnInit} from '@angular/core';
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

// Kendo
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
// Model
import {NewsModel} from '../../model/news.model';
import {Permission} from '../../../../shared/model/permission.model';
@Component({
	selector: 'tds-news-create-edit',
	templateUrl: 'news-create-edit.component.html'
})
export class NewsCreateEditComponent implements OnInit {
	constructor(
		private model: NewsModel,
		public activeDialog: UIActiveDialogService,
		private promptService: UIPromptService,
		private permissionService: PermissionService) {
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

	protected deleteNotice(): void {
		this.promptService.open('Confirmation Required', 'You are about to delete the selected item. Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					/*
					this.noticeService.deleteNotice(this.model.id.toString())
						.subscribe(
							res => this.activeDialog.close(),
							error => console.error(error));
					*/
				}
			});
	}

	/**
	 * Get and clean the payload to be sent to the server to create or edit a notice
	*/
	private getPayloadFromModel(): any {
		const payload = {};

		return payload;
	}

	/**
	 * Save the current status fo the Notice
	 */
	protected saveNotice(): void {
		const payload = this.getPayloadFromModel();
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
}
