import {Component, Input, Output, EventEmitter, ViewEncapsulation, Inject, ViewChild} from '@angular/core';

import { ViewSpec, ViewColumn, VIEW_COLUMN_MIN_WIDTH } from '../../model/view-spec.model';
import { State } from '@progress/kendo-data-query';
import {GridDataResult, DataStateChangeEvent, RowClassArgs} from '@progress/kendo-angular-grid';
import { PreferenceService, PREFERENCES_LIST } from '../../../../shared/services/preference.service';
import { Observable } from 'rxjs/Observable';

import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { DomainModel } from '../../../fieldSettings/model/domain.model';
import {
	SEARCH_QUITE_PERIOD, GRID_DEFAULT_PAGINATION_OPTIONS, GRID_DEFAULT_PAGE_SIZE, KEYSTROKE,
	DIALOG_SIZE
} from '../../../../shared/model/constants';
import { AssetShowComponent } from '../asset/asset-show.component';
import { FieldSettingsModel, FIELD_NOT_FOUND } from '../../../fieldSettings/model/field-settings.model';
import { NotifierService } from '../../../../shared/services/notifier.service';
import {TagModel} from '../../../assetTags/model/tag.model';
import {AssetTagSelectorComponent} from '../../../../shared/components/asset-tag-selector/asset-tag-selector.component';
import {BulkActionResult, BulkActions} from '../bulk-change/model/bulk-change.model';
import {CheckboxState, CheckboxStates} from '../../tds-checkbox/model/tds-checkbox.model';
import {BulkCheckboxService} from '../../service/bulk-checkbox.service';

const {
	ASSET_JUST_PLANNING: PREFERENCE_JUST_PLANNING,
	ASSET_LIST_SIZE: PREFERENCE_LIST_SIZE
} = PREFERENCES_LIST;
declare var jQuery: any;

@Component({
	selector: 'asset-explorer-view-grid',
	exportAs: 'assetExplorerViewGrid',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-grid/asset-explorer-view-grid.component.html'
})
export class AssetExplorerViewGridComponent {
	@Input() model: ViewSpec;
	@Output() modelChange = new EventEmitter<boolean>();
	@Input() edit: boolean;
	@Input() metadata: any;
	@ViewChild('tagSelector') tagSelector: AssetTagSelectorComponent;
	@Input()
	set viewId(viewId: number) {
		this._viewId = viewId;
		// changing the view reset selections
		this.bulkCheckboxService.setCurrentState(CheckboxStates.unchecked);
	}

	fields = [];
	justPlanning = false;
	VIEW_COLUMN_MIN_WIDTH = VIEW_COLUMN_MIN_WIDTH;
	gridMessage = 'ASSET_EXPLORER.GRID.INITIAL_VALUE';
	showMessage = true;
	typingTimeout: any;

	// Pagination Configuration
	notAllowedCharRegex = /ALT|ARROW|F+|ESC|TAB|SHIFT|CONTROL|PAGE|HOME|PRINT|END|CAPS|AUDIO|MEDIA/i;
	private maxDefault = GRID_DEFAULT_PAGE_SIZE;
	private maxOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	private _viewId: number;
	public fieldNotFound = FIELD_NOT_FOUND;

	state: State = {
		skip: 0,
		take: this.maxDefault,
		sort: []
	};
	gridData: GridDataResult;
	selectAll = false;
	private columnFiltersOldValues = [];
	protected tagList: Array<TagModel> = [];
	public bulkItems: number[] = [];

	constructor(
		private preferenceService: PreferenceService,
		private bulkCheckboxService: BulkCheckboxService,
		@Inject('fields') fields: Observable<DomainModel[]>,
		private notifier: NotifierService,
		private dialog: UIDialogService) {

		this.getPreferences().subscribe((preferences: any) => {
				this.state.take  = parseInt(preferences[PREFERENCE_LIST_SIZE], 10) || 25;
				this.bulkCheckboxService.setPageSize(this.state.take);
				this.justPlanning =  preferences[PREFERENCE_JUST_PLANNING].toString() ===  'true';
				this.onReload();
			});
		fields.subscribe((result: DomainModel[]) => {
			this.fields = result.reduce((p, c) => {
				return p.concat(c.fields);
			}, []).map((f: FieldSettingsModel) => {
				return {
					key: `${f['domain']}_${f.field}`,
					label: f.label
				};
			});
		}, (err) => console.log(err));
		// Listen to any Changes outside the model, like Asset Edit Views
		this.eventListeners();
	}

	private getPreferences(): Observable<any> {
		return this.preferenceService.getPreferences(PREFERENCE_LIST_SIZE, PREFERENCE_JUST_PLANNING);
	}

	/**
	 * Reload the current Kendo List when an event that requires the changes occurs completely out of the context.
	 */
	private eventListeners() {
		this.notifier.on('reloadCurrentAssetList', (event) => {
			this.onReload();
		});
	}

	/**
	 * Draws the Label property
	 * @param {ViewColumn} column
	 * @returns {string}
	 */
	getPropertyLabel(column: ViewColumn): string {
		let field = this.fields.find(f => f.key === `${column.domain}_${column.property}`);
		if (field) {
			return field.label;
		}
		column.notFound = true;
		return column.label;
	}

	rowCallbackClass(context: RowClassArgs) {
		let obj = {};
		obj[context.dataItem.common_assetClass.toLowerCase()] = true;
		return obj;
	}

	onClearFilters(): void {
		this.model.columns.forEach((c: ViewColumn) => {
			c.filter = '';
		});
		this.onFilter();
		if (this.tagSelector) {
			this.tagSelector.reset();
		}
	}

	hasFilterApplied(): boolean {
		return this.model.columns.filter((c: ViewColumn) => c.filter).length > 0;
	}

	clearText(column: ViewColumn): void {
		this.bulkCheckboxService.handleFiltering();
		if (column.filter) {
			column.filter = '';
			this.state.skip = 0;
			if ( this.preventFilterSearch(column)) {
				return; // prevent search
			}
			this.onReload();
		}
	}

	onReload(): void {
		this.modelChange.emit();
	}

	onFilter(): void {
		this.state.skip = 0;
		this.onReload();
	}

	private preventFilterSearch(column: ViewColumn): boolean {
		let key = `${column.domain}_${column.property}`;
		let oldVal = this.columnFiltersOldValues[key];
		this.columnFiltersOldValues[key] = column.filter;
		return oldVal === column.filter;
	}

	onFilterKeyUp(e: KeyboardEvent, column?: any): void {
		if ( this.preventFilterSearch(column)) {
			return; // prevent search
		}
		if (e.code === KEYSTROKE.ENTER) {
			this.onFilter();
		} else if (!this.notAllowedCharRegex.test(e.code)) {
			clearTimeout(this.typingTimeout);
			this.typingTimeout = setTimeout(() => this.onFilter(), SEARCH_QUITE_PERIOD);
		}
	}

	onFilterKeyDown(e: KeyboardEvent): void {
		this.bulkCheckboxService.handleFiltering();

		if (!this.notAllowedCharRegex.test(e.code)) {
			clearTimeout(this.typingTimeout);
		}
	}

	apply(data: any): void {
		this.gridMessage = 'ASSET_EXPLORER.GRID.NO_RECORDS';

		this.bulkCheckboxService.initializeKeysBulkItems(data.assets.map(asset => asset.common_id));

		this.gridData = {
			data: data.assets,
			total: data.pagination.total
		};
		this.showMessage = data.pagination.total === 0;
		this.notifier.broadcast({
			name: 'grid.header.position.change'
		});
		// when dealing with locked columns Kendo grid fails to update the height, leaving a lot of empty space
		jQuery('.k-grid-content-locked').addClass('element-height-100-per-i');
	}

	clear(): void {
		this.showMessage = true;
		this.gridMessage = 'ASSET_EXPLORER.GRID.SCHEMA_CHANGE';
		this.gridData = null;
		// when dealing with locked columns Kendo grid fails to update the height, leaving a lot of empty space
		jQuery('.k-grid-content-locked').addClass('element-height-100-per-i');
		this.state = {
			skip: 0,
			take: this.state.take,
			sort: []
		};
	}

	protected dataStateChange(state: DataStateChangeEvent): void {
		this.state = state;
		if (state.sort[0]) {
			// Invert the Order to remove the Natural/Default from the UI (no arrow)
			if (!state.sort[0].dir) {
				state.sort[0].dir = (this.model.sort.order === 'a' ? 'desc' : 'asc');
			}

			let field = state.sort[0].field.split('_');
			this.model.sort.domain = field[0];
			this.model.sort.property = field[1];
			this.model.sort.order = state.sort[0].dir === 'asc' ? 'a' : 'd';
		}
		this.modelChange.emit();
	}

	protected createDependencyPromise(assetClass: string, id: number) {
		setTimeout(() => {
			this.dialog.open(AssetShowComponent, [
				{ provide: 'ID', useValue: id },
				{ provide: 'ASSET', useValue: assetClass }],
				DIALOG_SIZE.LG).then(x => {
					this.createDependencyPromise(x.assetClass, x.id);
				}).catch(x => {
					console.log(x);
				});
		}, 500);
	}

	protected onShow(data: any) {
		this.dialog.open(AssetShowComponent, [
			{ provide: 'ID', useValue: data['common_id'] },
			{ provide: 'ASSET', useValue: data['common_assetClass'] }],
			DIALOG_SIZE.LG, false).then(x => {
				if (x) {
					this.createDependencyPromise(x.assetClass, x.id);
				}
			}).catch(x => {
				console.log(x);
			});
	}

	onWidthChange(data: any) {
		this.model.columns.filter((c: ViewColumn) =>
			data[0].column.field === `${c.domain}_${c.property}`
		).forEach((c: ViewColumn) => {
			c.width = data[0].newWidth;
		});
	}

	onChangeBulkCheckbox(checkboxState: CheckboxState): void {
		this.bulkCheckboxService.changeState(checkboxState);
	}

	checkItem(id: number, checked: boolean): void {
		this.bulkCheckboxService.checkItem(id, checked, this.gridData.data.length);
	}

	onBulkOperationResult(operationResult: BulkActionResult): void {
		if (operationResult.success) {
			this.bulkCheckboxService.uncheckItems();
			this.onReload();
		}
	}

	/**
	 * Make the entire header clickable on Grid
	 * @param event:any
	 */
	public onClickTemplate(event: any): void {
		if (event.target && event.target.parentNode) {
			event.target.parentNode.click();
		}
	}

	/**
	 * On cell click event.
	 * Determines if cell clicked property is either assetName or assetId and opens detail popup.
	 * @param e
	 */
	private cellClick(e): void {
		if (['common_assetName', 'common_id'].indexOf(e.column.field) !== -1) {
			jQuery('tr.k-state-selected').removeClass('k-state-selected');
			jQuery(`tr[data-kendo-grid-item-index=${e.rowIndex}]`).addClass('k-state-selected');
			this.onShow(e.dataItem);
		}
	}

	/**
	 * Returns specific class name based on the cell property name.
	 * @param {ViewColumn} column
	 * @returns {string} asset-detail-link for assetName or assetId.
	 */
	private getCellClass(column: ViewColumn): string {
		if (['common_assetName', 'common_id'].indexOf(column.domain + '_' + column.property) !== -1) {
			let classColumn = 'asset-detail-link';
			if (column.property === 'assetName') {
				classColumn += ' asset-detail-name-column';
			}
			return classColumn;
		}
		return '';
	}

	onChangeJustPlanning(isChecked = false): void {
		this.preferenceService.setPreference(PREFERENCE_JUST_PLANNING, isChecked.toString()).subscribe(() => {
			this.onReload();
		});
	}

	/**
	 * On Asset Tag Filter change, run the query.
	 * @param $event
	 */
	protected onTagFilterChange(column: ViewColumn, $event: any): void {
		column.filter = '';
		let operator = $event.operator && $event.operator === 'ALL' ? '&' : '|';
		let selectedTagsFilter = ($event.tags as Array<TagModel>).map( tag => tag.id).join(`${operator}`);
		column.filter = selectedTagsFilter;
		this.onFilter();
	}

	onClickBulkButton(): void {
		this.bulkCheckboxService.getBulkSelectedItems(this._viewId, this.model, this.justPlanning)
			.then((results: number[]) => {
				this.bulkItems = [...results];
			})
			.catch ((err) => console.log('Error:', err))
	}

	hasSelectedItems(): boolean {
		return this.bulkCheckboxService.hasSelectedItems();
	}

	getSelectedItemsCount(): number {
		const allCounter = (this.gridData && this.gridData.total) || 0;
		return this.bulkCheckboxService.getSelectedItemsCount(allCounter)
	}
}
