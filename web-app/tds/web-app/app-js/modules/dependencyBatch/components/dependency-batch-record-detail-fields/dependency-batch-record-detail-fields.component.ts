import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {BatchStatus, ImportBatchModel} from '../../model/import-batch.model';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {CompositeFilterDescriptor, FilterDescriptor, process, State} from '@progress/kendo-data-query';
import {GridDataResult} from '@progress/kendo-angular-grid';

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
			logic: 'or'
		}
	};
	private gridData: GridDataResult;
	private fieldsFilter: any = {
		options: [
			{text: 'All', value: 1},
			{text: 'With Errors', value: 2}
			],
		selected: {text: 'All', value: 1},
		text: ''
	};

	constructor(
		private dependencyBatchService: DependencyBatchService) {
		// Silence is golden
	}

	/**
	 * On Init Load Record Field details.
	 */
	ngOnInit(): void {
		this.loadRecordFieldDetails();
	}

	/**
	 * On Changes detected Batch Record selection reload it's Fields info.
	 * @param {SimpleChanges} changes
	ngOnChanges(changes: SimpleChanges): void {
		this.loadRecordFieldDetails();
	}
	 */

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
				currentValue: fields[fieldName].originalValue,
				importValue: fields[fieldName].value,
				error: fields[fieldName].error,
				overridedValue: null
			});
		}
		this.gridData = process(this.fieldsInfo, this.state);
	}

	/**
	 * On Update button click.
	 */
	private onUpdate(): void {
		let newFieldsValues: Array<{fieldName: string, value: string}> = [];
		// for (let field in this.fieldsInfo) {
		// 	if (this.fieldsInfo.hasOwnProperty(field) && this.fieldsInfo.overridedValue) {
		// 		// console.log(this.fieldsInfo[field]);
		// 		const newFieldValue = {fieldName: field, value: this.fieldsInfo.overridedValue};
		// 		newFieldsValues.push(newFieldValue);
		// 	}
		// }
		// this.dependencyBatchService.updateBatchRecordFieldsValues(this.importBatch.id, this.batchRecord.id, newFieldsValues)
		// 	.subscribe((result: ApiResponseModel) => {
		// 		if (result.status === ApiResponseModel.API_SUCCESS) {
		// 			this.updateSuccessEvent.emit();
		// 		} else {
		// 			this.handleError(result.errors[0] ? result.errors[0] : 'error updating field values');
		// 		}
		// }, error => this.handleError(error));
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

	private onTextFilter(): void {
		let foundedMatch = this.state.filter.filters.find((r: any) => r.field === 'name');
		if (foundedMatch) {
			(foundedMatch as FilterDescriptor).value = this.fieldsFilter.text;
		} else {
			let nameFilter: FilterDescriptor = {
				field: 'name',
				value: this.fieldsFilter.text,
				operator: 'contains',
				ignoreCase: true
			};
			this.state.filter.filters = [nameFilter];
		}
		this.gridData = process(this.fieldsInfo, this.state);
	}

	private clearTextFilter(): void {
		this.fieldsFilter.text = '';
		this.onTextFilter();
		// const filterIndex = this.state.filter.filters.findIndex((r: any) => r.field === 'name');
		// this.state.filter.filters.splice(filterIndex, 1);
		// this.gridData = process(this.fieldsInfo, this.state);
	}
}