import {Component} from '@angular/core';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {TagService} from '../../service/tag.service';
import {TagModel} from '../../model/tag.model';
import {TagListColumnsModel} from '../../model/tag-list-columns.model';

@Component({
	selector: 'tag-list',
	templateUrl: '../tds/web-app/app-js/modules/assetTags/components/tag-list/tag-list.component.html'
})
export class TagListComponent {

	protected gridSettings: DataGridOperationsHelper;
	protected gridColumns: TagListColumnsModel;

	constructor(
		private tagService: TagService,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private promptService: UIPromptService,
		private notifierService: NotifierService,
		private userPreferenceService: PreferenceService) {

		this.onLoad();
	}

	/**
	 * TODO: document.
	 */
	private onLoad(): void {
		this.gridColumns = new TagListColumnsModel();
		this.tagService.getTags().subscribe( (result: Array<TagModel>) => {
			this.gridSettings = new DataGridOperationsHelper(result,
				[{ dir: 'asc', field: 'Name'}], // initial sort config.
				{ mode: 'single', checkboxOnly: false}, // selectable config.
				{ useColumn: 'id' }); // checkbox config.
		});
	}
}