import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {ApiReponseModel} from '../../../../shared/model/ApiReponseModel';
import {ImportBatchModel} from '../../model/import-batch.model';

@Component({
	selector: 'dependency-batch-record-detail-fields',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-record-detail-fields/dependency-batch-record-detail-fields.component.html'
})
export class DependencyBatchRecordDetailFieldsComponent implements OnInit, OnChanges {

	@Input('importBatch') importBatch: ImportBatchModel;
	@Input('batchRecord') batchRecord: ImportBatchRecordModel;
	@Output('onCancel') cancelEvent = new EventEmitter<any>();

	private fieldsInfo: Array<any>;
	private fieldsFilter: any = {
		options: [
			{text: 'All', value: 1},
			{text: 'With Errors', value: 2}
			],
		selected: {text: 'All', value: 1}
	};

	constructor(private dependencyBatchService: DependencyBatchService) {
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
		this.dependencyBatchService.getImportBatchRecordFieldDetail(this.batchRecord.importBatch.id, this.batchRecord.id).subscribe( (result: ApiReponseModel) => {
			if (result.status === ApiReponseModel.API_SUCCESS) {
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
	 * Handles any API error and displays it on UI.
	 * @param e
	 */
	private handleError(e): void {
		console.log(e);
	}
}