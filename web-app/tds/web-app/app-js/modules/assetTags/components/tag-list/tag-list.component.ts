import {Component} from '@angular/core';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {TagService} from '../../service/tag.service';
import {TagModel} from '../../model/tag.model';
import {TagListColumnsModel} from '../../model/tag-list-columns.model';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {DIALOG_SIZE, PROMPT_CANCEL, PROMPT_CONFIRM, PROMPT_DEFAULT_TITLE_KEY} from '../../../../shared/model/constants';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {TagMergeDialogComponent} from '../tag-merge/tag-merge-dialog.component';
import {Permission} from '../../../../shared/model/permission.model';

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
	protected dateFormat: string;

	private readonly REMOVE_CONFIRMATION = 'ASSET_TAGS.TAG_LIST.REMOVE_CONFIRMATION';

	constructor(
		private tagService: TagService,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private promptService: UIPromptService,
		private translatePipe: TranslatePipe,
		userPreferenceService: PreferenceService) {
			userPreferenceService.getUserDatePreferenceAsKendoFormat().subscribe((dateFormat) => {
				this.dateFormat = dateFormat;
				this.gridColumns = new TagListColumnsModel(`{0:${dateFormat}}`);
				this.onLoad();
			});
	}

	/**
	 * Load necessary lists to render the view.
	 */
	private onLoad(): void {
		this.colorList = this.tagService.getTagColorList();
		this.tagService.getTags().subscribe( (result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				this.gridSettings = new DataGridOperationsHelper(result.data,
					[{ dir: 'asc', field: 'name'}], // initial sort config.
					{ mode: 'single', checkboxOnly: false}, // selectable config.
					{ useColumn: 'id' }); // checkbox config.
			} else {
				this.handleError(result.errors ? result.errors[0] : 'an error ocurred while loading the tag list.');
			}
		}, error => this.handleError(error));
	}

	/**
	 * On Merge button click.
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
	 * Handles the Remove action on Remove/Delete button click.
	 * @param {any} sender
	 */
	protected removeHandler({dataItem}): void {
		this.promptService.open(
			this.translatePipe.transform(PROMPT_DEFAULT_TITLE_KEY),
			this.translatePipe.transform(this.REMOVE_CONFIRMATION),
			this.translatePipe.transform(PROMPT_CONFIRM),
			this.translatePipe.transform(PROMPT_CANCEL)).then(result => {
			if (result) {
				this.tagService.deleteTag(dataItem.id).subscribe( (result: ApiResponseModel) => {
					if (result.status === ApiResponseModel.API_SUCCESS) {
						this.reloadTagList();
					} else {
						this.handleError(result.errors ? result.errors[0] : 'an error ocurred while deleting the tag.');
					}
				}, error => this.handleError(error));
			}
		}, (reason: any) => console.log('confirm rejected', reason));
	}

	/**
	 * Handles the Add process.
	 * @param {any} sender
	 */
	protected addHandler({sender}): void {
		this.closeEditor(sender);
		sender.addRow(new TagModel());
	}

	/**
	 * Handles the Update process.
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
	 * Handles the Save action on button click.
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
	 * Handles the Cancel action on Cancel button click.
	 * @param {any} sender
	 * @param {any} rowIndex
	 */
	protected cancelHandler({sender, rowIndex}): void {
		// call the helper method
		this.closeEditor(sender, rowIndex);
	}

	/**
	 * Closes the current row in edition.
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
	 * Creates a new tag.
	 * @param {TagModel} tagModel
	 * @param sender
	 * @param rowIndex
	 */
	private createTag(tagModel: TagModel, sender, rowIndex): void {
		this.tagService.createTag(tagModel).subscribe( (result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				this.finishSave(sender, rowIndex);
				this.reloadTagList();
			} else {
				this.handleError(result.errors ? result.errors[0] : 'an error ocurred while creating the tag');
			}
		}, error => this.handleError(error) );
	}

	/**
	 * Updates an existing tag.
	 * @param {TagModel} tagModel
	 * @param sender
	 * @param rowIndex
	 */
	private updateTag(tagModel: TagModel, sender, rowIndex): void {
		this.tagService.updateTag(tagModel).subscribe( (result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				this.finishSave(sender, rowIndex);
				this.reloadTagList();
			} else {
				this.handleError(result.errors ? result.errors[0] : 'an error ocurred while updating the tag');
			}
		}, error => this.handleError(error) );
	}

	/**
	 * Common logic for create and update processes.
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
	 * Reloads the current tag list from grid.
	 */
	private reloadTagList(): void {
		this.tagService.getTags().subscribe((result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				this.gridSettings.reloadData(result.data);
			} else {
				this.handleError(result.errors ? result.errors[0] : 'an error ocurred while loading the tag list.');
			}
		}, error => this.handleError(error));
	}

	/**
	 * Generic error handler function.
	 * @param error
	 */
	private handleError(error): void {
		console.log(error);
	}

	/**
	 * Validates the current name on form is unique.
	 * @param {string} name
	 */
	protected validateUniqueName(dataItem: TagModel): void {
		this.duplicateName = false;
		const match: TagModel = this.gridSettings.resultSet.find( (item: TagModel) => item.name.toLowerCase() === dataItem.name.trim().toLocaleLowerCase());
		if (match) {
			this.duplicateName = dataItem.id ? dataItem.id !== match.id : true;
		}
	}

	protected canCreate(): boolean {
		return true; // this.permissionService.permissions[Permission.TagCreate] === 1;
	}

	protected canEdit(): boolean {
		return true; // this.permissionService.permissions[Permission.TagEdit] === 1;
	}

	protected canDelete(): boolean {
		return true; // this.permissionService.permissions[Permission.TagDelete] === 1;
	}

	protected canMerge(): boolean {
		return true; // this.permissionService.permissions[Permission.TagMerge] === 1;
	}
}