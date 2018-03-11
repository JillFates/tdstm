import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {BatchStatus, ImportBatchModel} from '../../model/import-batch.model';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {CompositeFilterDescriptor, FilterDescriptor, process, State} from '@progress/kendo-data-query';
import {GridDataResult} from '@progress/kendo-angular-grid';
import {ValidationUtils} from '../../../../shared/utils/validation.utils';

@Component({
	selector: 'dependency-batch-record-detail-fields',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-record-detail-fields/dependency-batch-record-detail-fields.component.html'
})
export class DependencyBatchRecordDetailFieldsComponent implements OnInit {

	@Input('importBatch') importBatch: ImportBatchModel;
	@Input('batchRecord') batchRecord: ImportBatchRecordModel;
	@Output('onCancel') cancelEvent = new EventEmitter<any>();
	@Output('onUpdateSuccess') updateSuccessEvent = new EventEmitter<any>();

	private fieldsInfo: Array<{
		name: string,
		currentValue: string,
		importValue: string,
		error: boolean,
		overridedValue: string
	}> = [];
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
		}
	};

	constructor(
		private dependencyBatchService: DependencyBatchService) {
			this.state.filter.filters.push(this.fieldsFilter.nameFilter);
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
	private loadRecordFieldDetails(): void {
		this.dependencyBatchService.getImportBatchRecordFieldDetail(this.batchRecord.importBatch.id, this.batchRecord.id)
			.subscribe( (result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				this.buildGridData(result.data.fieldsInfo);
			} else {
				this.handleError(result.errors[0] ? result.errors[0] : 'error calling endpoint');
			}
		}, error => {
			this.fieldsInfo = [];
			this.handleError(error);
		});
	}

	private buildGridData(fields): void {
		// let data: Array<{name: string, currentValue: string, importValue: string, error: boolean}> = [];
		for (const fieldName of this.importBatch.fieldNameList) {
			this.fieldsInfo.push({
				name: fieldName,
				currentValue: !ValidationUtils.isEmptyObject(fields[fieldName].originalValue)
					? fields[fieldName].originalValue : '(null)',
				importValue: !ValidationUtils.isEmptyObject(fields[fieldName].value)
					? fields[fieldName].value : '(null)',
				error: fields[fieldName].error,
				overridedValue: null
			});
		}
		this.gridData = process(this.fieldsInfo, this.state);
	}

	/**
	 * Checks if input overrided values are not empty or with a text value.
	 */
	private areOverrideValuesDirty(): boolean {
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
					this.updateSuccessEvent.emit();
				} else {
					this.handleError(result.errors[0] ? result.errors[0] : 'error updating field values');
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
	 * On Text Filter input change it's value.
	 */
	private onTextFilter(): void {
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
		// this.batchRecord = null;
		this.cancelEvent.emit();
	}

	/**
	 * Handles any API error and displays it on UI.
	 * @param e
	 */
	private handleError(e): void {
		console.log(e);
	}
}