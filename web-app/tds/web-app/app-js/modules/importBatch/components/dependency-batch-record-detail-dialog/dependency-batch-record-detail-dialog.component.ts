import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {BatchStatus, ImportBatchModel} from '../../model/import-batch.model';
import {UIDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {DependencyBatchRecordDetailFieldsComponent} from '../dependency-batch-record-detail-fields/dependency-batch-record-detail-fields.component';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {PROMPT_DEFAULT_MESSAGE_KEY, PROMPT_DEFAULT_TITLE_KEY} from '../../../../shared/model/constants';

@Component({
	selector: 'dependency-batch-record-detail',
	templateUrl: '../tds/web-app/app-js/modules/importBatch/components/dependency-batch-record-detail-dialog/dependency-batch-record-detail-dialog.component.html'
})
export class DependencyBatchRecordDetailDialogComponent extends UIExtraDialog {

	@ViewChild('detailFieldsComponent') detailFieldsComponent: DependencyBatchRecordDetailFieldsComponent;
	private batchRecordUpdatedFlag = false;

	constructor(
		private importBatch: ImportBatchModel,
		private batchRecord: ImportBatchRecordModel,
		private promptService: UIPromptService,
		private translatePipe: TranslatePipe) {
			super('#dependency-batch-record-detail');
	}

	/**
	 * On close dialog.
	 */
	private onCancelCloseDialog(): void {
		if (this.detailFieldsComponent.areOverrideValuesDirty()) {
			this.promptService.open(
				this.translatePipe.transform(PROMPT_DEFAULT_TITLE_KEY),
				this.translatePipe.transform(PROMPT_DEFAULT_MESSAGE_KEY),
				'Confirm', 'Cancel').then(result => {
				if (result) {
					this.close(this.batchRecordUpdatedFlag ? 'reload' : null);
				}
			}, (reason: any) => console.log('confirm rejected', reason));
		} else {
			this.close(this.batchRecordUpdatedFlag ? 'reload' : null);
		}
	}

	/**
	 * On Fields Values updated successfully.
	 */
	private onUpdateSuccess(): void {
		this.batchRecordUpdatedFlag = true;
	}

	/**
	 * On Fields Values updated successfully.
	 */
	private onBatchRecordUpdated($event): void {
		this.batchRecord = $event.batchRecord;
	}
}