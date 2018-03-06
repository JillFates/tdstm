import {Component, OnInit} from '@angular/core';
import {ImportBatchModel} from '../../model/import-batch.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {CellClickEvent, SelectableSettings} from '@progress/kendo-angular-grid';
import {DataGridOperationsHelper} from '../dependency-batch-list/data-grid-operations.helper';
import {ImportBatchRecordDetailColumnsModel, ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {GridColumnModel} from '../../../../shared/model/data-list-grid.model';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {DependencyBatchRecordDetailDialogComponent} from '../dependency-batch-record-detail-dialog/dependency-batch-record-detail-dialog.component';

@Component({
	selector: 'dependency-batch-detail-dialog',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-detail-dialog/dependency-batch-detail-dialog.component.html'
})
export class DependencyBatchDetailDialogComponent implements OnInit {

	private columnsModel: ImportBatchRecordDetailColumnsModel;
	private selectableSettings: SelectableSettings = { mode: 'single', checkboxOnly: false};
	private dataGridOperationsHelper: DataGridOperationsHelper;
	private checkboxSelectionConfig = {
		useColumn: 'id'
	};
	private batchRecords: Array<ImportBatchRecordModel>;
	// private selectedBatchRecord: ImportBatchRecordModel;
	private batchRecordsFilter: any = {
		options: [{id: 1, name: 'All', filter: {}},
			{id: 2, name: 'Pending'},
			{id: 3, name: 'Pending with Errors'},
			{id: 4, name: 'Ignored'},
			{id: 5, name: 'Completed'}],
		selected: {id: 1, name: 'All'}
	};

	constructor(
		private importBatchModel: ImportBatchModel,
		private dependencyBatchService: DependencyBatchService,
		private activeDialog: UIActiveDialogService,
		private dialogService: UIDialogService) {
			this.prepareColumnsModel();
	}

	/**
	 * On Component Init get Import Batch Records.
	 */
	ngOnInit(): void {
		this.loadImportBatchRecords();
	}

	/**
	 * Load Import Batch Records from API.
	 */
	private loadImportBatchRecords(): void {
		this.dependencyBatchService.getImportBatchRecords(this.importBatchModel.id).subscribe( (result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				this.batchRecords = result.data;
				this.dataGridOperationsHelper = new DataGridOperationsHelper(this.batchRecords, [], this.selectableSettings, this.checkboxSelectionConfig);
			} else {
				this.batchRecords = [];
				this.handleError(result.errors[0] ? result.errors[0] : 'error calling endpoint');
			}
		}, error => {
			this.batchRecords = [];
			this.handleError(error);
		});
	}

	/**
	 * Get and add the dyanmic batch record field columns.
	 */
	private prepareColumnsModel(): void {
		this.columnsModel = new ImportBatchRecordDetailColumnsModel();
		// const mock: Array<string> = [ 'Name (P)', 'Type (P)', 'Dep Type (P)', 'Name (D)', 'Type (D)'];
		let fieldColumns: Array<GridColumnModel> = this.importBatchModel.fieldNameList.map( field => {
			const column: GridColumnModel = new GridColumnModel();
			column.label = field;
			column.property = `currentValues.${field}`;
			column.width = 130;
			column.cellStyle = {'max-height': '20px'};
			return column;
		});
		this.columnsModel.columns = this.columnsModel.columns.concat(fieldColumns);
	}

	/**
	 * On Row Click open the record detail extra popup.
	 * @param $event
	 */
	private openBatchRecordDetail(cellClick: CellClickEvent): void {
		// this.selectedBatchRecord = (cellClick as any).dataItem;
		let selectedBatchRecord = (cellClick as any).dataItem;
		this.dialogService.extra(DependencyBatchRecordDetailDialogComponent, [
				{provide: ImportBatchModel, useValue: this.importBatchModel},
				{provide: ImportBatchRecordModel, useValue: selectedBatchRecord}
			], false, false)
			.then((result) => {
				console.log('closed');
				if (result === 'reload') {
					this.loadImportBatchRecords();
				}
			}).catch( result => { console.log('dismissed'); });
	}

	/**
	 * Handles API errors and displays it on UI.
	 * @param error
	 */
	private handleError(error): void {
		console.log(error);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	private cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

	/**
	 * On Status Select Filter Select handle the multi filter on status + errors.
	 * @param $event
	 */
	private onStatusFilter($event) {
		console.log($event);
	}
}