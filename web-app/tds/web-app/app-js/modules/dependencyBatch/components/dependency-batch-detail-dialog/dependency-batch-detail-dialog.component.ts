import {Component, OnInit} from '@angular/core';
import {ImportBatchModel} from '../../model/import-batch.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {CellClickEvent, SelectableSettings} from '@progress/kendo-angular-grid';
import {DataGridOperationsHelper} from '../dependency-batch-list/data-grid-operations.helper';
import {ImportBatchRecordDetailColumnsModel, ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {GridColumnModel} from '../../../../shared/model/data-list-grid.model';

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
	private selectedBatchRecord: ImportBatchRecordModel;
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
		private activeDialog: UIActiveDialogService) {
			this.prepareColumnsModel();
	}

	/**
	 * TODO: document
	 */
	ngOnInit(): void {
		this.dependencyBatchService.getImportBatchRecords(this.importBatchModel.id).subscribe( result => {
			this.batchRecords = result;
			this.dataGridOperationsHelper = new DataGridOperationsHelper(this.batchRecords, [], this.selectableSettings, this.checkboxSelectionConfig);
		}, error => this.handleError(error));
	}

	/**
	 * TODO: document
	 */
	private prepareColumnsModel(): void {
		this.columnsModel = new ImportBatchRecordDetailColumnsModel();
		// TODO: use --> this.importBatchModel.fieldNameList;
		const mock: Array<string> = [ 'Name (P)', 'Type (P)', 'Dep Type (P)', 'Name (D)', 'Type (D)'];
		let fieldColumns: Array<GridColumnModel> = mock.map( field => {
			const column: GridColumnModel = new GridColumnModel();
			column.label = field;
			column.property = `fields.${field}`;
			column.width = 130;
			return column;
		});
		this.columnsModel.columns = this.columnsModel.columns.concat(fieldColumns);
	}

	/**
	 * TODO: document
	 * @param $event
	 */
	private openBatchRecordDetail(cellClick: CellClickEvent): void {
		this.selectedBatchRecord = (cellClick as any).dataItem;
	}

	/**
	 * TODO: document
	 */
	private closeBatchRecordDetail(): void {
		this.selectedBatchRecord = null;
	}

	/**
	 * TODO: document
	 * @param error
	 */
	private handleError(error): void {
		console.log(error);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}
}