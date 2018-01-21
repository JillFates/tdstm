import {Component} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {DependencyBatchColumnsModel, DependencyBatchModel} from '../../model/dependency-batch.model';
import {CompositeFilterDescriptor, process, State} from '@progress/kendo-data-query';
import {CellClickEvent, GridDataResult, RowArgs, SelectableSettings} from '@progress/kendo-angular-grid';
import {DataGridOperationsHelper} from './data-grid-operations.helper';

@Component({
	selector: 'dependency-batch-list',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-list/dependency-batch-list.component.html',
})
export class DependencyBatchListComponent {

	private columnsModel: DependencyBatchColumnsModel;
	private selectableSettings: SelectableSettings = { mode: 'single', checkboxOnly: false};
	private dataGridOperationsHelper: DataGridOperationsHelper;
	private initialSort: any = [{
		dir: 'desc',
		field: 'importedDate'
	}];

	constructor(
		private dependencyBatchService: DependencyBatchService,
		private permissionService: PermissionService) {
		this.onLoad();
	}

	private onLoad(): void {
		this.columnsModel = new DependencyBatchColumnsModel();
		this.dependencyBatchService.getBatchList().subscribe( result => {
			this.dataGridOperationsHelper = new DataGridOperationsHelper(result, this.initialSort, this.selectableSettings);
		});
	}

	private openBatchDetail(cellClick: CellClickEvent): void {
		console.log( (cellClick as any).dataItem );
	}
}