import {AfterViewInit, Component, OnDestroy, OnInit} from '@angular/core';
import {ImportBatchService} from '../../service/import-batch.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {BatchStatus, ImportBatchColumnsModel, ImportBatchModel} from '../../model/import-batch.model';
import {CellClickEvent, PageChangeEvent, SelectableSettings} from '@progress/kendo-angular-grid';
import {Permission} from '../../../../shared/model/permission.model';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {
	DIALOG_SIZE, PROMPT_DEFAULT_TITLE_KEY, PROMPT_DELETE_ITEM_CONFIRMATION,
	PROMPT_DELETE_ITEMS_CONFIRMATION
} from '../../../../shared/model/constants';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {GridColumnModel} from '../../../../shared/model/data-list-grid.model';
import {IMPORT_BATCH_PREFERENCES, PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import {GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {EnumModel} from '../../../../shared/model/enum.model';
import {ImportBatchDetailDialogComponent} from '../detail/import-batch-detail-dialog.component';
import {ActivatedRoute} from '@angular/router';

@Component({
	selector: 'import-batch-list',
	templateUrl: 'import-batch-list.component.html',
	providers: [TranslatePipe]
})
export class ImportBatchListComponent implements OnDestroy {

	public userTimeZone: string;

	protected BatchStatus = BatchStatus;
	protected columnsModel: ImportBatchColumnsModel;
	protected importBatchPreferences = {};
	private selectableSettings: SelectableSettings = { mode: 'single', checkboxOnly: false};
	public dataGridOperationsHelper: DataGridOperationsHelper;
	private initialSort: any = [{
		dir: 'desc',
		field: 'dateCreated'
	}];
	private checkboxSelectionConfig = {
		useColumn: 'id'
	};
	private viewArchived = false;
	private batchRunningLoop: any;
	private batchQueuedLoop: any;
	private readonly PROGRESS_MAX_TRIES = 10;
	private readonly PROGRESS_CHECK_INTERVAL = 3 * 1000;
	private readonly STOP_BATCH_CONFIRMATION = 'IMPORT_BATCH.LIST.STOP_BATCH_CONFIRMATION';
	private readonly ARCHIVE_ITEM_CONFIRMATION = 'IMPORT_BATCH.LIST.ARCHIVE_ITEM_CONFIRMATION';
	private readonly ARCHIVE_ITEMS_CONFIRMATION = 'IMPORT_BATCH.LIST.ARCHIVE_ITEMS_CONFIRMATION';
	private readonly UNARCHIVE_ITEM_CONFIRMATION = 'IMPORT_BATCH.LIST.UNARCHIVE_ITEM_CONFIRMATION';
	private readonly UNARCHIVE_ITEMS_CONFIRMATION = 'IMPORT_BATCH.LIST.UNARCHIVE_ITEMS_CONFIRMATION';
	private runningBatches: Array<ImportBatchModel> = [];
	private queuedBatches: Array<ImportBatchModel> = [];

	constructor(
		private dialogService: UIDialogService,
		private importBatchService: ImportBatchService,
		private permissionService: PermissionService,
		private promptService: UIPromptService,
		private translatePipe: TranslatePipe,
		private notifierService: NotifierService,
		private userPreferenceService: PreferenceService,
		private route: ActivatedRoute) {
			this.onLoad();
	}

	/**
	 * On Page Load.
	 */
	private onLoad(): void {
		// Fetch the user preferences for their TimeZone and DateFormat
		this.userTimeZone = this.userPreferenceService.getUserTimeZone();

		this.columnsModel = new ImportBatchColumnsModel();
		if ( !this.canRunActions() ) {
			this.columnsModel.columns.splice(0, 1);
		}
		this.userPreferenceService.getSinglePreference(PREFERENCES_LIST.IMPORT_BATCH_PREFERENCES).subscribe( res => {
			this.getUnarchivedBatches().then( batchList => {
				let pageSize;
				if (res) {
					this.importBatchPreferences = JSON.parse(res);
					pageSize = parseInt(this.importBatchPreferences[IMPORT_BATCH_PREFERENCES.LIST_SIZE], 0);
				} else {
					pageSize = GRID_DEFAULT_PAGE_SIZE;
				}
				this.dataGridOperationsHelper = new DataGridOperationsHelper(batchList, this.initialSort, this.selectableSettings, this.checkboxSelectionConfig, pageSize);
				this.preSelectBatch();
				this.setRunningLoop();
				this.setQueuedLoop();
			});
		});
	}

	/**
	 * Checks if batchId is given and should be open.
	 */
	private preSelectBatch(): void {
		this.route.params.subscribe(params => {
			const batchId = params['id'] ? parseInt(params['id'], 0) : null;
			const match = !batchId ? null : this.dataGridOperationsHelper.resultSet.find( item => item.id === batchId);
			if (batchId && match) {
				let cellClickEvent = { dataItem: match };
				this.openBatchDetail(cellClickEvent);
			}
		});
	}

	/**
	 * Used in template to forward click events from the element to the target parentNode
	 * @param event: any
	 */
	public onClickTemplate(event: any): void {
		if (event.target && event.target.parentNode) {
			event.target.parentNode.click();
		}
	}

	/**
	 * Load all Import Batch Unarchived list
	 */
	private reloadBatchList(): void {
		this.getUnarchivedBatches().then( batchList => {
			this.dataGridOperationsHelper.reloadData(batchList);
			this.clearLoopsLists();
		});
	}

	/**
	 * Reloads the current batch record.
	 * Stops looper and restarts it.
	 * @param {ImportBatchModel} batchRecord
	 */
	private reloadImportBatch(batch: ImportBatchModel) {
		this.importBatchService.getImportBatch(batch.id).subscribe( (response: ApiResponseModel) => {
				if (response.status === ApiResponseModel.API_SUCCESS) {
					Object.assign(batch, response.data);
				}
		});
	}

	/**
	 * Get Unarchived Import Batch List.
	 * Calling the endpoint as a Promise for reuse.
	 * @returns {Array<ImportBatchModel>}
	 */
	private getUnarchivedBatches(): Promise<any> {
		let promise = new Promise((resolve, reject) => {
			this.importBatchService.getImportBatches().subscribe( (result) => {
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
		this.importBatchService.getImportBatches().subscribe( result => {
			if (result.status === 'success') {
				let batches = result.data.filter( (item: ImportBatchModel) => {
					return item.archived;
				});
				this.dataGridOperationsHelper.reloadData(batches);
				this.clearLoopsLists();
			} else {
				this.handleError(result.errors ? result.errors[0] : null);
			}
		});
	}

	/**
	 * Open Dialog Popups to display Batch Import detail.
	 * @param {CellClickEvent} cellClick
	 */
	private openBatchDetail(cellClick: any): void {
		// prevent open detail on column 0
		let selectedBatch: ImportBatchModel = cellClick.dataItem;
		if (cellClick.columnIndex === 0 ) {
			return;
		}
		this.dataGridOperationsHelper.selectCell(cellClick); // mark row as selected
		this.dialogService.open(ImportBatchDetailDialogComponent, [
			{ provide: ImportBatchModel, useValue: selectedBatch}
		], DIALOG_SIZE.XXL).then(result => {
			if (result) {
				this.reloadImportBatch(selectedBatch);
			}
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
		this.importBatchService.archiveImportBatches(ids).subscribe( (result: ApiResponseModel) => {
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
		this.importBatchService.unArchiveImportBatches(ids).subscribe( (result: ApiResponseModel) => {
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
		this.importBatchService.deleteImportBatches(ids).subscribe( (result: ApiResponseModel) => {
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
	 * @param batch
	 */
	private onPlayButton(batch: ImportBatchModel): void {
		const ids = [batch.id];
		this.importBatchService.queueImportBatches(ids).subscribe( (response: ApiResponseModel) => {
				if (response.status === ApiResponseModel.API_SUCCESS && response.data.QUEUE === 1) {
					batch.status.code = BatchStatus.QUEUED.toString();
					batch.status.label = 'Queued';
					this.addToQueuedBatchesLoop(batch);
				} else {
					this.reloadBatchList();
					this.handleError(response.errors ? response.errors[0] : null);
				}
			},
			(err) => this.handleError(err)
		);
	}

	/**
	 * On Eject action button clicked, start import batch.
	 * @param batch
	 */
	private onEjectButton(batch: ImportBatchModel): void {
		const ids = [batch.id];
		this.importBatchService.ejectImportBatches(ids).subscribe( (result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					batch.status.code = BatchStatus.PENDING.toString();
					batch.status.label = 'Pending';
					this.removeBatchFromQueuedLoop(batch);
				} else {
					this.handleError(result.errors ? result.errors[0] : null);
				}
			},
			(err) => this.handleError(err)
		);
	}

	/**
	 * On Stop action button clicked, confirm then stop import batch.
	 * @param item
	 */
	private onStopButton(batch: ImportBatchModel): void {
		this.confirmStopAction(batch);
	}

	/**
	 * Confirmation dialog pops up when stop button is clicked.
	 */
	private confirmStopAction(batch: ImportBatchModel): void {
		this.promptService.open(
			this.translatePipe.transform(PROMPT_DEFAULT_TITLE_KEY),
			this.translatePipe.transform(this.STOP_BATCH_CONFIRMATION),
			'Confirm', 'Cancel').then(result => {
			if (result) {
				this.stopBatch(batch);
			}
		}, /* on rejected do nothing */);
	}

	/**
	 * Stop batch calling the endpoint.
	 */
	private stopBatch(batch: ImportBatchModel): void {
		const ids = [batch.id];
		this.importBatchService.stopImportBatch(ids).subscribe( (result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				this.removeBatchFromRunningLoop(batch);
				this.reloadImportBatch(batch);
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
	 * Initializes the loop for batches on RUNNING state.
	 */
	private setRunningLoop(): void {
		this.runningBatches = this.dataGridOperationsHelper.resultSet.filter( (item: ImportBatchModel) => {
			return item.status.code === BatchStatus.RUNNING.toString();
		});
		this.runningLoop();
	}

	/**
	 * Running Loop, called recursively to check if there's any batch on RUNNING state and checking it's progress.
	 * This implementation waits for the first API call to finish in order to apply the time interval and do the next
	 * check. (instead of queuing the API calls asynchronously).
	 */
	private runningLoop(): void {
		// console.log('Running batches: ', this.runningBatches.length);
		if (this.runningBatches.length === 0) {
			this.batchRunningLoop = setTimeout(() => {
				this.setRunningLoop();
			}, this.PROGRESS_CHECK_INTERVAL);
		} else {
			for (let batch of this.runningBatches) {
				this.importBatchService.getImportBatchProgress(batch.id).subscribe((response: ApiResponseModel) => {
					if (response.status === ApiResponseModel.API_SUCCESS) {
						batch.currentProgress =  response.data.progress ? response.data.progress : 0;
						const lastUpdated = (response.data.lastUpdated as Date);
						batch.stalledCounter = batch.lastUpdated === lastUpdated ? batch.stalledCounter += 1 : 0 ;
						// If batch doesn't update after N times, then move to STALLED and remove it from the looper.
						if (batch.stalledCounter >= this.PROGRESS_MAX_TRIES) {
							batch.status.code = BatchStatus.STALLED.toString();
							batch.status.label = 'Stalled';
							this.removeBatchFromRunningLoop(batch);
						} else if (batch.currentProgress >= 100) {
							batch.status = response.data.status as EnumModel;
							batch.currentProgress = 0;
							this.removeBatchFromRunningLoop(batch);
							this.reloadImportBatch(batch);
						} else {
							batch.lastUpdated =  response.data.lastUpdated as Date;
						}
					} else {
						this.handleError(response.errors[0] ? response.errors[0] : 'error on get batch progress');
					}
					// keep the loop running ..
					this.batchRunningLoop = setTimeout(() => {
						this.runningLoop();
					}, this.PROGRESS_CHECK_INTERVAL);
				}, error => {
					clearTimeout(this.batchRunningLoop);
					this.handleError(error);
				});
			}
		}
	}

	/**
	 * Initializes the loop for batches on QUEUED state.
	 */
	private setQueuedLoop(): void {
		this.queuedBatches = this.dataGridOperationsHelper.resultSet.filter( (item: ImportBatchModel) => {
			return item.status.code === BatchStatus.QUEUED.toString();
		});
		this.queuedLoop([...this.queuedBatches]);
	}

	/**
	 * Queued Loop, called recursively to check if there's any batch on QUEUED state and checking it's state.
	 * Receives a copy of the original list of queued batches, since this queued state can be applied to several batches
	 * at time, this avoids to run the function exponentially every time we add a batch to the list.
	 * This implementation waits for the first API call to finish in order to apply the time interval and do the next
	 * check. (instead of queuing the API calls asynchronously).
	 * @param {Array<ImportBatchModel>} batchList
	 */
	private queuedLoop(batchList: Array<ImportBatchModel>): void {
		// console.log('Queued batches: ', batchList.length);
		if (batchList.length === 0) {
			this.batchQueuedLoop = setTimeout(() => {
				this.setQueuedLoop();
			}, this.PROGRESS_CHECK_INTERVAL);
		} else {
			for (let i = 0; i < batchList.length; i++) {
				let batch: ImportBatchModel = batchList[i];
				this.importBatchService.getImportBatch(batch.id).subscribe((response: ApiResponseModel) => {
					if (response.status === ApiResponseModel.API_SUCCESS && response.data.status.code !== BatchStatus.QUEUED) {
						batch.status.code = (response.data as ImportBatchModel).status.code;
						batch.status.label = (response.data as ImportBatchModel).status.label;
						batch.recordsSummary = response.data.recordsSummary;
						this.removeBatchFromQueuedLoop(batch);
						if (batch.status.code === BatchStatus.RUNNING.toString()) {
							this.addToRunningBatchesLoop(batch);
						}
					}
					// last batch to check .. launch the loop;
					if (i === batchList.length - 1) {
						// keep the loop running ..
						this.batchQueuedLoop = setTimeout(() => {
							this.setQueuedLoop();
						}, this.PROGRESS_CHECK_INTERVAL);
					}
				}, error => {
					clearTimeout(this.batchQueuedLoop);
					this.handleError(error)
				});
			}
		}
	}

	/**
	 * Removes a batch from the RUNNING state batch list.
	 * @param {ImportBatchModel} batch
	 */
	private removeBatchFromRunningLoop(batch: ImportBatchModel): void {
		const filterIndex = this.runningBatches.findIndex((item: ImportBatchModel) => item.id === batch.id);
		this.runningBatches.splice(filterIndex, 1);
	}

	/**
	 * Removes a batch from the QUEUED state batch list.
	 * @param {ImportBatchModel} batch
	 */
	private removeBatchFromQueuedLoop(batch: ImportBatchModel): void {
		const filterIndex = this.queuedBatches.findIndex((item: ImportBatchModel) => item.id === batch.id);
		this.queuedBatches.splice(filterIndex, 1);
	}

	/**
	 * Adds a batch to the RUNNING state batch list.
	 * @param {ImportBatchModel} batch
	 */
	private addToRunningBatchesLoop(batch: ImportBatchModel): void {
		const filterIndex = this.runningBatches.findIndex((item: ImportBatchModel) => item.id === batch.id);
		if (filterIndex < 0) {
			this.runningBatches.push(batch);
		}
	}

	/**
	 * Adds a batch to the QUEUED state batch list.
	 * @param {ImportBatchModel} batch
	 */
	private addToQueuedBatchesLoop(batch: ImportBatchModel): void {
		const filterIndex = this.queuedBatches.findIndex((item: ImportBatchModel) => item.id === batch.id);
		if (filterIndex < 0) {
			this.queuedBatches.push(batch);
		}
	}

	/**
	 * Clears both RUNNING and QUEUED lists.
	 */
	private clearLoopsLists(): void {
		this.runningBatches = [];
		this.queuedBatches = [];
	}

	/**
	 * On grid pagination change event.
	 * @param {PageChangeEvent} $event
	 */
	protected onPageChange($event: PageChangeEvent): void {
		this.importBatchPreferences[IMPORT_BATCH_PREFERENCES.LIST_SIZE] = $event.take.toString();
		this.userPreferenceService.setPreference(PREFERENCES_LIST.IMPORT_BATCH_PREFERENCES, JSON.stringify(this.importBatchPreferences)).subscribe( result => {
			// nothing to do here ..
		});
		this.dataGridOperationsHelper.pageChange($event)
	}

	ngOnDestroy(): void {
		clearTimeout(this.batchRunningLoop);
		clearTimeout(this.batchQueuedLoop);
	}
}
