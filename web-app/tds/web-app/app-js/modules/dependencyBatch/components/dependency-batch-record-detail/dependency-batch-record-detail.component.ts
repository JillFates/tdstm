import {Component, EventEmitter, Input, Output} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {ImportBatchModel} from '../../model/import-batch.model';
import {UIDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'dependency-batch-record-detail',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-record-detail/dependency-batch-record-detail.component.html'
})
export class DependencyBatchRecordDetailComponent extends UIExtraDialog {

	// @Input('importBatch') importBatch: ImportBatchModel;
	// @Input('batchRecord') batchRecord: ImportBatchRecordModel;
	// @Output('onCancel') cancelEvent = new EventEmitter<any>();

	constructor(
		private dialogService: UIDialogService,
		private importBatch: ImportBatchModel,
		private batchRecord: ImportBatchRecordModel,
		private dependencyBatchService: DependencyBatchService) {
			super('#dependency-batch-record-detail');
	}

	/**
	 * TODO: document
	 */
	private onCancelCloseDialog(): void {
		// this.cancelEvent.emit();
		// this.batchRecord = null;
		this.dismiss();
	}
}