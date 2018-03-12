import {Component, EventEmitter, Input, Output} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {BatchStatus, ImportBatchModel} from '../../model/import-batch.model';
import {UIDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'dependency-batch-record-detail',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-record-detail-dialog/dependency-batch-record-detail-dialog.component.html'
})
export class DependencyBatchRecordDetailDialogComponent extends UIExtraDialog {

	private batchRecordUpdatedFlag = false;

	constructor(
		private importBatch: ImportBatchModel,
		private batchRecord: ImportBatchRecordModel,
		private dependencyBatchService: DependencyBatchService) {
			super('#dependency-batch-record-detail');
	}

	/**
	 * On close dialog.
	 */
	private onCancelCloseDialog(): void {
		this.close(this.batchRecordUpdatedFlag ? 'reload' : null);
	}

	/**
	 * On Fields Values updated successfully.
	 */
	private onUpdateSuccess(): void {
		this.batchRecordUpdatedFlag = true;
		// this.close('reload');
	}

	/**
	 * On Fields Values updated successfully.
	 */
	private onBatchRecordUpdated($event): void {
		this.batchRecord = $event.batchRecord;
	}
}