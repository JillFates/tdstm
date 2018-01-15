import {Component} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {DependencyBatchColumnsModel, DependencyBatchModel} from '../../model/dependency-batch.model';
import {State} from '@progress/kendo-data-query';
import {RowArgs} from '@progress/kendo-angular-grid';

@Component({
	selector: 'dependency-batch-list',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-list/dependency-batch-list.component.html',
})
export class DependencyBatchListComponent {

	private columnsModel: DependencyBatchColumnsModel;
	private gridStates: State = {
		sort: [{
			dir: 'asc',
			field: 'id'
		}],
		filter: {
			filters: [],
			logic: 'and'
		}
	};
	private selectedRows = [];
	private isRowSelected = (e: RowArgs) => this.selectedRows.indexOf(e.dataItem.id) >= 0;
	private batchList: Array<DependencyBatchModel>;

	constructor(
		private dependencyBatchService: DependencyBatchService,
		private permissionService: PermissionService) {
		this.onLoad();
	}

	private onLoad(): void {
		this.batchList = [];
		this.columnsModel = new DependencyBatchColumnsModel();
		this.dependencyBatchService.getBatchList().subscribe( result => {
			this.batchList = result.data;
		});
	}
}