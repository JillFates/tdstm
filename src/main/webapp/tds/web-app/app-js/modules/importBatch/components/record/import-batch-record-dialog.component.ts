// Angular
import {AfterViewInit, ChangeDetectorRef, Component, Input, OnInit, ViewChild} from '@angular/core';
// Model
import {ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {ImportBatchModel} from '../../model/import-batch.model';
import {PROMPT_DEFAULT_MESSAGE_KEY, PROMPT_DEFAULT_TITLE_KEY} from '../../../../shared/model/constants';
import {Dialog, DialogButtonType} from 'tds-component-library';
// Component
import {ImportBatchRecordFieldsComponent} from './import-batch-record-fields.component';
// Service
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'import-batch-record-dialog',
	templateUrl: 'import-batch-record-dialog.component.html'
})
export class ImportBatchRecordDialogComponent extends Dialog implements OnInit, AfterViewInit {
	@Input() data: any;

	@ViewChild('detailFieldsComponent', {static: false}) detailFieldsComponent: ImportBatchRecordFieldsComponent;
	private batchRecordUpdatedFlag = false;
	public importBatch: ImportBatchModel = null;
	public batchRecord: ImportBatchRecordModel = null;

	constructor(
		private promptService: UIPromptService,
		private translatePipe: TranslatePipe,
		private cdr: ChangeDetectorRef) {
			super();
	}

	/**
	 * On Component Init get Import Batch Records.
	 */
	async ngOnInit(): Promise<void> {
		// Get Modal Model
		this.importBatch = Object.assign({}, this.data.importBatchModel);
		this.batchRecord = Object.assign({}, this.data.importBatchRecordModel);

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			tooltipText: 'Save',
			show: () => this.showActionButtons(),
			disabled: () => !this.detailFieldsComponent.areOverrideValuesDirty(),
			type: DialogButtonType.ACTION,
			action: this.onSaveClicked.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			tooltipText: 'Close',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.onCancelCloseDialog.bind(this)
		});
	}

	ngAfterViewInit() {
		this.cdr.detectChanges();
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
					this.onAcceptSuccess({reloadRecords: this.batchRecordUpdatedFlag});
				}
			}, (reason: any) => console.log('confirm rejected', reason));
		} else {
			this.onAcceptSuccess({reloadRecords: this.batchRecordUpdatedFlag});
		}
	}

	/**
	 * On save button clicked.
	 */
	public onSaveClicked(): void {
		this.detailFieldsComponent.onUpdate();
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

	protected showActionButtons(): boolean {
		return this.detailFieldsComponent ? this.detailFieldsComponent.showActionButtons() : false;
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.onCancelCloseDialog();
	}
}
