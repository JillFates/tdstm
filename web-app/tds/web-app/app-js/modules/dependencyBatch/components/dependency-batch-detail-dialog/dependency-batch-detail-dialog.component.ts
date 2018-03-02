
import {Component, Inject, OnInit} from '@angular/core';
import {ImportBatchModel, ImportBatchRecordDetailColumnsModel} from '../../model/import-batch.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {SelectableSettings} from '@progress/kendo-angular-grid';
import {DataGridOperationsHelper} from '../dependency-batch-list/data-grid-operations.helper';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {CellClickEvent, SelectableSettings} from '@progress/kendo-angular-grid';
import {DataGridOperationsHelper} from '../dependency-batch-list/data-grid-operations.helper';
import {ImportBatchRecordDetailColumnsModel, ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {GridColumnModel} from '../../../../shared/model/data-list-grid.model';
import {ApiReponseModel} from '../../../../shared/model/ApiReponseModel';
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
		options: [{id: 1, name: 'All'},
			{id: 2, name: 'Pending'},
			{id: 3, name: 'Pending with Errors'},
			{id: 4, name: 'Ignored'},
			{id: 5, name: 'Completed'}],
		selected: {id: 1, name: 'All'}
	};

	constructor(
		private importBatchModel: ImportBatchModel,
		private promptService: UIPromptService,
		private dependencyBatchService: DependencyBatchService,
		private activeDialog: UIActiveDialogService,
		private dialogService: UIDialogService) {
			this.prepareColumnsModel();
	}

	ngOnInit(): void {
		this.dependencyBatchService.getImportBatchRecords(this.importBatchModel.id).subscribe( (result: ApiReponseModel) => {
			if (result.status === ApiReponseModel.API_SUCCESS) {
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
	 * TODO: document
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
	 * TODO: document
	 * @param $event
	 */
	private openBatchRecordDetail(cellClick: CellClickEvent): void {
		// this.selectedBatchRecord = (cellClick as any).dataItem;
		let selectedBatchRecord = (cellClick as any).dataItem;
		this.dialogService.extra(DependencyBatchRecordDetailDialogComponent, [
				{provide: ImportBatchModel, useValue: this.importBatchModel},
				{provide: ImportBatchRecordModel, useValue: selectedBatchRecord}
			], false, false).then((result) => {
				// ???
			});
	}

	/**
	 * TODO: document
	 */
	/*private closeBatchRecordDetail(): void {
		this.selectedBatchRecord = null;
	}*/

	/**
	 * TODO: document
	 * @param error
	 */
	private handleError(error): void {
		console.log(error);
		this.dataGridOperationsHelper = new DataGridOperationsHelper(this.batchRecords, [], this.selectableSettings, this.checkboxSelectionConfig);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}
}