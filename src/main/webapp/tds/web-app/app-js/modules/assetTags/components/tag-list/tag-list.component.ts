// Angular
import {
	Component,
	ComponentFactoryResolver,
	ElementRef,
	HostListener,
	OnDestroy,
	OnInit,
	ViewChild
} from '@angular/core';
// Services
import {PermissionService} from '../../../../shared/services/permission.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {TagService} from '../../service/tag.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {DateUtils} from '../../../../shared/utils/date.utils';
// Components
import {TagMergeDialogComponent} from '../tag-merge/tag-merge-dialog.component';
import {DialogConfirmAction, DialogService, HeaderActionButtonData, ModalSize} from 'tds-component-library';
// Models
import {TagModel} from '../../model/tag.model';
import {TagListColumnsModel} from '../../model/tag-list-columns.model';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {Permission} from '../../../../shared/model/permission.model';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {
	PROMPT_DEFAULT_TITLE_KEY,
} from '../../../../shared/model/constants';
// Others
import {takeUntil} from 'rxjs/operators';
import {ReplaySubject} from 'rxjs';

@Component({
	selector: 'tag-list',
	templateUrl: 'tag-list.component.html',
	providers: [TranslatePipe],
})
export class TagListComponent implements OnInit, OnDestroy {
	@ViewChild('tagNameInput', {static: false}) tagNameInput: ElementRef;

	public disableClearFilters: Function;
	public headerActionButtons: HeaderActionButtonData[];
	public gridSettings: DataGridOperationsHelper;
	protected gridColumns: TagListColumnsModel;
	protected colorList: Array<string>;
	protected duplicateName = false;
	private editedRowIndex: number;
	private editedTag: TagModel;
	protected dateFormat: string;
	private readonly DELETE_TITLE = 'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_TITLE';
	private readonly CANCEL_TITLE = 'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED';
	private readonly DELETE_CONFIRMATION_MSG = 'ASSET_TAGS.TAG_LIST.DELETE_CONFIRMATION';
	private readonly CANCEL_CONFIRMATION_MSG = 'GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE';

	unsubscribeOnDestroy$: ReplaySubject<void> = new ReplaySubject(1);
	protected showFilters = false;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private tagService: TagService,
		private dialogService: DialogService,
		private permissionService: PermissionService,
		private translatePipe: TranslatePipe,
		private userContext: UserContextService
	) {
	}

	ngOnInit(): void {
		this.disableClearFilters = this.onDisableClearFilter.bind(this);
		this.headerActionButtons = [
			{
				icon: 'plus',
				iconClass: 'is-solid',
				title: this.translatePipe.transform('GLOBAL.CREATE'),
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
							[{dir: 'asc', field: 'name'}], // initial sort config.
							{mode: 'single', checkboxOnly: false}, // selectable config.
							{useColumn: 'id'}
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
	protected onMerge = async (dataItem: TagModel): Promise<void> => {
		try {
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: TagMergeDialogComponent,
				data: {
					tagModel: dataItem,
				},
				modalConfiguration: {
					title: 'Tag Merge',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).toPromise();
			await this.reloadTagList();
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Handles the Remove action on Remove/Delete button click.
	 * @param {any} sender
	 */
	protected removeHandler = async (item: any): Promise<void> => {
		try {
			const confirmation = await this.dialogService.confirm(this.translatePipe.transform(this.DELETE_TITLE), this.translatePipe.transform(this.DELETE_CONFIRMATION_MSG)).toPromise();
			if (confirmation) {
				if (confirmation.confirm === DialogConfirmAction.CONFIRM) {
					this.tagService
						.deleteTag(item.dataItem.id)
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
											: 'an error occurred while deleting the tag.'
									);
								}
							},
							error => this.handleError(error)
						);
				}
			}
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Handles the Add process.
	 * @param {any} sender
	 */
	protected addHandler({sender}): void {
		this.closeEditor(sender);
		sender.addRow(new TagModel());
		setTimeout( () => {
			this.tagNameInput.nativeElement.focus();
		});
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
	protected cancelHandler({sender, rowIndex, dataItem, isNew}): void {

		if (this.isDirty(dataItem, isNew)) {
			this.dialogService.confirm(
				this.translatePipe.transform(this.CANCEL_TITLE),
				this.translatePipe.transform(this.CANCEL_CONFIRMATION_MSG)
			).subscribe((confirmation: any) => {
				if (confirmation) {
					if (confirmation.confirm === DialogConfirmAction.CONFIRM) {
						// call the helper method
						this.closeEditor(sender, rowIndex);
					}
				}
			});
		} else {
			this.closeEditor(sender, rowIndex);
		}
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	public isDirty(dataItem, isNew) {
		if (isNew) {
			return JSON.stringify(dataItem) !== JSON.stringify(new TagModel())
		} else {
			if (this.editedTag) {
				let match = this.gridSettings.resultSet.find((item: TagModel) => {
					return item.id === this.editedTag.id;
				});
				return JSON.stringify(this.editedTag) !== JSON.stringify(match);
			}
			return false;
		}
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
