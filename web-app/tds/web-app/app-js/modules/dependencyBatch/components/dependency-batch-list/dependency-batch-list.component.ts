import {Component} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {BatchStatus, DependencyBatchColumnsModel, ImportBatchModel} from '../../model/import-batch.model';
import {CellClickEvent, SelectableSettings} from '@progress/kendo-angular-grid';
import {Permission} from '../../../../shared/model/permission.model';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {DependencyBatchDetailDialogComponent} from '../dependency-batch-detail-dialog/dependency-batch-detail-dialog.component';
import {
	DIALOG_SIZE, PROMPT_DEFAULT_TITLE_KEY, PROMPT_DELETE_ITEM_CONFIRMATION,
	PROMPT_DELETE_ITEMS_CONFIRMATION
} from '../../../../shared/model/constants';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {GridColumnModel} from '../../../../shared/model/data-list-grid.model';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {EnumModel} from '../../../../shared/model/enum.model';

@Component({
	selector: 'dependency-batch-list',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-list/dependency-batch-list.component.html',
	providers: [TranslatePipe]
})
export class DependencyBatchListComponent {

	private columnsModel: DependencyBatchColumnsModel;
	private selectableSettings: SelectableSettings = { mode: 'single', checkboxOnly: false};
	private dataGridOperationsHelper: DataGridOperationsHelper;
	private initialSort: any = [{
		dir: 'desc',
		field: 'dateCreated'
	}];
	private checkboxSelectionConfig = {
		useColumn: 'id'
	};
	private viewArchived = false;
	private batchStatusLooper: any;
	private readonly PROGRESS_MAX_TRIES = 10;
	private readonly PROGRESS_CHECK_INTERVAL = 10 * 1000;
	private readonly ARCHIVE_ITEM_CONFIRMATION = 'IMPORT_BATCH.LIST.ARCHIVE_ITEM_CONFIRMATION';
	private readonly ARCHIVE_ITEMS_CONFIRMATION = 'IMPORT_BATCH.LIST.ARCHIVE_ITEMS_CONFIRMATION';
	private readonly UNARCHIVE_ITEM_CONFIRMATION = 'IMPORT_BATCH.LIST.UNARCHIVE_ITEM_CONFIRMATION';
	private readonly UNARCHIVE_ITEMS_CONFIRMATION = 'IMPORT_BATCH.LIST.UNARCHIVE_ITEMS_CONFIRMATION';

	constructor(
		private dialogService: UIDialogService,
		private dependencyBatchService: DependencyBatchService,
		private permissionService: PermissionService,
		private promptService: UIPromptService,
		private translatePipe: TranslatePipe,
		private notifierService: NotifierService,
		private userPreferenceService: PreferenceService) {
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
		// prevent open detail on column 0
		if (cellClick.columnIndex === 0 ) {
			return;
		}
		this.dataGridOperationsHelper.selectCell(cellClick); // mark row as selected
		this.dialogService.open(DependencyBatchDetailDialogComponent, [
			{ provide: ImportBatchModel, useValue: (cellClick as any).dataItem}
		], DIALOG_SIZE.XXL, true).then(result => {
			// silence is golden
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * Confirmation to proceed with the archive of the batches.
	 */
	private confirmArchive(): void {
		const ids = this.dataGridOperationsHelper.getCheckboxSelectedItems().map( item => parseInt(item, 10));
		this.promptService.open(
			this.translatePipe.transform(PROMPT_DEFAULT_TITLE_KEY),
			this.translatePipe.transform(ids.length === 1 ? this.ARCHIVE_ITEM_CONFIRMATION : this.ARCHIVE_ITEMS_CONFIRMATION),
			'Confirm', 'Cancel').then(result => {
			if (result) {
				this.onArchiveBatch();
			}
		}, (reason: any) => console.log('confirm rejected', reason));
	}

	/**
	 * On Archive batch button click.
	 */
	private onArchiveBatch(): void {
		const ids = this.dataGridOperationsHelper.getCheckboxSelectedItemsAsNumbers();
		this.dependencyBatchService.archiveImportBatches(ids).subscribe( (result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.reloadBatchList();
					this.dataGridOperationsHelper.unSelectAllCheckboxes();
				} else {
					this.handleError(result.errors ? result.errors[0] : null);
				}
			},
			(err) => this.handleError(err)
		);
	}

	/**
	 * Confirmation to proceed with the archive of the batches.
	 */
	private confirmUnarchive(): void {
		const ids = this.dataGridOperationsHelper.getCheckboxSelectedItems().map( item => parseInt(item, 10));
		this.promptService.open(
			this.translatePipe.transform(PROMPT_DEFAULT_TITLE_KEY),
			this.translatePipe.transform(ids.length === 1 ? this.UNARCHIVE_ITEM_CONFIRMATION : this.UNARCHIVE_ITEMS_CONFIRMATION),
			'Confirm', 'Cancel').then(result => {
			if (result) {
				this.onUnarchiveBatch();
			}
		}, (reason: any) => console.log('confirm rejected', reason));
	}

	/**
	 * On UnArchive batch button click.
	 */
	private onUnarchiveBatch(): void {
		const ids = this.dataGridOperationsHelper.getCheckboxSelectedItemsAsNumbers();
		this.dependencyBatchService.unArchiveImportBatches(ids).subscribe( (result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.loadArchivedBatchList();
					this.dataGridOperationsHelper.unSelectAllCheckboxes();
				} else {
					this.handleError(result.errors ? result.errors[0] : null);
				}
			},
			(err) => this.handleError(err)
		);
	}

	/**
	 * Confirmation to proceed with the delete of the batches.
	 */
	private confirmDelete(): void {
		const ids = this.dataGridOperationsHelper.getCheckboxSelectedItems().map( item => parseInt(item, 10));
		this.promptService.open(
			this.translatePipe.transform(PROMPT_DEFAULT_TITLE_KEY),
			this.translatePipe.transform(ids.length === 1 ? PROMPT_DELETE_ITEM_CONFIRMATION : PROMPT_DELETE_ITEMS_CONFIRMATION),
			'Confirm', 'Cancel').then(result => {
			if (result) {
				this.onDeleteBatch();
			}
		}, (reason: any) => console.log('confirm rejected', reason));
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
						this.dataGridOperationsHelper.unSelectAllCheckboxes();
					} else {
						this.reloadBatchList();
						this.dataGridOperationsHelper.unSelectAllCheckboxes();
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
	private onPlayButton(item: ImportBatchModel): void {
		const ids = [item.id];
		this.dependencyBatchService.queueImportBatches(ids).subscribe( (result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS && result.data.QUEUE) {
					item.status.code = BatchStatus.QUEUED;
					item.status.label = 'Queued';
				} else {
					this.reloadBatchList();
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
	private clearBatchStatusLooper(): void {
		if (this.batchStatusLooper) {
			clearInterval(this.batchStatusLooper);
		}
	}

	/**
	 * Creates an interval loop to retreive batch current progress.
	 */
	private setBatchStatusLooper(): void {
		let runningBatches = this.dataGridOperationsHelper.resultSet.filter( (item: ImportBatchModel) => {
			return item.status.code === BatchStatus.RUNNING;
		});
		this.getBatchesCurrentProgress(runningBatches);
		this.batchStatusLooper = setInterval(() => {
			this.getBatchesCurrentProgress(runningBatches);
		}, this.PROGRESS_CHECK_INTERVAL); // every 10 seconds
	}

	/**
	 * Gets from API batch current progress.
	 */
	private getBatchesCurrentProgress(runningBatches: Array<ImportBatchModel>): void {
		for (let batch of runningBatches ) {
			this.dependencyBatchService.getImportBatchProgress(batch.id).subscribe((response: ApiResponseModel) => {
				if (response.status === ApiResponseModel.API_SUCCESS) {
					batch.currentProgress =  response.data.progress ? response.data.progress : 0;
					const lastUpdated = (response.data.lastUpdated as Date);
					batch.stalledCounter = batch.lastUpdated === lastUpdated ? batch.stalledCounter += 1 : 0 ;

					// If batch doesn't update after N times, then move to STALLED and remove it from the looper.
					if (batch.stalledCounter >= this.PROGRESS_MAX_TRIES) {
						batch.status.code = BatchStatus.STALLED;
						batch.status.label = 'Stalled';
						this.removeBatchFromLoop(batch, runningBatches);
					} else if (batch.currentProgress >= 100) {
						batch.status = response.data.status as EnumModel;
						this.removeBatchFromLoop(batch, runningBatches);
					} else {
						batch.lastUpdated =  response.data.lastUpdated as Date;
					}
				} else {
					this.handleError(response.errors[0] ? response.errors[0] : 'error on get batch progress');
				}
			}, error => {
				this.clearBatchStatusLooper();
				this.handleError(error);
			});
		}
		console.log(runningBatches);
	}

	/**
	 * Removes a batch from Running Loop List.
	 * @param {ImportBatchModel} batch
	 * @param {Array} runningBatches
	 */
	private removeBatchFromLoop(batch: ImportBatchModel, runningBatches: Array<ImportBatchModel>): void {
		const filterIndex = runningBatches.findIndex((item: ImportBatchModel) => item.id === batch.id);
		runningBatches.splice(filterIndex, 1);
	}
}