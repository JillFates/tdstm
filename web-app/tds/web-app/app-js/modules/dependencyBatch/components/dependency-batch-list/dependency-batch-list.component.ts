import {Component} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {DependencyBatchColumnsModel} from '../../model/dependency-batch.model';
import {CellClickEvent, SelectableSettings} from '@progress/kendo-angular-grid';
import {DataGridOperationsHelper} from './data-grid-operations.helper';
import {Permission} from '../../../../shared/model/permission.model';

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
	private checkboxSelectionConfig = {
		useColumn: 'id'
	};
	private viewArchived = false;

	constructor(
		private dependencyBatchService: DependencyBatchService,
		private permissionService: PermissionService) {
		this.onLoad();
	}

	private onLoad(): void {
		this.columnsModel = new DependencyBatchColumnsModel();
		if ( !this.canRunActions() ) {
			this.columnsModel.columns.splice(0, 1);
		}
		this.dependencyBatchService.getBatchList().subscribe( result => {
			this.dataGridOperationsHelper = new DataGridOperationsHelper(result, this.initialSort, this.selectableSettings, this.checkboxSelectionConfig);
		});
	}

	private openBatchDetail(cellClick: CellClickEvent): void {
		console.log( (cellClick as any).dataItem );
	}

	private onViewArchived(): void {
		if (this.viewArchived) {
			console.log('show ARCHIVED batches, reload');
		} else {
			console.log('show UN-ARCHIVED batches, reload');
		}
	}

	private canRunActions(): boolean {
		return this.permissionService.hasPermission(Permission.DataTransferBatchProcess);
	}

	private canBulkDelete(): boolean {
		return this.permissionService.hasPermission(Permission.DataTransferBatchDelete);
	}

	private canBulkArchive(): boolean {
		return this.permissionService.hasPermission(Permission.DataTransferBatchProcess);
	}
}