import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild} from '@angular/core';
import {ImportBatchService} from '../../service/import-batch.service';
import {BATCH_RECORD_OPERATION, ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {BatchStatus, ImportBatchModel} from '../../model/import-batch.model';
import {process, State} from '@progress/kendo-data-query';
import {GridDataResult} from '@progress/kendo-angular-grid';
import {ValidationUtils} from '../../../../shared/utils/validation.utils';
import {CHECK_ACTION, OperationStatusModel} from '../../../../shared/components/check-action/model/check-action.model';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'import-batch-record-fields',
	templateUrl: '../tds/web-app/app-js/modules/importBatch/components/record/import-batch-record-fields.component.html'
})
export class ImportBatchRecordFieldsComponent implements OnInit {

	@Input('importBatch') importBatch: ImportBatchModel;
	@Input('batchRecord') batchRecord: ImportBatchRecordModel;
	@Output('onClose') closeEvent = new EventEmitter<any>();
	@Output('onUpdateSuccess') updateSuccessEvent = new EventEmitter<any>();
	@Output('onBatchRecordUpdated') onBatchRecordUpdated = new EventEmitter<any>();

	private fieldsInfo: Array<{
		name: string,
		currentValue: string,
		importValue: string,
		initValue: string,
		errors: Array<string>,
		errorsAsString: string,
		overridedValue: string
	}>;
	private state: State = {
		filter: {
			filters: [],
			logic: 'and'
		}
	};
	protected gridData: GridDataResult;
	protected fieldsFilter: any = {
		options: [
			{text: 'All', value: 1},
			{text: 'With Errors', value: 2}
			],
		selected: {text: 'All', value: 1},
		nameFilter: {
			field: 'name',
			value: '',
			operator: 'contains',
			ignoreCase: true
		},
		errorsFilter: {
			field: 'errorsAsString',
			value: '',
			operator: 'isnotempty',
			ignoreCase: true
		}
	};
	private originalImportValues: string;
	protected BATCH_RECORD_OPERATION = BATCH_RECORD_OPERATION;
	protected BatchStatus = BatchStatus;
	protected saveStatus: OperationStatusModel = new OperationStatusModel();
	protected processStatus: OperationStatusModel = new OperationStatusModel();
	public MESSAGE_FIELD_WILL_BE_INITIALIZED: string;
	protected popup: any = { show: false, offset: {}, margin: {horizontal: 2, vertical: 2}, position: 'fixed'};

	constructor(
		private importBatchService: ImportBatchService,
		private translatePipe: TranslatePipe,
		private dialogService: UIDialogService) {
			this.state.filter.filters.push(this.fieldsFilter.nameFilter);
			this.processStatus.state = CHECK_ACTION.NONE;
			this.saveStatus.state = CHECK_ACTION.NONE;
	}

	/**
	 * On Init Load Record Field details.
	 */
	ngOnInit(): void {
		this.MESSAGE_FIELD_WILL_BE_INITIALIZED =  this.translatePipe.transform('DATA_INGESTION.DATASCRIPT.DESIGNER.FIELD_WILL_BE_INITIALIZED');
		this.loadRecordFieldDetails();
	}

	/**
	 * Gets the Batch Record Field Details from API.
	 */
	private loadRecordFieldDetails(updateProcessStatus = false): void {
		this.importBatchService.getImportBatchRecordFieldDetail(this.batchRecord.importBatch.id, this.batchRecord.id)
			.subscribe( (result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				this.onBatchRecordUpdated.emit({batchRecord: result.data});
				this.buildGridData(result.data.fieldsInfo);
				this.processStatus.state = CHECK_ACTION.NONE;
				if (updateProcessStatus) {
					let fieldsWithErrors = this.fieldsInfo.filter(item => item.errors.length > 0);
					this.processStatus.state = fieldsWithErrors.length > 0 ? CHECK_ACTION.INVALID : CHECK_ACTION.NONE;
				}
			} else {
				this.handleError(result.errors[0] ? result.errors[0] : 'error calling endpoint');
			}
		}, error => {
			this.fieldsInfo = [];
			this.handleError(error);
		});
	}

	/**
	 * Builds and prepares fieldInfo array to be display on the gridData.
	 * @param fields
	 */
	private buildGridData(fields): void {
		// let data: Array<{name: string, currentValue: string, importValue: string, error: boolean}> = [];
		const {fieldNameList, fieldLabelMap} = this.importBatch;

		this.fieldsInfo = [];
		for (const fieldName of fieldNameList) {
			// Not all rows will have all of the same fields so must check first
			if ( fields[fieldName] !== undefined ) {
				this.fieldsInfo.push({
					name: (fieldLabelMap && fieldLabelMap[fieldName]) || fieldName,
					currentValue: !ValidationUtils.isEmptyObject(fields[fieldName].originalValue)
						? fields[fieldName].originalValue : '(null)',
					importValue: !ValidationUtils.isEmptyObject(fields[fieldName].value)
						? fields[fieldName].value : '',
					initValue: !ValidationUtils.isEmptyObject(fields[fieldName].init)
						? fields[fieldName].init : '',
					errors: fields[fieldName].errors,
					errorsAsString: fields[fieldName].errors ? fields[fieldName].errors.join() : '',
					overridedValue: null
				});
			}
		}
		let importValues = this.fieldsInfo.map( item => item.importValue);
		this.originalImportValues = JSON.stringify(importValues);
		this.gridData = process(this.fieldsInfo, this.state);
	}

	/**
	 * Checks if input overrided values are not empty or with a text value.
	 */
	public areOverrideValuesDirty(): boolean {
		let currentImportValues = this.fieldsInfo.map( item => item.importValue);
		if (this.originalImportValues !== JSON.stringify(currentImportValues)) {
			this.saveStatus.state = CHECK_ACTION.NONE;
			return true;
		}
		return false;
	}

	/**
	 * On Ignore button click.
	 */
	private onIgnore(): void {
		const ids = [this.batchRecord.id];
		this.importBatchService.ignoreBatchRecords(this.importBatch.id, ids)
			.subscribe((result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.loadRecordFieldDetails();
					this.updateSuccessEvent.emit();
				} else {
					this.handleError(result.errors[0] ? result.errors[0] : 'error on ignore batch record.');
				}
			}, error => this.handleError(error));
	}

	/**
	 * On Include button click.
	 */
	protected onInclude(): void {
		const ids = [this.batchRecord.id];
		this.importBatchService.includeBatchRecords(this.importBatch.id, ids)
			.subscribe((result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.loadRecordFieldDetails();
					this.updateSuccessEvent.emit();
				} else {
					this.handleError(result.errors[0] ? result.errors[0] : 'error on include batch record.');
				}
			}, error => this.handleError(error));
	}

	/**
	 * On Update button click.
	 */
	protected onUpdate(): void {
		let newFieldsValues: Array<{fieldName: string, value: string}> = [];
		for (let field of this.fieldsInfo) {
			if (field.importValue) {
				const newFieldValue = {fieldName: field.name, value: field.importValue};
				newFieldsValues.push(newFieldValue);
			}
		}
		this.importBatchService.updateBatchRecordFieldsValues(this.importBatch.id, this.batchRecord.id, newFieldsValues)
			.subscribe((result: ApiResponseModel) => {
				this.loadRecordFieldDetails();
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.updateSuccessEvent.emit();
					setTimeout(() => this.saveStatus.state = CHECK_ACTION.VALID, 200);
				} else {
					setTimeout(() => this.saveStatus.state = CHECK_ACTION.INVALID, 200);
					this.handleError(result.errors[0] ? result.errors[0] : 'error updating field values');
				}
		}, error => this.handleError(error));
	}

	/**
	 * On Process button click.
	 */
	protected onProcess(): void {
		const ids = [this.batchRecord.id];
		this.importBatchService.processBatchRecords(this.importBatch.id, ids)
			.subscribe((result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.loadRecordFieldDetails(true);
					this.updateSuccessEvent.emit();
				} else {
					this.handleError(result.errors[0] ? result.errors[0] : 'error on Process batch record.');
				}
			}, error => this.handleError(error));
	}

	/**
	 * Determine if Ignore button should be showed on UI.
	 * @returns {boolean}
	 */
	protected showIgnoreButton(): boolean {
		return this.batchRecord.status.code === BatchStatus.PENDING;
	}

	/**
	 * Determine if Include button should be showed on UI.
	 * @returns {boolean}
	 */
	protected showIncludeButton(): boolean {
		return this.batchRecord.status.code === BatchStatus.IGNORED;
	}

	/**
	 * Hide Action buttons if Record is already completed.
	 * @returns {boolean}
	 */
	protected showActionButtons(): boolean {
		return this.batchRecord.status.code === BatchStatus.PENDING;
	}

	/**
	 * On Text Filter input change it's value.
	 */
	private onTextFilter(): void {
		this.gridData = process(this.fieldsInfo, this.state);
	}

	/**
	 * On Fields Filter dropdown select change.
	 * @param {{text: string; value: number}} $event
	 */
	private onFieldsFilter($event: {text: string, value: number}): void {
		if ($event.value === 2) {
			this.state.filter.filters.push(this.fieldsFilter.errorsFilter);
		} else {
			const filterIndex = this.state.filter.filters.findIndex((r: any) => r.field === 'errorsAsString');
			this.state.filter.filters.splice(filterIndex, 1);
		}
		this.gridData = process(this.fieldsInfo, this.state);
	}

	/**
	 * On Text Filter input clear icon click.
	 */
	private clearTextFilter(): void {
		this.fieldsFilter.nameFilter.value = '';
		this.onTextFilter();
	}

	/**
	 * On Cancel.
	 */
	private onCancel(): void {
		this.closeEvent.emit();
	}

	/**
	 * Handles any API error and displays it on UI.
	 * @param e
	 */
	private handleError(e): void {
		console.log(e);
	}

	/**
	 * Opens and positions the popup based on the click event.
	 * @param {MouseEvent} $event
	 */
	protected onShowPopup($event: MouseEvent): void {
		this.popup.offset = { left: $event.pageX, top: $event.pageY};
		this.popup.show = true;
	}
}