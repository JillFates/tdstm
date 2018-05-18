import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {BatchStatus, ImportBatchModel} from '../../model/import-batch.model';
import {process, State} from '@progress/kendo-data-query';
import {GridDataResult} from '@progress/kendo-angular-grid';
import {ValidationUtils} from '../../../../shared/utils/validation.utils';
import {CHECK_ACTION, OperationStatusModel} from '../../../../shared/components/check-action/model/check-action.model';

@Component({
	selector: 'dependency-batch-record-detail-fields',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-record-detail-fields/dependency-batch-record-detail-fields.component.html'
})
export class DependencyBatchRecordDetailFieldsComponent implements OnInit {

	@Input('importBatch') importBatch: ImportBatchModel;
	@Input('batchRecord') batchRecord: ImportBatchRecordModel;
	@Output('onClose') closeEvent = new EventEmitter<any>();
	@Output('onUpdateSuccess') updateSuccessEvent = new EventEmitter<any>();
	@Output('onBatchRecordUpdated') onBatchRecordUpdated = new EventEmitter<any>();

	private fieldsInfo: Array<{
		name: string,
		currentValue: string,
		importValue: string,
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
	private gridData: GridDataResult;
	private fieldsFilter: any = {
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
	private processStatus: OperationStatusModel = new OperationStatusModel();

	constructor(private dependencyBatchService: DependencyBatchService) {
			this.state.filter.filters.push(this.fieldsFilter.nameFilter);
			this.processStatus.state = CHECK_ACTION.NONE;
	}

	/**
	 * On Init Load Record Field details.
	 */
	ngOnInit(): void {
		this.loadRecordFieldDetails();
	}

	/**
	 * Gets the Batch Record Field Details from API.
	 */
	private loadRecordFieldDetails(updateProcessStatus = false): void {
		this.dependencyBatchService.getImportBatchRecordFieldDetail(this.batchRecord.importBatch.id, this.batchRecord.id)
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
		this.fieldsInfo = [];
		for (const fieldName of this.importBatch.fieldNameList) {
			this.fieldsInfo.push({
				name: fieldName,
				currentValue: !ValidationUtils.isEmptyObject(fields[fieldName].value)
					? fields[fieldName].value : '(null)',
				importValue: !ValidationUtils.isEmptyObject(fields[fieldName].originalValue)
					? fields[fieldName].originalValue : '(null)',
				errors: fields[fieldName].errors,
				errorsAsString: fields[fieldName].errors ? fields[fieldName].errors.join() : '',
				overridedValue: null
			});
		}
		this.gridData = process(this.fieldsInfo, this.state);
	}

	/**
	 * Checks if input overrided values are not empty or with a text value.
	 */
	public areOverrideValuesDirty(): boolean {
		for (let field of this.fieldsInfo) {
			if (field.overridedValue && field.overridedValue.length > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * On Ignore button click.
	 */
	private onIgnore(): void {
		const ids = [this.batchRecord.id];
		this.dependencyBatchService.ignoreBatchRecords(this.importBatch.id, ids)
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
	private onInclude(): void {
		const ids = [this.batchRecord.id];
		this.dependencyBatchService.includeBatchRecords(this.importBatch.id, ids)
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
	private onUpdate(): void {
		let newFieldsValues: Array<{fieldName: string, value: string}> = [];
		for (let field of this.fieldsInfo) {
			if (field.overridedValue) {
				const newFieldValue = {fieldName: field.name, value: field.overridedValue};
				newFieldsValues.push(newFieldValue);
			}
		}
		this.dependencyBatchService.updateBatchRecordFieldsValues(this.importBatch.id, this.batchRecord.id, newFieldsValues)
			.subscribe((result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.loadRecordFieldDetails();
					this.updateSuccessEvent.emit();
				} else {
					this.handleError(result.errors[0] ? result.errors[0] : 'error updating field values');
				}
		}, error => this.handleError(error));
	}

	/**
	 * On Process button click.
	 */
	private onProcess(): void {
		const ids = [this.batchRecord.id];
		this.dependencyBatchService.processBatchRecords(this.importBatch.id, ids)
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
	private showIgnoreButton(): boolean {
		return this.batchRecord.status.code === BatchStatus.PENDING;
	}

	/**
	 * Determine if Include button should be showed on UI.
	 * @returns {boolean}
	 */
	private showIncludeButton(): boolean {
		return this.batchRecord.status.code === BatchStatus.IGNORED;
	}

	/**
	 * Hide Action buttons if Record is already completed.
	 * @returns {boolean}
	 */
	private showActionButtons(): boolean {
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
		this.fieldsFilter.text = '';
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
}