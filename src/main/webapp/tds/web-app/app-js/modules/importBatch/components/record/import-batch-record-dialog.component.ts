import {Component, ViewChild} from '@angular/core';
import {ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {ImportBatchModel} from '../../model/import-batch.model';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {ImportBatchRecordFieldsComponent} from './import-batch-record-fields.component';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {KEYSTROKE} from '../../../../shared/model/constants';
import {PROMPT_DEFAULT_MESSAGE_KEY, PROMPT_DEFAULT_TITLE_KEY} from '../../../../shared/model/constants';
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';

@Component({
	selector: 'import-batch-record-dialog',
	templateUrl: 'import-batch-record-dialog.component.html',
	host: {
		'(keydown)': 'keyDownHandler($event)'
	}
})
export class ImportBatchRecordDialogComponent extends UIExtraDialog {

	@ViewChild('detailFieldsComponent') detailFieldsComponent: ImportBatchRecordFieldsComponent;
	private batchRecordUpdatedFlag = false;
	public modalOptions: DecoratorOptions;
	public isWindowMaximized;

	constructor(
		public importBatch: ImportBatchModel,
		public batchRecord: ImportBatchRecordModel,
		private promptService: UIPromptService,
		private translatePipe: TranslatePipe) {
			super('#import-batch-record-dialog');
			this.isWindowMaximized = false;
			this.modalOptions = { isFullScreen: true, isResizable: true, isDraggable: true };
	}

	/**
	 * On close dialog.
	 */
	public onCancelCloseDialog(): void {
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

	/**
	 * Maximizes windows to fullscreen.
	 */
	protected maximizeWindow() {
		this.isWindowMaximized = true;
	}

	/**
	 * Resets windows default size.
	 */
	protected restoreWindow() {
		this.isWindowMaximized = false;
	}
}