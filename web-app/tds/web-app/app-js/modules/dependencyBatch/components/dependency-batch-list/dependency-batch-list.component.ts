import {Component} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {DependencyBatchColumnsModel, DependencyBatchModel} from '../../model/dependency-batch.model';
import {CompositeFilterDescriptor, process, State} from '@progress/kendo-data-query';
import {GridDataResult, RowArgs} from '@progress/kendo-angular-grid';
import {DataGridOperationsHelper} from './data-grid-operations.helper';

@Component({
	selector: 'dependency-batch-list',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-list/dependency-batch-list.component.html',
})
export class DependencyBatchListComponent {

	private columnsModel: DependencyBatchColumnsModel;
	private selectedRows = [];
	private isRowSelected = (e: RowArgs) => this.selectedRows.indexOf(e.dataItem.id) >= 0;
	// private gridData: GridDataResult;
	// private resultSet: DependencyBatchModel[];
	private gridOperationsHelper: DataGridOperationsHelper;

	constructor(
		private dependencyBatchService: DependencyBatchService,
		private permissionService: PermissionService) {
		this.onLoad();
	}

	private onLoad(): void {
		this.columnsModel = new DependencyBatchColumnsModel();
		let state: State = {
			sort: [{
				dir: 'asc',
				field: 'id'
			}],
			filter: {
				filters: [],
				logic: 'and'
			}
		};
		this.dependencyBatchService.getBatchList().subscribe( result => {
			this.gridOperationsHelper = new DataGridOperationsHelper(result, state);
		});
	}

	protected onFilter(column: any): void {
		this.gridOperationsHelper.onFilter(column);
	}

	protected clearValue(column: any): void {
		this.gridOperationsHelper.clearValue(column);
	}

	protected filterChange(filter: CompositeFilterDescriptor): void {
		this.gridOperationsHelper.filterChange(filter);
	}

	// protected onFilter(column: any): void {
	// 	GridFiltersUtils.filterColumn(column, this.gridStates, this.gridData, this.resultSet);
	// }
}