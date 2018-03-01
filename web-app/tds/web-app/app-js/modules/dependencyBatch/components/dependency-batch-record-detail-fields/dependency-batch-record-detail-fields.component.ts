import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {ImportBatchRecordModel} from '../../model/import-batch-record.model';

@Component({
	selector: 'dependency-batch-record-detail-fields',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-record-detail-fields/dependency-batch-record-detail-fields.component.html'
})
export class DependencyBatchRecordDetailFieldsComponent implements OnInit {

	@Input('batchRecord') batchRecord: ImportBatchRecordModel;
	@Output('onCancel') cancelEvent = new EventEmitter<any>();

	private fields: Array<any> = [];
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
	 * TODO: document
	 */
	private onCancel(): void {
		this.batchRecord = null;
		this.cancelEvent.emit();
		console.log('fields onCancel');
	}

	/**
	 * TODO: document
	 */
	private loadRecordFieldDetails(): void {
		this.dependencyBatchService.getImportBatchRecordFieldDetail(this.batchRecord.id).subscribe( result => {
			this.fields = result;
		}, error => this.handleError(error) );
	}

	/**
	 * TODO: document
	 * @param e
	 */
	private handleError(e): void {
		console.log(e);
	}
}