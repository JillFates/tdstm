import {Component, OnInit} from '@angular/core';
import {BatchStatus, ImportBatchModel} from '../../model/import-batch.model';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {CellClickEvent, SelectableSettings} from '@progress/kendo-angular-grid';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {ImportBatchRecordDetailColumnsModel, ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {GridColumnModel} from '../../../../shared/model/data-list-grid.model';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {DependencyBatchRecordDetailDialogComponent} from '../dependency-batch-record-detail-dialog/dependency-batch-record-detail-dialog.component';
import {KEYSTROKE} from '../../../../shared/model/constants';
import {NULL_OBJECT_PIPE} from '../../../../shared/pipes/utils.pipe';
import {PreferenceService} from '../../../../shared/services/preference.service';

@Component({
	selector: 'dependency-batch-detail-dialog',
	templateUrl: '../tds/web-app/app-js/modules/importBatch/components/dependency-batch-detail-dialog/dependency-batch-detail-dialog.component.html',
	host: {
		'(keydown)': 'keyDownHandler($event)'
	}
})
export class DependencyBatchDetailDialogComponent implements OnInit {

	private BatchStatus = BatchStatus;
	private columnsModel: ImportBatchRecordDetailColumnsModel;
	private selectableSettings: SelectableSettings = { mode: 'single', checkboxOnly: false};
	private dataGridOperationsHelper: DataGridOperationsHelper;
	private checkboxSelectionConfig = {
		useColumn: 'id'
	};
	private batchRecords: Array<ImportBatchRecordModel>;
	private batchRecordsFilter: any = {
		options: [{id: 1, name: 'All'},
			{id: 2, name: 'Pending', filters: [
				{column: 'status.label', value: 'Pending'},
			]},
			{id: 3, name: 'Pending with Errors', filters: [
				{column: 'status.label', value: 'Pending'},
				{column: 'errorCount', value: 1, operator: 'gte'},
			]},
			{id: 4, name: 'Ignored', filters: [
				{column: 'status.label', value: 'Ignored'},
			]},
			{id: 5, name: 'Completed', filters: [
				{column: 'status.label', value: 'Completed'},
			]}],
		selected: {id: 1, name: 'All'}
	};
	private batchRecordsUpdatedFlag = false;
	protected NULL_OBJECT_PIPE = NULL_OBJECT_PIPE;
	public dateTimeFormat: string;

	constructor(
		private importBatchModel: ImportBatchModel,
		private dependencyBatchService: DependencyBatchService,
		private activeDialog: UIActiveDialogService,
		private dialogService: UIDialogService,
		private userPreferenceService: PreferenceService
	) {
			this.prepareColumnsModel();
			this.onLoad();
	}

	/**
	 * On Page Load
	 */
	private onLoad(): void {
		this.dateTimeFormat = this.userPreferenceService.getUserDateTimeFormat();
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
	 * Load A Single Batch Record from API.
	 */
	private reloadSingleBatchRecord(batchRecord: ImportBatchRecordModel): void {
		this.dependencyBatchService.getImportBatchRecordUpdated(this.importBatchModel.id, batchRecord.id).subscribe((result: ImportBatchRecordModel) => {
			if (result) {
				Object.assign(batchRecord, result);
			} else {
				this.loadImportBatchRecords();
			}
		});
	}

	/**
	 * Get and add the dyanmic batch record field columns.
	 */
	private prepareColumnsModel(): void {
		this.columnsModel = new ImportBatchRecordDetailColumnsModel();
		const {fieldNameList, fieldLabelMap} = this.importBatchModel;

		// const mock: Array<string> = [ 'Name (P)', 'Type (P)', 'Dep Type (P)', 'Name (D)', 'Type (D)'];
		let fieldColumns: Array<GridColumnModel> = fieldNameList.map( field => {
			const column: GridColumnModel = new GridColumnModel();
			column.label = (fieldLabelMap && fieldLabelMap[field]) || field;
			column.properties = ['currentValues', field];
			column.width = 130;
			column.cellStyle = {'max-height': '20px'};
			column.type = 'dynamicValue';
			return column;
		});
		this.columnsModel.columns = this.columnsModel.columns.concat(fieldColumns);
	}

	/**
	 * On Row Click open the record detail extra popup.
	 * @param $event
	 */
	private openBatchRecordDetail(cellClick: CellClickEvent): void {
		// prevent open detail on column 0
		if (cellClick.columnIndex === 0 ) {
			return;
		}
		this.dataGridOperationsHelper.selectCell(cellClick); // mark row as selected
		let selectedBatchRecord = (cellClick as any).dataItem;
		// prevent errors when clicking empty rows ..
		if (!selectedBatchRecord || !selectedBatchRecord.id) {
			return;
		}
		this.dialogService.extra(DependencyBatchRecordDetailDialogComponent, [
				{provide: ImportBatchModel, useValue: this.importBatchModel},
				{provide: ImportBatchRecordModel, useValue: selectedBatchRecord}
			], false, false)
			.then((result) => {
				if (result === 'reload') {
					this.reloadSingleBatchRecord(selectedBatchRecord);
					this.batchRecordsUpdatedFlag = true;
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
		this.activeDialog.close(this.batchRecordsUpdatedFlag);
	}

	/**
	 * Clear the batchRecordsFilter if needed (depending on column that was changed)
	 * @param {GridColumnModel} column
	 */
	private preProcessFilter(column: GridColumnModel): void {
		this.clearStatusFilter(column);
		this.dataGridOperationsHelper.onFilter(column);
	}

	/**
	 * Clear the batchRecordsFilter if needed (depending on column that was changed)
	 */
	private preProcessClear(column: GridColumnModel): void {
		this.clearStatusFilter(column);
		this.dataGridOperationsHelper.clearValue(column);
	}

	/**
	 * Clear batchRecordsFilter to default All option.
	 * @param {GridColumnModel} column
	 */
	private clearStatusFilter(column: GridColumnModel): void {
		if (column.property === 'status.label' || column.property === 'errorCount') {
			this.batchRecordsFilter.selected = {id: 1, name: 'All'};
		}
	}

	/**
	 * On Status Select Filter Select handle the multi filter on status + errors.
	 * @param $event
	 */
	private onStatusFilter($event) {
		for (const columnProperty of ['status.label', 'errorCount']) {
			let foundMatch: GridColumnModel = this.columnsModel.columns.find( (column: GridColumnModel) => column.property === columnProperty );
			foundMatch.filter = null;
			this.dataGridOperationsHelper.clearValue(foundMatch);
		}
		if ($event.id !== 1) {
			for (const filter of $event.filters) {
				let foundMatch: GridColumnModel = this.columnsModel.columns.find( (column: GridColumnModel) => column.property === filter.column );
				if (foundMatch) {
					foundMatch.filter = filter.value;
					this.dataGridOperationsHelper.onFilter(foundMatch, $event.id === 3 ? 'gte' : null);
				}
			}
		}
	}

	/**
	 * On Ignore button click.
	 */
	private onIgnore(): void {
		const ids = this.dataGridOperationsHelper.getCheckboxSelectedItemsAsNumbers();
		this.dependencyBatchService.ignoreBatchRecords(this.importBatchModel.id, ids)
			.subscribe((result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.loadImportBatchRecords();
					this.batchRecordsUpdatedFlag = true;
				} else {
					this.handleError(result.errors[0] ? result.errors[0] : 'error on bulk ignore batch records.');
				}
			}, error => this.handleError(error));
	}

	/**
	 * On Process button click
	 */
	private onProcess(): void {
		const ids = this.dataGridOperationsHelper.getCheckboxSelectedItemsAsNumbers();
		this.dependencyBatchService.processBatchRecords(this.importBatchModel.id, ids)
			.subscribe((result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.loadImportBatchRecords();
					this.batchRecordsUpdatedFlag = true;
				} else {
					this.handleError(result.errors[0] ? result.errors[0] : 'error on bulk Process batch records.');
				}
			}, error => this.handleError(error));
	}

	/**
	 * Can Batch Record perform any bulk operation? (PROCESS or IGNORE).
	 * @param {ImportBatchRecordModel} batchRecord
	 * @returns {boolean}
	 */
	private batchRecordCanAction(batchRecord: ImportBatchRecordModel): boolean {
		return batchRecord.status.code === BatchStatus.PENDING || batchRecord.status.code === BatchStatus.IGNORED;
	}

	/**
	 * Detect if the use has pressed the on Escape to close the dialog and popup if there are pending changes.
	 * @param {KeyboardEvent} event
	 */
	private keyDownHandler($event: KeyboardEvent): void {
		if ($event && $event.code === KEYSTROKE.ESCAPE) {
			this.cancelCloseDialog();
		}
	}
}