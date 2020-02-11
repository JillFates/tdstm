// Angular
import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
// Services
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { UserContextService } from '../../../auth/service/user-context.service';
// Components
import { TagMergeDialogComponent } from '../tag-merge/tag-merge-dialog.component';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { HeaderActionButtonData } from 'tds-component-library';

// Models
import { TagModel } from '../../model/tag.model';
import { TagListColumnsModel } from '../../model/tag-list-columns.model';
import { ApiResponseModel } from '../../../../shared/model/ApiResponseModel';
import { Permission } from '../../../../shared/model/permission.model';
import { UserContextModel } from '../../../auth/model/user-context.model';
import {
	DIALOG_SIZE,
	PROMPT_CANCEL,
	PROMPT_CONFIRM,
	PROMPT_DEFAULT_TITLE_KEY,
} from '../../../../shared/model/constants';
// Others
import { DataGridOperationsHelper } from '../../../../shared/utils/data-grid-operations.helper';
import { TagService } from '../../service/tag.service';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import { DateUtils } from '../../../../shared/utils/date.utils';
import { takeUntil } from 'rxjs/operators';
import { ReplaySubject } from 'rxjs';

@Component({
	selector: 'tag-list',
	templateUrl: 'tag-list.component.html',
	providers: [TranslatePipe],
})
export class TagListComponent implements OnInit, OnDestroy {
	public disableClearFilters: Function;
	public headerActionButtons: HeaderActionButtonData[];
	public gridSettings: DataGridOperationsHelper;
	protected gridColumns: TagListColumnsModel;
	protected colorList: Array<string>;
	protected duplicateName = false;
	private editedRowIndex: number;
	private editedTag: TagModel;
	protected dateFormat: string;

	private readonly REMOVE_CONFIRMATION =
		'ASSET_TAGS.TAG_LIST.REMOVE_CONFIRMATION';
	unsubscribeOnDestroy$: ReplaySubject<void> = new ReplaySubject(1);
	protected showFilters = false;

	constructor(
		private tagService: TagService,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private promptService: UIPromptService,
		private translatePipe: TranslatePipe,
		private userContext: UserContextService
	) {}

	ngOnInit(): void {
		this.disableClearFilters = this.onDisableClearFilter.bind(this);
		this.headerActionButtons = [
			{
				icon: 'plus-circle',
				iconClass: 'is-solid',
				title: this.translatePipe.transform('ASSET_TAGS.CREATE_TAG'),
				disabled: !this.canCreate(),
				show: true,
				onClick: this.onAddButton.bind(this),
			},
		];
		this.userContext
			.getUserContext()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((userContext: UserContextModel) => {
				this.dateFormat = DateUtils.translateDateFormatToKendoFormat(
					userContext.dateFormat
				);
				this.gridColumns = new TagListColumnsModel(
					`{0:${this.dateFormat}}`
				);
				this.onLoad();
			});
	}

	/**
	 * Load necessary lists to render the view.
	 */
	private onLoad(): void {
		this.colorList = this.tagService.getTagColorList();
		this.tagService
			.getTags()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(
				(result: ApiResponseModel) => {
					if (result.status === ApiResponseModel.API_SUCCESS) {
						this.gridSettings = new DataGridOperationsHelper(
							result.data,
							[{ dir: 'asc', field: 'name' }], // initial sort config.
							{ mode: 'single', checkboxOnly: false }, // selectable config.
							{ useColumn: 'id' }
						); // checkbox config.
					} else {
						this.handleError(
							result.errors
								? result.errors[0]
								: 'an error ocurred while loading the tag list.'
						);
					}
				},
				error => this.handleError(error)
			);
	}

	/**
	 * On Merge button click.
	 */
	protected onMerge(dataItem: TagModel): void {
		this.dialogService
			.open(
				TagMergeDialogComponent,
				[{ provide: TagModel, useValue: dataItem }],
				DIALOG_SIZE.MD
			)
			.then(result => {
				if (result) {
					this.reloadTagList();
				}
			})
			.catch(result => {
				console.log('Dismissed Dialog');
			});
	}

	/**
	 * Handles the Remove action on Remove/Delete button click.
	 * @param {any} sender
	 */
	protected removeHandler({ dataItem }): void {
		this.promptService
			.open(
				this.translatePipe.transform(PROMPT_DEFAULT_TITLE_KEY),
				this.translatePipe.transform(this.REMOVE_CONFIRMATION),
				this.translatePipe.transform(PROMPT_CONFIRM),
				this.translatePipe.transform(PROMPT_CANCEL)
			)
			.then(
				result => {
					if (result) {
						this.tagService
							.deleteTag(dataItem.id)
							.pipe(takeUntil(this.unsubscribeOnDestroy$))
							.subscribe(
								(result: ApiResponseModel) => {
									if (
										result.status ===
										ApiResponseModel.API_SUCCESS
									) {
										this.reloadTagList();
									} else {
										this.handleError(
											result.errors
												? result.errors[0]
												: 'an error ocurred while deleting the tag.'
										);
									}
								},
								error => this.handleError(error)
							);
					}
				},
				(reason: any) => console.log('confirm rejected', reason)
			);
	}

	/**
	 * Handles the Add process.
	 * @param {any} sender
	 */
	protected addHandler({ sender }): void {
		this.closeEditor(sender);
		sender.addRow(new TagModel());
	}

	/**
	 * Handles the Update process.
	 * @param {any} sender
	 * @param {any} rowIndex
	 * @param {any} dataItem
	 */
	protected editHandler({ sender, rowIndex, dataItem }): void {
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
	protected saveHandler({ sender, rowIndex, dataItem, isNew }): void {
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
	protected cancelHandler({ sender, rowIndex }): void {
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
			let match = this.gridSettings.resultSet.find((item: TagModel) => {
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
		this.tagService
			.createTag(tagModel)
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(
				(result: ApiResponseModel) => {
					if (result.status === ApiResponseModel.API_SUCCESS) {
						this.finishSave(sender, rowIndex);
						this.reloadTagList();
					} else {
						this.handleError(
							result.errors
								? result.errors[0]
								: 'an error ocurred while creating the tag'
						);
					}
				},
				error => this.handleError(error)
			);
	}

	/**
	 * Updates an existing tag.
	 * @param {TagModel} tagModel
	 * @param sender
	 * @param rowIndex
	 */
	private updateTag(tagModel: TagModel, sender, rowIndex): void {
		this.tagService
			.updateTag(tagModel)
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(
				(result: ApiResponseModel) => {
					if (result.status === ApiResponseModel.API_SUCCESS) {
						this.finishSave(sender, rowIndex);
						this.reloadTagList();
					} else {
						this.handleError(
							result.errors
								? result.errors[0]
								: 'an error ocurred while updating the tag'
						);
					}
				},
				error => this.handleError(error)
			);
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
		this.tagService
			.getTags()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(
				(result: ApiResponseModel) => {
					if (result.status === ApiResponseModel.API_SUCCESS) {
						this.gridSettings.reloadData(result.data);
					} else {
						this.handleError(
							result.errors
								? result.errors[0]
								: 'an error ocurred while loading the tag list.'
						);
					}
				},
				error => this.handleError(error)
			);
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
		const match: TagModel = this.gridSettings.resultSet.find(
			(item: TagModel) =>
				item.name.toLowerCase() ===
				dataItem.name.trim().toLocaleLowerCase()
		);
		if (match) {
			this.duplicateName = dataItem.id ? dataItem.id !== match.id : true;
		}
	}

	/**
	 * Clear all filters
	 */
	protected clearAllFilters(): void {
		this.gridSettings.clearAllFilters(this.gridColumns.columns);
		this.reloadTagList();
	}

	/**
	 * Disable clear filters
	 */
	private onDisableClearFilter(): boolean {
		return this.filterCount() === 0;
	}

	/**
	 * Filter Assets Toggle
	 */
	public toggleFilters(): void {
		this.showFilters = !this.showFilters;
	}

	hasFilterApplied(): boolean {
		return this.gridSettings.state.filter.filters.length > 0;
	}

	protected onAddButton() {
		document.getElementById('addButton').click();
	}
	/**
	 * Returns the number of current filters applied.
	 */
	public filterCount(): number {
		return this.gridSettings.getFilterCounter();
	}

	protected canCreate(): boolean {
		return this.permissionService.hasPermission(Permission.TagCreate);
	}

	protected canEdit(): boolean {
		return this.permissionService.hasPermission(Permission.TagEdit);
	}

	protected canDelete(): boolean {
		return this.permissionService.hasPermission(Permission.TagDelete);
	}

	protected canMerge(): boolean {
		return this.permissionService.hasPermission(Permission.TagMerge);
	}

	/**
	 * unsubscribe from all subscriptions on destroy hook.
	 * @HostListener decorator ensures the OnDestroy hook is called on events like
	 * Page refresh, Tab close, Browser close, navigation to another view.
	 */
	@HostListener('window:beforeunload')
	ngOnDestroy(): void {
		this.unsubscribeOnDestroy$.next();
		this.unsubscribeOnDestroy$.complete();
	}
}
