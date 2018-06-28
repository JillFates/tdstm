import {Component} from '@angular/core';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {TagService} from '../../service/tag.service';
import {TagModel} from '../../model/tag.model';
import {TagListColumnsModel} from '../../model/tag-list-columns.model';
import {FormControl, FormGroup, Validators} from '@angular/forms';

@Component({
	selector: 'tag-list',
	templateUrl: '../tds/web-app/app-js/modules/assetTags/components/tag-list/tag-list.component.html'
})
export class TagListComponent {

	protected gridSettings: DataGridOperationsHelper;
	protected gridColumns: TagListColumnsModel;
	private editedRowIndex: number;
	private editedTag: TagModel;

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

	/**
	 * TODO: document
	 * @param {any} sender
	 */
	protected addHandler({sender}) {
		this.closeEditor(sender);

		sender.addRow(new TagModel());
	}

	protected editHandler({sender, rowIndex, dataItem}) {
		console.log(dataItem);
		// close the previously edited item
		this.closeEditor(sender);

		// track the most recently edited row
		// it will be used in `closeEditor` for closing the previously edited row
		this.editedRowIndex = rowIndex;

		// clone the current - `[(ngModel)]` will modify the original item
		// use this copy to revert changes
		this.editedTag = Object.assign({}, dataItem);

		// edit the row
		sender.editRow(rowIndex);
	}

	protected saveHandler({sender, rowIndex, dataItem, isNew}) {
		// this.editService.save(dataItem, isNew);

		sender.closeRow(rowIndex);

		this.editedRowIndex = undefined;
		this.editedTag = undefined;
	}

	protected cancelHandler({sender, rowIndex}) {
		// call the helper method
		this.closeEditor(sender, rowIndex);
	}

	private closeEditor(grid, rowIndex = this.editedRowIndex) {
		// close the editor
		grid.closeRow(rowIndex);

		// revert the data item to original state
		if (this.editedTag) {
			this.tagService.getTag(this.editedTag.id).subscribe( (result: TagModel) => {
				let match = this.gridSettings.resultSet.find( (item: TagModel) => {
					return item.id === result.id;
				});
				Object.assign(result, match);
			});
		}

		// reset the helpers
		this.editedRowIndex = undefined;
		this.editedTag = undefined;
	}
}