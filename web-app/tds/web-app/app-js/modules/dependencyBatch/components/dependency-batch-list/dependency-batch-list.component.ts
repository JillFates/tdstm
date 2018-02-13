import {Component} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {DependencyBatchColumnsModel} from '../../model/import-batch.model';
import {CellClickEvent, SelectableSettings} from '@progress/kendo-angular-grid';
import {DataGridOperationsHelper} from './data-grid-operations.helper';
import {Permission} from '../../../../shared/model/permission.model';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {DependencyBatchDetailDialogComponent} from '../dependency-batch-detail-dialog/dependency-batch-detail-dialog.component';

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
		private dialogService: UIDialogService,
		private dependencyBatchService: DependencyBatchService,
		private permissionService: PermissionService,
		private notifierService: NotifierService) {
		this.onLoad();
	}

	private onLoad(): void {
		this.columnsModel = new DependencyBatchColumnsModel();
		if ( !this.canRunActions() ) {
			this.columnsModel.columns.splice(0, 1);
		}
		this.dependencyBatchService.getImportBatches().subscribe(result => {
			this.dataGridOperationsHelper = new DataGridOperationsHelper(result, this.initialSort, this.selectableSettings, this.checkboxSelectionConfig);
		});
	}

	private loadBatchList(): void {
		this.dependencyBatchService.getImportBatches().subscribe(result => {
			this.dataGridOperationsHelper.reloadData(result);
		});
	}

	private openBatchDetail(cellClick: CellClickEvent): void {
		this.dialogService.open(DependencyBatchDetailDialogComponent, []).then(result => {
			// silence is golden
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	private onViewArchived(): void {
		if (this.viewArchived) {
			this.loadArchivedBatchList();
		} else {
			this.loadBatchList();
		}
	}

	private loadArchivedBatchList(): void {
		this.dependencyBatchService.getArchivedBatchList().subscribe( result => {
			this.dataGridOperationsHelper.reloadData(result);
		});
	}

	private onPlayButton(item: any): void {
		this.dependencyBatchService.startBatch(item.id).subscribe( (result) => {
			if (result.status === 'success') {
				let batchFound = this.dataGridOperationsHelper.resultSet.find( batch => {
					return batch.id === item.id;
				});
				batchFound.status = 'Processing';
			} else {
				this.notifierService.broadcast({
					name: AlertType.DANGER,
					message: result.error
				});
				console.log(result.error);
			}
		});
	}

	private onStopButton(item: any): void {
		this.dependencyBatchService.stopBatch(item.id).subscribe( (result) => {
			if (result.status === 'success') {
				let batchFound = this.dataGridOperationsHelper.resultSet.find( batch => {
					return batch.id === item.id;
				});
				batchFound.status = 'Pending';
			}
		});
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