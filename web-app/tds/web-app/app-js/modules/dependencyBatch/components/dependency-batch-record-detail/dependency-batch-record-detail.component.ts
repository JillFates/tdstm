import {Component, EventEmitter, Input, Output} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {ImportBatchModel} from '../../model/import-batch.model';

@Component({
	selector: 'dependency-batch-record-detail',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-record-detail/dependency-batch-record-detail.component.html'
})
export class DependencyBatchRecordDetailComponent {

	@Input('importBatch') importBatch: ImportBatchModel;
	@Input('batchRecord') batchRecord: ImportBatchRecordModel;
	@Output('onCancel') cancelEvent = new EventEmitter<any>();

	constructor(private dependencyBatchService: DependencyBatchService) {
	}

	/**
	 * TODO: document
	 */
	private onCancel(): void {
		this.cancelEvent.emit();
		this.batchRecord = null;
	}
}