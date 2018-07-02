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
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {DIALOG_SIZE, PROMPT_DEFAULT_TITLE_KEY} from '../../../../shared/model/constants';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {DependencyBatchDetailDialogComponent} from '../../../dependencyBatch/components/dependency-batch-detail-dialog/dependency-batch-detail-dialog.component';
import {ImportBatchModel} from '../../../dependencyBatch/model/import-batch.model';
import {TagMergeDialogComponent} from '../tag-merge/tag-merge-dialog.component';

@Component({
	selector: 'tag-list',
	templateUrl: '../tds/web-app/app-js/modules/assetTags/components/tag-list/tag-list.component.html',
	providers: [TranslatePipe]
})
export class TagListComponent {

	protected gridSettings: DataGridOperationsHelper;
	protected gridColumns: TagListColumnsModel;
	protected colorList: Array<string>;
	protected duplicateName = false;
	private editedRowIndex: number;
	private editedTag: TagModel;

	constructor(
		private tagService: TagService,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private promptService: UIPromptService,
		private translatePipe: TranslatePipe,
		private notifierService: NotifierService,
		private userPreferenceService: PreferenceService) {

		this.onLoad();
	}

	/**
	 * TODO: document.
	 */
	private onLoad(): void {
		this.colorList = this.tagService.getTagColorList();
		this.gridColumns = new TagListColumnsModel();
		this.tagService.getTags().subscribe( (result: Array<TagModel>) => {
			this.gridSettings = new DataGridOperationsHelper(result,
				[{ dir: 'asc', field: 'Name'}], // initial sort config.
				{ mode: 'single', checkboxOnly: false}, // selectable config.
				{ useColumn: 'id' }); // checkbox config.
		}, error => this.handleError(error));
	}

	/**
	 * TODO: document.
	 */
	protected onMerge(dataItem: TagModel): void {
		this.dialogService.open(TagMergeDialogComponent, [
			{ provide: TagModel, useValue: dataItem}
		], DIALOG_SIZE.MD).then(result => {
			if (result) {
				this.reloadTagList();
			}
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * TODO: document
	 * @param {any} sender
	 */
	protected removeHandler({dataItem}): void {
		this.promptService.open(
			'Confirmation Required',
			'This Tag is removed from all linked records and will be deleted. There is no undo for this action.',
			'Confirm', 'Cancel').then(result => {
			if (result) {
				this.tagService.deleteTag(dataItem.id).subscribe( result => {
					this.reloadTagList();
				}, error => this.handleError(error));
			}
		}, (reason: any) => console.log('confirm rejected', reason));
	}

	/**
	 * TODO: document
	 * @param {any} sender
	 */
	protected addHandler({sender}): void {
		this.closeEditor(sender);

		sender.addRow(new TagModel());
	}

	/**
	 * * TODO: document.
	 * @param {any} sender
	 * @param {any} rowIndex
	 * @param {any} dataItem
	 */
	protected editHandler({sender, rowIndex, dataItem}): void {
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

	/**
	 * * TODO: document.
	 * @param {any} sender
	 * @param {any} rowIndex
	 * @param {any} dataItem
	 * @param {any} isNew
	 */
	protected saveHandler({sender, rowIndex, dataItem, isNew}): void {
		const tagModel: TagModel = dataItem as TagModel;
		if (isNew) {
			this.createTag(tagModel, sender, rowIndex);
		} else {
			this.updateTag(tagModel, sender, rowIndex);
		}
	}

	/**
	 * * TODO: document.
	 * @param {any} sender
	 * @param {any} rowIndex
	 */
	protected cancelHandler({sender, rowIndex}): void {
		// call the helper method
		this.closeEditor(sender, rowIndex);
	}

	/**
	 * * TODO: document.
	 * @param grid
	 * @param {number} rowIndex
	 */
	private closeEditor(grid, rowIndex = this.editedRowIndex): void {
		// close the editor
		grid.closeRow(rowIndex);

		// revert the data item to original state
		if (this.editedTag) {
			let match = this.gridSettings.resultSet.find( (item: TagModel) => {
				return item.id === this.editedTag.id;
			});
			Object.assign(match, this.editedTag);
		}

		// reset the helpers
		this.editedRowIndex = undefined;
		this.editedTag = undefined;
	}

	/**
	 * * TODO: document.
	 * @param {TagModel} tagModel
	 * @param sender
	 * @param rowIndex
	 */
	private createTag(tagModel: TagModel, sender, rowIndex): void {
		this.tagService.createTag(tagModel).subscribe( (result: any) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				this.finishSave(sender, rowIndex);
				this.reloadTagList();
			} else {
				this.handleError(result.errors ? result.errors[0] : null);
			}
		}, error => this.handleError(error) );
	}

	/**
	 * TODO: document.
	 * @param {TagModel} tagModel
	 * @param sender
	 * @param rowIndex
	 */
	private updateTag(tagModel: TagModel, sender, rowIndex): void {
		this.tagService.updateTag(tagModel).subscribe( (result: any) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				this.finishSave(sender, rowIndex);
				this.reloadTagList();
			} else {
				this.handleError(result.errors ? result.errors[0] : null);
			}
		}, error => this.handleError(error) );
	}

	/**
	 * * TODO: document.
	 * @param sender
	 * @param rowIndex
	 */
	private finishSave(sender, rowIndex): void {
		// reset the helpers
		sender.closeRow(rowIndex);
		this.editedRowIndex = undefined;
		this.editedTag = undefined;
	}

	/**
	 * * TODO: document.
	 */
	private reloadTagList(): void {
		this.tagService.getTags().subscribe((result: Array<TagModel>) => {
			this.gridSettings.reloadData(result);
		}, error => this.handleError(error));
	}

	/**
	 * TODO: document this.
	 * @param error
	 */
	private handleError(error): void {
		console.log(error);
	}

	/**
	 * TODO: document.
	 * @param {string} name
	 */
	protected validateUniqueName(dataItem: TagModel): void {
		this.duplicateName = false;
		const match: TagModel = this.gridSettings.resultSet.find( item => item.Name.toLowerCase() === dataItem.Name.trim().toLocaleLowerCase());
		if (match) {
			this.duplicateName = dataItem.id ? dataItem.id !== match.id : true;
		}
	}
}