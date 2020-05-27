// Angular
import {Component, ComponentFactoryResolver, Input, OnInit} from '@angular/core';
// Model
import {BatchStatus, ImportBatchModel} from '../../model/import-batch.model';
import {ImportBatchRecordDetailColumnsModel, ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {
	COLUMN_MIN_WIDTH,
	GridColumnModel,
	SELECT_ALL_COLUMN_WIDTH
} from '../../../../shared/model/data-list-grid.model';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {Dialog, DialogButtonType, DialogExit, DialogService, ModalSize} from 'tds-component-library';
// Component
import {ImportBatchRecordDialogComponent} from '../record/import-batch-record-dialog.component';
// Service
import {ImportBatchService} from '../../service/import-batch.service';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {NULL_OBJECT_PIPE} from '../../../../shared/pipes/utils.pipe';
import {
	PREFERENCES_LIST,
	IMPORT_BATCH_PREFERENCES,
	PreferenceService
} from '../../../../shared/services/preference.service';
import {ValidationUtils} from '../../../../shared/utils/validation.utils';
// Other
import {CellClickEvent, SelectableSettings} from '@progress/kendo-angular-grid';

@Component({
	selector: 'import-batch-detail-dialog',
	templateUrl: 'import-batch-detail-dialog.component.html'
})
export class ImportBatchDetailDialogComponent extends Dialog implements OnInit {
	@Input() data: any;

	private BatchStatus = BatchStatus;
	private columnsModel: ImportBatchRecordDetailColumnsModel;
	private selectableSettings: SelectableSettings = {mode: 'single', checkboxOnly: false};
	public dataGridOperationsHelper: DataGridOperationsHelper;
	private checkboxSelectionConfig = {
		useColumn: 'id'
	};
	private batchRecords: Array<ImportBatchRecordModel>;
	private batchRecordsFilter: any = {
		options: [{id: 1, name: 'All'},
			{
				id: 2, name: 'Pending', filters: [
					{column: 'status.label', value: 'Pending'},
				]
			},
			{
				id: 3, name: 'Pending with Errors', filters: [
					{column: 'status.label', value: 'Pending'},
					{column: 'errorCount', value: 1, operator: 'gte'},
				]
			},
			{
				id: 4, name: 'Ignored', filters: [
					{column: 'status.label', value: 'Ignored'},
				]
			},
			{
				id: 5, name: 'Completed', filters: [
					{column: 'status.label', value: 'Completed'},
				]
			}],
		selected: {id: 1, name: 'All'}
	};
	private batchRecordsUpdatedFlag = false;
	protected NULL_OBJECT_PIPE = NULL_OBJECT_PIPE;
	public dateTimeFormat: string;
	private importBatchPreferences = {};
	public SELECT_ALL_COLUMN_WIDTH = SELECT_ALL_COLUMN_WIDTH;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public importBatchModel: ImportBatchModel;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private importBatchService: ImportBatchService,
		private dialogService: DialogService,
		private userPreferenceService: PreferenceService) {
		super();
	}

	/**
	 * On Component Init get Import Batch Records.
	 */
	async ngOnInit(): Promise<void> {
		// Get Modal Model
		this.importBatchModel = Object.assign({}, this.data.importBatchModel);

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			tooltipText: 'Close',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.dateTimeFormat = this.userPreferenceService.getUserDateTimeFormat();
		this.batchRecords = [];

		const result = await this.userPreferenceService.getSinglePreference(PREFERENCES_LIST.IMPORT_BATCH_PREFERENCES).toPromise();
		if (result) {
			this.importBatchPreferences = JSON.parse(result);
			const match = this.batchRecordsFilter.options.find(item => item.name === this.importBatchPreferences[IMPORT_BATCH_PREFERENCES.RECORDS_FILTER]);
			if (match) {
				this.batchRecordsFilter.selected = match;
			}
		}

		this.prepareColumnsModel();

		this.loadImportBatchRecords();
	}

	/**
	 * Load Import Batch Records from API.
	 */
	private loadImportBatchRecords(): void {
		this.importBatchService.getImportBatchRecords(this.importBatchModel.id).subscribe((result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				this.batchRecords = result.data;
				this.dataGridOperationsHelper = new DataGridOperationsHelper(this.batchRecords, [], this.selectableSettings, this.checkboxSelectionConfig);
				this.onStatusFilter(this.batchRecordsFilter.selected, true);
			} else {
				this.handleError(result.errors[0] ? result.errors[0] : 'error loading Batch Records');
			}
		}, error => this.handleError(error));
	}

	/**
	 * Load A Single Batch Record from API.
	 */
	private reloadSingleBatchRecord(batchRecord: ImportBatchRecordModel): void {
		this.importBatchService.getImportBatchRecordUpdated(this.importBatchModel.id, batchRecord.id).subscribe((result: ImportBatchRecordModel) => {
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
		let fieldColumns: Array<GridColumnModel> = fieldNameList.map(field => {
			const column: GridColumnModel = new GridColumnModel();
			column.label = (fieldLabelMap && fieldLabelMap[field]) || field;
			column.properties = ['currentValues', field];
			column.width = 130;
			column.cellStyle = {'max-height': '20px'};
			column.type = 'dynamicValue';
			column.property =  column.properties.join('.');
			return column;
		});
		this.columnsModel.columns = this.columnsModel.columns.concat(fieldColumns);
	}

	/**
	 * On Row Click open the record detail extra popup.
	 * @param $event
	 */
	private async openBatchRecordDetail(cellClick: CellClickEvent): Promise<void> {
		// prevent open detail on column 0
		if (cellClick.columnIndex === 0) {
			return;
		}
		this.dataGridOperationsHelper.selectCell(cellClick); // mark row as selected
		let selectedBatchRecord = (cellClick as any).dataItem;
		// prevent errors when clicking empty rows ..
		if (!selectedBatchRecord || !selectedBatchRecord.id) {
			return;
		}
		const result = await this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: ImportBatchRecordDialogComponent,
			data: {
				importBatchModel: this.importBatchModel,
				importBatchRecordModel:  selectedBatchRecord
			},
			modalConfiguration: {
				title: `Record Detail`,
				draggable: true,
				modalSize: ModalSize.XL
			}
		}).toPromise();

		if (result.status === DialogExit.ACCEPT && result.reloadRecords) {
			this.reloadSingleBatchRecord(selectedBatchRecord);
			this.batchRecordsUpdatedFlag = true;
		}
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
	public cancelCloseDialog(): void {
		this.onAcceptSuccess({batchRecordsUpdatedFlag: this.batchRecordsUpdatedFlag});
	}

	/**
	 * Clear the batchRecordsFilter if needed (depending on column that was changed)
	 * @param {GridColumnModel} column
	 */
	private preProcessFilter(value: any, column: GridColumnModel): void {
		if (!value) {
			this.preProcessClear(column);
		} else {
			this.clearStatusFilter(column);
			this.dataGridOperationsHelper.onFilterWithValue(value, column);
		}
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
	 * @param avoidPreferenceSave used when we want to stop saving the filter in user preferences.
	 */
	private onStatusFilter(event, avoidPreferenceSave = false) {
		for (const columnProperty of ['status.label', 'errorCount']) {
			let foundMatch: GridColumnModel = this.columnsModel.columns.find((column: GridColumnModel) => column.property === columnProperty);
			foundMatch.filter = null;
			this.dataGridOperationsHelper.clearValue(foundMatch);
		}
		if (event.id !== 1) {
			for (const filter of event.filters) {
				let foundMatch: GridColumnModel = this.columnsModel.columns.find((column: GridColumnModel) => column.property === filter.column);
				if (foundMatch) {
					foundMatch.filter = filter.value;
					this.dataGridOperationsHelper.onFilter(foundMatch, event.id === 3 ? 'gte' : null);
				}
			}
		}
		if (!avoidPreferenceSave) {
			this.importBatchPreferences[IMPORT_BATCH_PREFERENCES.RECORDS_FILTER] = event.name;
			this.userPreferenceService.setPreference(PREFERENCES_LIST.IMPORT_BATCH_PREFERENCES, JSON.stringify(this.importBatchPreferences)).subscribe(r => { /**/
			});
		}
	}

	/**
	 * On Ignore button click.
	 */
	private onIgnore(): void {
		const ids = this.dataGridOperationsHelper.getCheckboxSelectedItemsAsNumbers();
		this.importBatchService.ignoreBatchRecords(this.importBatchModel.id, ids)
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
		this.importBatchService.processBatchRecords(this.importBatchModel.id, ids)
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
	 * Determines which value to print on the batch record dynamic values, can be direct value, init or null.
	 * @returns {string}
	 */
	protected getInitOrValue(dataItem: ImportBatchRecordModel, column: GridColumnModel): string {
		const isEmptyVal = !dataItem.currentValues[column.properties[1]] || ValidationUtils.isEmptyObject(dataItem.currentValues[column.properties[1]])
		if (isEmptyVal && dataItem.init) {
			const isEmptyInit = !dataItem.init[column.properties[1]] || ValidationUtils.isEmptyObject(dataItem.init[column.properties[1]]);
			return !isEmptyInit ? dataItem.init[column.properties[1]] : '(null)';
		}
		return isEmptyVal ? '(null)' : dataItem.currentValues[column.properties[1]];
	}

	/**
	 * Determines if value of current record value is INIT value.
	 * @returns {string}
	 */
	protected hasInitVal(dataItem: ImportBatchRecordModel, column: GridColumnModel): boolean {
		const isEmptyVal = !dataItem.currentValues[column.properties[1]] || ValidationUtils.isEmptyObject(dataItem.currentValues[column.properties[1]])
		if (isEmptyVal && dataItem.init) {
			const isEmptyInit = !dataItem.init[column.properties[1]] || ValidationUtils.isEmptyObject(dataItem.init[column.properties[1]]);
			return !isEmptyInit;
		}
		return false;
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
