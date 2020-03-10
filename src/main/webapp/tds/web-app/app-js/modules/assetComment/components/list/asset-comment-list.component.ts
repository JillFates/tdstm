import { Component, ElementRef, HostListener, OnDestroy, OnInit, Renderer2 } from '@angular/core';
import { CompositeFilterDescriptor, process, State } from '@progress/kendo-data-query';
import {
	DIALOG_SIZE,
	GRID_DEFAULT_PAGE_SIZE,
	GRID_DEFAULT_PAGINATION_OPTIONS,
	ModalType
} from '../../../../shared/model/constants';
import { ActionType, COLUMN_MIN_WIDTH } from '../../../dataScript/model/data-script.model';
import { CellClickEvent, GridDataResult } from '@progress/kendo-angular-grid';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { AssetCommentService } from '../../service/asset-comment.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { PreferenceService } from '../../../../shared/services/preference.service';
import { ActivatedRoute } from '@angular/router';
import { AssetCommentColumnModel, AssetCommentModel } from '../../model/asset-comment.model';
import {Permission} from '../../../../shared/model/permission.model';
import { AssetCommentViewEditComponent } from '../view-edit/asset-comment-view-edit.component';
import { ReplaySubject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { COMMON_SHRUNK_COLUMNS, COMMON_SHRUNK_COLUMNS_WIDTH } from '../../../../shared/constants/common-shrunk-columns';
import {AssetShowComponent} from '../../../assetExplorer/components/asset/asset-show.component';

declare var jQuery: any;

@Component({
	selector: `asset-comment-list`,
	templateUrl: 'asset-comment-list.component.html',
})
export class AssetCommentListComponent implements OnInit, OnDestroy {
	private state: State = {
		sort: [{
			dir: 'asc',
			field: 'name'
		}],
		filter: {
			filters: [],
			logic: 'and'
		}
	};
	public skip = 0;
	public pageSize = GRID_DEFAULT_PAGE_SIZE;
	public defaultPageOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	public assetCommentColumnModel = null;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: AssetCommentModel[];
	public dateFormat = '';
	commonShrunkColumns = COMMON_SHRUNK_COLUMNS;
	commonShrunkColumnWidth = COMMON_SHRUNK_COLUMNS_WIDTH;
	unsubscribeOnDestroy$: ReplaySubject<void> = new ReplaySubject(1);

	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private assetCommentService: AssetCommentService,
		private prompt: UIPromptService,
		private preferenceService: PreferenceService,
		private route: ActivatedRoute,
		private elementRef: ElementRef,
		private renderer: Renderer2) {
		this.state.take = this.pageSize;
		this.state.skip = this.skip;
		this.resultSet = this.route.snapshot.data['assetComments'];
		this.gridData = process(this.resultSet, this.state);
	}

	ngOnInit() {
		this.preferenceService.getUserDatePreferenceAsKendoFormat()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((dateFormat) => {
				this.dateFormat = dateFormat;
				this.assetCommentColumnModel = new AssetCommentColumnModel(`{0:${ dateFormat }}`);
			});
	}

	protected filterChange(filter: CompositeFilterDescriptor): void {
		this.state.filter = filter;
		this.gridData = process(this.resultSet, this.state);
	}

	protected sortChange(sort): void {
		this.state.sort = sort;
		this.gridData = process(this.resultSet, this.state);
	}

	/**
	 * Returns whether or not any filters are applied to the grid.
	 */
	hasFilterApplied(): boolean {
		return this.state.filter.filters.length > 0;
	}

	protected onFilter(column: any): void {
		const root = this.assetCommentService.filterColumn(column, this.state);
		this.filterChange(root);
	}

	public onClearFilters(): void {
		this.state.filter.filters = [];
		this.assetCommentColumnModel.columns.forEach((column) => {
			delete column.filter;
		});
		this.onFilter({filter: ''});
	}

	protected clearValue(column: any): void {
		this.assetCommentService.clearFilter(column, this.state);
		this.filterChange(this.state.filter);
	}

	/**
	 * Select the current element and open the Edit Dialog
	 * @param dataItem
	 */
	protected onEdit(dataItem: any): void {
		this.openAssetCommentDialogViewEdit(dataItem, ModalType.EDIT);
	}

	/**
	 * Check the field clicked and if appropriate open the comment view or the asset details
	 * @param {SelectionEvent} event
	 */
	protected cellClick(event: CellClickEvent): void {
		if (event.columnIndex === 1) {
			this.openAssetCommentDialogViewEdit(event['dataItem'], ModalType.VIEW);
		} else {
			if (event.columnIndex === 2) {
				this.openAssetDetails(event.dataItem.assetEntityId, event.dataItem.assetClass.name);
			}
		}
	}

	protected reloadData(): void {
		this.assetCommentService.getAssetComments()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(
				(result) => {
					this.resultSet = result;
					this.gridData = process(this.resultSet, this.state);
					setTimeout(() => this.forceDisplayLastRowAddedToGrid(), 100);
				},
				(err) => console.log(err));
	}

	/**
	 * This work as a temporary fix.
	 * TODO: talk when Jorge Morayta get's back to do a proper/better fix.
	 */
	private forceDisplayLastRowAddedToGrid(): void {
		const lastIndex = this.gridData.data.length - 1;
		let target = this.elementRef.nativeElement.querySelector(`tr[data-kendo-grid-item-index="${ lastIndex }"]`);
		this.renderer.setStyle(target, 'height', '36px');
	}

	/**
	 * Open the asset show details
	 * @param assetEntityId
	 * @param assetType
	 */
	private openAssetDetails(assetEntityId: string, assetClassName: string) {
		this.dialogService.open(AssetShowComponent, [
				{provide: 'ID', useValue: assetEntityId},
				{provide: 'ASSET', useValue: assetClassName}
			], DIALOG_SIZE.LG)
		.then(result => {
			// Do nothing
		}).catch(result => {
			// Do nothing
		});
	}

	/**
	 * Open The Dialog to Create, View or Edit the Provider
	 * @param {ProviderModel} providerModel
	 * @param {number} actionType
	 */
	private openAssetCommentDialogViewEdit(comment: any, type: ModalType): void {
		let commentModel: AssetCommentModel = {
			id: comment.id,
			modal: {
				type: type
			},
			archive: comment.dateResolved !== null,
			comment: comment.comment,
			category: comment.category,
			assetClass: {
				text: comment.assetType,
			},
			asset: {
				id: comment.assetEntityId,
				text: comment.assetName
			},
			lastUpdated: comment.lastUpdated,
			dateCreated: comment.dateCreated
		};
		this.dialogService.extra(AssetCommentViewEditComponent, [
			{ provide: AssetCommentModel, useValue: commentModel }
		]).then(result => {
			this.reloadData();
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * Make the entire header clickable on Grid
	 * @param event: any
	 */
	public onClickTemplate(event: any): void {
		if (event.target && event.target.parentNode) {
			event.target.parentNode.click();
		}
	}

	/**
	 * Manage Pagination
	 * @param {PageChangeEvent} event
	 */
	public pageChange(event: any): void {
		this.skip = event.skip;
		this.state.skip = this.skip;
		this.state.take = event.take || this.state.take;
		this.pageSize = this.state.take;
		this.gridData = process(this.resultSet, this.state);
		// Adjusting the locked column(s) height to prevent cut-off issues.
		jQuery('.k-grid-content-locked').addClass('element-height-100-per-i');
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

	protected isCommentEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.CommentEdit);
	}
}
