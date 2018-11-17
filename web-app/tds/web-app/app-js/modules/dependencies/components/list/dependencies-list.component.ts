import {Component, OnInit, ViewChild} from '@angular/core';
import { State } from '@progress/kendo-data-query';
import { GridDataResult, DataStateChangeEvent} from '@progress/kendo-angular-grid';
import {BehaviorSubject} from 'rxjs';
import {CompositeFilterDescriptor, State, process} from '@progress/kendo-data-query';
import {CellClickEvent, GridDataResult, DataStateChangeEvent} from '@progress/kendo-angular-grid';
import { State } from '@progress/kendo-data-query';

import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {ActivatedRoute} from '@angular/router';
import {DependenciesService} from '../../service/dependencies.service';
import {DependenciesColumnModel} from '../../model/dependencies-column.model';
import {GRID_DEFAULT_PAGINATION_OPTIONS, GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';

import {tap, map, mergeMap} from 'rxjs/operators';
import {BulkCheckboxService} from '../../../assetExplorer/service/bulk-checkbox.service';
import {BulkActionResult, BulkChangeType} from '../../../assetExplorer/components/bulk-change/model/bulk-change.model';
import {CheckboxState, CheckboxStates} from '../../../../shared/components/tds-checkbox/model/tds-checkbox.model';
import {BulkChangeButtonComponent} from '../../../assetExplorer/components/bulk-change/components/bulk-change-button/bulk-change-button.component';
import {DependencyResults} from '../../model/dependencies.model';

@Component({
	selector: 'tds-dependencies-list',
	templateUrl: '../tds/web-app/app-js/modules/dependencies/components/list/dependencies-list.component.html'
})
export class DependenciesListComponent implements OnInit {
	@ViewChild('tdsBulkChangeButton') tdsBulkChangeButton: BulkChangeButtonComponent;
	protected bulkChangeType: BulkChangeType = BulkChangeType.Dependencies;
	protected gridStateSubject: BehaviorSubject<State>;
	protected assets: any[];
	protected skip = 0;
	protected pageSize = GRID_DEFAULT_PAGE_SIZE;
	protected maxOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	protected dependenciesColumnModel: DependenciesColumnModel;
	public gridData: GridDataResult;
	protected state: State;
	protected idFieldName = 'id';
	public bulkItems: number[] = [];

	constructor(
		private route: ActivatedRoute,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private prompt: UIPromptService,
		private bulkCheckboxService: BulkCheckboxService,
		private dependenciesService: DependenciesService
	) {
		this.gridData = { data: [], total: 0 };
		this.state = this.getInitialGridState();
		this.bulkCheckboxService.setCurrentState(CheckboxStates.unchecked);
		this.bulkCheckboxService.setIdFieldName(this.idFieldName);
		this.dependenciesColumnModel = new DependenciesColumnModel();
		this.gridStateSubject = new BehaviorSubject(this.getInitialGridState());
	}

	ngOnInit() {
		this.gridStateSubject
			.pipe(
				tap((state: State) => {
					this.state = state;
					this.bulkCheckboxService.setPageSize(this.state.take);
				}),
				mergeMap((state) => this.dependenciesService.getDependencies(state)),
				map((results: DependencyResults) => ({data: results.dependencies, total: results.total}))
			)
			.subscribe((results) => {
				this.gridData = results;
				this.bulkCheckboxService.initializeKeysBulkItems(results.data || []);
			});
	}

	/**
	 * Handle pageChange, filterChange, sortChange events
	 * @param {DataStateChangeEvent} state
	 */
	protected dataStateChange(state: DataStateChangeEvent): void {
		this.gridStateSubject.next(state);
	}

	/**
	 * Get the initial state of the grid
	 * @returns {State} default grid state properties
	 */
	getInitialGridState(): State {
		return {
			sort: [{
				dir: 'asc',
				field: 'name'
			}],
			filter: {
				filters: [],
				logic: 'and'
			},
			take: GRID_DEFAULT_PAGE_SIZE,
			skip: 0
		}
	}


	onEdit(dataItem: any) {
	}

	onDelete(dataItem: any) {
	}

	clearValue(dataItem: any) {
	}

	onFilter(dataItem: any) {
	}

	protected filterChange(filter: CompositeFilterDescriptor): void {
		this.state.filter = filter;
		// this.gridData = process(this.assets, this.state);
	}

	protected sortChange(sort): void {
		this.state.sort = sort;
		// this.gridData = process(this.assets, this.state);
	}

	/**
	 * Handle pageChange, filterChange, sortChange events
	 * @param {DataStateChangeEvent} state
	 */
	protected dataStateChange(state: DataStateChangeEvent): void {
		this.gridStateSubject.next(state);
	}

	/**
	 * Get the initial state of the grid
	 * @returns {State} default grid state properties
	 */
	getInitialGridState(): State {
		return {
			sort: [{
				dir: 'asc',
				field: 'name'
			}],
			filter: {
				filters: [],
				logic: 'and'
			},
			take: GRID_DEFAULT_PAGE_SIZE,
			skip: 0
		}
	}

	/**
	 * Get the current selected items counter
	 * @returns {number}
	 */
	getSelectedItemsCount(): number {
		const allCounter = (this.gridData && this.gridData.total) || 0;
		return this.bulkCheckboxService.getSelectedItemsCount(allCounter)
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
			// this.onReload();
		}
	}

	onClickBulkButton(): void {
		this.bulkCheckboxService
			.getBulkSelectedItems({skip: 0, take: this.gridData.total},
				this.dependenciesService.getDependencies.bind(this.dependenciesService))
			.subscribe((results: any) => {
				console.log('The results are');
				console.log(results);
				this.bulkItems = [...results.selectedAssetsIds];
				this.tdsBulkChangeButton.bulkData({bulkItems: this.bulkItems, assetsSelectedForBulk: [...results.selectedAssets]});

			}, (err) => console.log('Error', err));
	}

	hasSelectedItems(): boolean {
		return this.bulkCheckboxService.hasSelectedItems();
	}

}
