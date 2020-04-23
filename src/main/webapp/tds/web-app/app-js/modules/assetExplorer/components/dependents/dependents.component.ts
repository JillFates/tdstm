// Angular
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
// Model
import {DependentType, SupportDependentsColumnsModel} from './model/support-dependents-columns.model';
// Service
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {ViewColumn} from '../../model/view-spec.model';
import {COLUMN_MIN_WIDTH} from 'tds-component-library';

@Component({
	selector: `dependents-component`,
	templateUrl: 'dependents.component.html',
	styles: [],
})
export class DependentsComponent implements OnInit {
	@Input('dependencies') dependencies: any;
	@Output() onAssetShow: EventEmitter<any> = new EventEmitter<any>();
	@Output() onDependencyShow: EventEmitter<any> = new EventEmitter<any>();

	public dependentType: any = DependentType;
	public gridSupportsData: DataGridOperationsHelper;
	public gridDependenciesData: DataGridOperationsHelper;
	public showFilterDep = false;
	public showFilterSup = false;

	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;

	private supportOnColumnModel: SupportDependentsColumnsModel;
	private dependentOnColumnModel: SupportDependentsColumnsModel;

	protected showSupportFilter = false;

	ngOnInit(): void {
		this.gridSupportsData = new DataGridOperationsHelper(this.dependencies.supports, [{
			dir: 'asc',
			field: 'name'
		}], {mode: 'single', checkboxOnly: false}, {useColumn: 'id'}, 25);

		this.gridDependenciesData = new DataGridOperationsHelper(this.dependencies.dependents, [{
			dir: 'asc',
			field: 'name'
		}], {mode: 'single', checkboxOnly: false}, {useColumn: 'id'}, 25);

		this.supportOnColumnModel = new SupportDependentsColumnsModel();
		this.dependentOnColumnModel = new SupportDependentsColumnsModel();
	}

	/**
	 * Clears the filter on supports
	 */
	public showFilterSupports(): void {
		if (this.showFilterSup) {
			this.showFilterSup = false;
			this.gridSupportsData.clearAllFilters(this.supportOnColumnModel.columns);
		} else {
			this.showFilterSup = true;
		}
	}

	/**
	 * Clears the Dependent filter
	 */
	showFilterDependents(): void {
		if (this.showFilterDep) {
			this.showFilterDep = false;
			this.gridDependenciesData.clearAllFilters(this.dependentOnColumnModel.columns);
		} else {
			this.showFilterDep = true;
		}
	}

	/**
	 * Emit the event to Show the Asset upto the Parent Component
	 */
	public showAssetDetailView(assetClass: string, id: number): void {
		this.onAssetShow.emit({assetClass, id});
	}

	/**
	 * Emit the event to Show the Dependency Tree Parent Component
	 */
	public showDependencyView(type: string, assetId: number, dependencyAsset: number, rowId = ''): void {
		this.onDependencyShow.emit({type, assetId, dependencyAsset, rowId})
	}

	/**
	 * Calculate the class for the Move Bundle
	 * @returns {string}
	 */
	public getMoveBundleClass(dataItem: any): string {
		if (dataItem.moveBundle.id !== this.dependencies.asset && this.dependencies.asset.moveBundleId && dataItem.status === 'Validated') {
			return 'bundle-dep-questioned';
		} else {
			return 'cell-template dep-' + dataItem.status;
		}
	}

	// Support Filtering

	/**
	 * Set the filter value to the new search string and start off the filtering process
	 * @param {string} search - Current search value
	 * @param {ViewColumn} column - Column of the datagrid which threw the event
	 */
	public setFilter(search: string, column: ViewColumn): void {
		column.filter = search;
	}

	public hasSupportFilterApplied(): boolean {
		return this.supportOnColumnModel.columns.filter((c: any) => c.filter).length > 0;
	}

	public onClearSupportFilters(): void {
		this.supportOnColumnModel.columns.forEach((c: any) => {
			c.filter = '';
		});
	}

	public onClearDependentFilters(): void {
		this.dependentOnColumnModel.columns.forEach((c: any) => {
			c.filter = '';
		});
	}
}
