import {Component} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {BatchStatus, DependencyBatchColumnsModel, ImportBatchModel} from '../../model/import-batch.model';
import {CellClickEvent, SelectableSettings} from '@progress/kendo-angular-grid';
import {DataGridOperationsHelper} from './data-grid-operations.helper';
import {Permission} from '../../../../shared/model/permission.model';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {DependencyBatchDetailDialogComponent} from '../dependency-batch-detail-dialog/dependency-batch-detail-dialog.component';
import {Observable} from 'rxjs/Observable';
import {DIALOG_SIZE} from '../../../../shared/model/constants';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {GridColumnModel} from '../../../../shared/model/data-list-grid.model';

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
	private batchStatusLooper: any;

	constructor(
		private dialogService: UIDialogService,
		private dependencyBatchService: DependencyBatchService,
		private permissionService: PermissionService,
		private notifierService: NotifierService) {
		this.onLoad();
	}
	/**
	 * On Page Load.
	 */
	private onLoad(): void {
		this.columnsModel = new DependencyBatchColumnsModel();
		if ( !this.canRunActions() ) {
			this.columnsModel.columns.splice(0, 1);
		}
		this.getUnarchivedBatches().then( batchList => {
			this.dataGridOperationsHelper = new DataGridOperationsHelper(batchList, this.initialSort, this.selectableSettings, this.checkboxSelectionConfig);
			this.setBatchStatusLooper();
		});
	}

	/**
	 * Load all Import Batch Unarchived list
	 */
	private reloadBatchList(): void {
		this.clearBatchStatusLooper();
		this.getUnarchivedBatches().then( batchList => {
			this.dataGridOperationsHelper.reloadData(batchList);
			this.setBatchStatusLooper();
		});
	}

	/**
	 * Get Unarchived Import Batch List.
	 * Calling the endpoint as a Promise for reuse.
	 * @returns {Array<ImportBatchModel>}
	 */
	private getUnarchivedBatches(): Promise<any> {
		let promise = new Promise((resolve, reject) => {
			this.dependencyBatchService.getImportBatches().subscribe( (result) => {
				if (result.status === 'success') {
					let batches: Array<ImportBatchModel> = result.data.filter( item => {
						return !item.archived;
					});
					resolve(batches);
				} else {
					this.handleError(result.errors ? result.errors[0] : null);
					resolve([]);
				}
			}, (err) => {
				this.handleError(err);
				resolve([]);
			});
		});
		return promise;
	}

	/**
	 * Load Archived Batches.
	 */
	private loadArchivedBatchList(): void {
		this.clearBatchStatusLooper();
		this.dependencyBatchService.getImportBatches().subscribe( result => {
			if (result.status === 'success') {
				let batches = result.data.filter( (item: ImportBatchModel) => {
					return item.archived;
				});
				this.dataGridOperationsHelper.reloadData(batches);
				this.setBatchStatusLooper();
			} else {
				this.handleError(result.errors ? result.errors[0] : null);
			}
		});
	}

	/**
	 * Open Dialog Popups to display Batch Import detail.
	 * @param {CellClickEvent} cellClick
	 */
	private openBatchDetail(cellClick: CellClickEvent): void {
		this.dialogService.open(DependencyBatchDetailDialogComponent, [
			{ provide: ImportBatchModel, useValue: (cellClick as any).dataItem}
		], DIALOG_SIZE.XXL, false).then(result => {
			// silence is golden
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * On Archive batch button click.
	 */
	private onArchiveBatch(): void {
		const ids = this.dataGridOperationsHelper.getCheckboxSelectedItemsAsNumbers();
		this.dependencyBatchService.archiveImportBatches(ids).subscribe( (result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.reloadBatchList();
				} else {
					this.handleError(result.errors ? result.errors[0] : null);
				}
			},
			(err) => this.handleError(err)
		);
	}

	/**
	 * On UnArchive batch button click.
	 */
	private onUnArchiveBatch(): void {
		const ids = this.dataGridOperationsHelper.getCheckboxSelectedItemsAsNumbers();
		this.dependencyBatchService.unArchiveImportBatches(ids).subscribe( (result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.loadArchivedBatchList();
				} else {
					this.handleError(result.errors ? result.errors[0] : null);
				}
			},
			(err) => this.handleError(err)
		);
	}

	/**
	 * On Delete batch button click.
	 */
	private onDeleteBatch(): void {
		const ids = this.dataGridOperationsHelper.getCheckboxSelectedItems().map( item => parseInt(item, 10));
		this.dependencyBatchService.deleteImportBatches(ids).subscribe( (result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					if (this.viewArchived) {
						this.loadArchivedBatchList();
					} else {
						this.reloadBatchList();
					}
				} else {
					this.handleError(result.errors ? result.errors[0] : null);
				}
			},
			(err) => this.handleError(err)
		);
	}

	/**
	 * Handles API error results. Displays messages.
	 * @param {string} error
	 */
	private handleError(error: string): void {
		this.notifierService.broadcast({
			name: AlertType.DANGER,
			message: error
		});
	}

	/**
	 * On View Archived checkbox clicked, toggle load archived batch list
	 */
	private onToggleViewArchived(): void {
		this.dataGridOperationsHelper.unSelectAllCheckboxes();
		if (this.viewArchived) {
			this.loadArchivedBatchList();
		} else {
			this.reloadBatchList();
		}
	}

	/**
	 * On Play action button clicked, start import batch.
	 * @param item
	 */
	private onPlayButton(item: any): void {
		const ids = [item.id];
		this.dependencyBatchService.queueImportBatches(ids).subscribe( (result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.reloadBatchList();
				} else {
					this.handleError(result.errors ? result.errors[0] : null);
				}
			},
			(err) => this.handleError(err)
		);
	}

	/**
	 * On Eject action button clicked, start import batch.
	 * @param item
	 */
	private onEjectButton(item: any): void {
		const ids = [item.id];
		this.dependencyBatchService.ejectImportBatches(ids).subscribe( (result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.reloadBatchList();
				} else {
					this.handleError(result.errors ? result.errors[0] : null);
				}
			},
			(err) => this.handleError(err)
		);
	}

	/**
	 * On Stop action button clicked, stop import batch.
	 * @param item
	 */
	private onStopButton(batch: ImportBatchModel): void {
		const ids = [batch.id];
		this.dependencyBatchService.stopImportBatch(ids).subscribe( (result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				this.reloadBatchList();
			} else {
				this.handleError(result.errors[0] ? result.errors[0] : 'Error on stop import batch endpoint.');
			}
		}, error => this.handleError(error));
	}

	/**
	 * Determines if has DataTransferBatchProcess permission to run actions.
	 * @returns {boolean}
	 */
	private canRunActions(): boolean {
		return this.permissionService.hasPermission(Permission.DataTransferBatchProcess);
	}

	/**
	 * Determines if has DataTransferBatchDelete permission to bulk delete batches.
	 * @returns {boolean}
	 */
	private canBulkDelete(): boolean {
		return this.permissionService.hasPermission(Permission.DataTransferBatchDelete);
	}

	/**
	 * Determines if has DataTransferBatchProcess permission to bulk archive batches.
	 * @returns {boolean}
	 */
	private canBulkArchive(): boolean {
		return this.permissionService.hasPermission(Permission.DataTransferBatchProcess);
	}

	/**
	 * Add modifications to the params send to filter if needed.
	 * @param {GridColumnModel} column
	 */
	private preProcessFilter(column: GridColumnModel): void {
		if (column.property === 'status') {
			let columnCopy = {...column};
			columnCopy.property = 'status.label';
			this.dataGridOperationsHelper.onFilter(columnCopy);
		} else {
			this.dataGridOperationsHelper.onFilter(column);
		}
	}

	/**
	 * Clears out the Batch Status Interval if currently running.
	 */
	clearBatchStatusLooper(): void {
		if (this.batchStatusLooper) {
			clearInterval(this.batchStatusLooper);
		}
	}

	/**
	 * Creates an interval loop to retreive batch current progress.
	 */
	private setBatchStatusLooper(): void {
		const runningBatches = this.dataGridOperationsHelper.resultSet.filter( (item: ImportBatchModel) => {
			return item.status.code === BatchStatus.RUNNING;
		});
		this.getBatchesCurrentProgress(runningBatches);
		this.batchStatusLooper = setInterval(() => {
			this.getBatchesCurrentProgress(runningBatches);
		}, 5000); // every 5 seconds
	}

	/**
	 * Gets from API batch current progress.
	 */
	private getBatchesCurrentProgress(runningBatches: Array<ImportBatchModel>): void {
		for (let batch of runningBatches ) {
			this.dependencyBatchService.getImportBatchProgress(batch.id).subscribe((response: ApiResponseModel) => {
				if (response.status === ApiResponseModel.API_SUCCESS) {
					batch.currentProgress =  response.data.progress ? response.data.progress : 0;
					if (batch.currentProgress === 100) {
						if (this.viewArchived) {
							this.loadArchivedBatchList();
						} else {
							this.reloadBatchList();
						}
					}
				} else {
					this.handleError(response.errors[0] ? response.errors[0] : 'error on get batch progress');
				}
			}, error => this.handleError(error));
		}
		console.log(runningBatches);
	}
}