import {Component} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {DependencyBatchColumnsModel, ImportBatchModel} from '../../model/import-batch.model';
import {CellClickEvent, SelectableSettings} from '@progress/kendo-angular-grid';
import {DataGridOperationsHelper} from './data-grid-operations.helper';
import {Permission} from '../../../../shared/model/permission.model';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {DependencyBatchDetailDialogComponent} from '../dependency-batch-detail-dialog/dependency-batch-detail-dialog.component';
import {Observable} from 'rxjs/Observable';

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
		});
	}

	/**
	 * Load all Import Batch Unarchived list
	 */
	private reloadBatchList(): void {
		this.getUnarchivedBatches().then( batchList => {
			this.dataGridOperationsHelper.reloadData(batchList);
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
					for (let batch of batches ) {
						if (batch.status === 'RUNNING') {
							this.dependencyBatchService.getImportBatchProgress(batch.id).subscribe(res => {
								if (res.data.percentComp) {
									batch.currentProgress =  res.data.percentComp;
								} else {
									batch.currentProgress = 80;
								}
							}, error => console.log(error));
						}
					}
					resolve(batches);
				} else {
					this.handleError(result.errors ? result.errors[0] : null);
					resolve(null);
				}
			}, (err) => {
				this.handleError(err);
				resolve(null);
			});
		});
		return promise;
	}

	/**
	 * Load Archived Batches.
	 */
	private loadArchivedBatchList(): void {
		this.dependencyBatchService.getImportBatches().subscribe( result => {
			if (result.status === 'success') {
				let batches = result.data.filter( (item: ImportBatchModel) => {
					return item.archived;
				});
				this.dataGridOperationsHelper.reloadData(batches);
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
		this.dialogService.open(DependencyBatchDetailDialogComponent, []).then(result => {
			// silence is golden
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * On Archive batch button click.
	 */
	private onArchiveBatch(): void {
		const ids = this.dataGridOperationsHelper.getCheckboxSelectedItems().map( item => parseInt(item, 10));
		this.dependencyBatchService.archiveImportBatches(ids).subscribe( result => {
				if (result.status === 'success') {
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
		const ids = this.dataGridOperationsHelper.getCheckboxSelectedItems().map( item => parseInt(item, 10));
		this.dependencyBatchService.unArchiveImportBatches(ids).subscribe( result => {
				if (result.status === 'success') {
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
		this.dependencyBatchService.deleteImportBatches(ids).subscribe( result => {
				if (result.status === 'success') {
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
		// Not yet implemented ..
	}

	/**
	 * On Stop action button clicked, stop import batch.
	 * @param item
	 */
	private onStopButton(batch: ImportBatchModel): void {
		this.dependencyBatchService.stopImportBatch(batch.id).subscribe( (result) => {
			if (result.status === 'success') {
				this.reloadBatchList();
			} else {
				this.handleError(result.errors ? result.errors[0] : null);
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
}