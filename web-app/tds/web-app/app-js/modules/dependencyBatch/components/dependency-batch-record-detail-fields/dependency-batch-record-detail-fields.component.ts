import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {ImportBatchModel} from '../../model/import-batch.model';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'dependency-batch-record-detail-fields',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-record-detail-fields/dependency-batch-record-detail-fields.component.html'
})
export class DependencyBatchRecordDetailFieldsComponent implements OnInit, OnChanges {

	@Input('importBatch') importBatch: ImportBatchModel;
	@Input('batchRecord') batchRecord: ImportBatchRecordModel;
	@Output('onCancel') cancelEvent = new EventEmitter<any>();
	@Output('onUpdateSuccess') updateSuccessEvent = new EventEmitter<any>();

	private fieldsInfo: Array<any>;
	private fieldsFilter: any = {
		options: [
			{text: 'All', value: 1},
			{text: 'With Errors', value: 2}
			],
		selected: {text: 'All', value: 1}
	};

	constructor(
		private dependencyBatchService: DependencyBatchService) {
		// Silence is golden
	}

	ngOnInit(): void {
		this.loadRecordFieldDetails();
	}

	/**
	 * On Changes detected Batch Record selection reload it's Fields info.
	 * @param {SimpleChanges} changes
	 */
	ngOnChanges(changes: SimpleChanges): void {
		this.loadRecordFieldDetails();
	}

	/**
	 * On Cancel.
	 */
	private onCancel(): void {
		// this.batchRecord = null;
		this.cancelEvent.emit();
	}

	/**
	 * Gets the Batch Record Field Details from API.
	 */
	private loadRecordFieldDetails(): void {
		this.dependencyBatchService.getImportBatchRecordFieldDetail(this.batchRecord.importBatch.id, this.batchRecord.id).subscribe( (result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				this.fieldsInfo = result.data.fieldsInfo;
			} else {
				this.handleError(result.errors[0] ? result.errors[0] : 'error calling endpoint');
			}
		}, error => {
			this.fieldsInfo = [];
			this.handleError(error);
		});
	}

	/**
	 * On Update button click.
	 */
	private onUpdate(): void {
		let newFieldsValues: Array<{fieldName: string, value: string}> = [];
		for (let field in this.fieldsInfo) {
			if (this.fieldsInfo.hasOwnProperty(field) && this.fieldsInfo[field].overridedValue) {
				// console.log(this.fieldsInfo[field]);
				const newFieldValue = {fieldName: field, value: this.fieldsInfo[field].overridedValue};
				newFieldsValues.push(newFieldValue);
			}
		}
		console.log(newFieldsValues);
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
	 * Handles any API error and displays it on UI.
	 * @param e
	 */
	private handleError(e): void {
		console.log(e);
	}
}