
import {Component, Inject, OnInit} from '@angular/core';
import {ImportBatchModel, ImportBatchRecordDetailColumnsModel} from '../../model/import-batch.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {DependencyBatchService} from '../../service/dependency-batch.service';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {SelectableSettings} from '@progress/kendo-angular-grid';
import {DataGridOperationsHelper} from '../dependency-batch-list/data-grid-operations.helper';

@Component({
	selector: 'dependency-batch-detail-dialog',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-detail-dialog/dependency-batch-detail-dialog.component.html'
})
export class DependencyBatchDetailDialogComponent implements OnInit {

	private columnsModel: ImportBatchRecordDetailColumnsModel;
	private selectableSettings: SelectableSettings = { mode: 'single', checkboxOnly: false};
	private dataGridOperationsHelper: DataGridOperationsHelper;
	private checkboxSelectionConfig = {
		useColumn: 'id'
	};
	private batchRecords: Array<any> = [];

	constructor(
		private batchModel: ImportBatchModel,
		private promptService: UIPromptService,
		private dependencyBatchService: DependencyBatchService,
		private activeDialog: UIActiveDialogService) {
		this.columnsModel = new ImportBatchRecordDetailColumnsModel();
	}

	ngOnInit(): void {
		this.dataGridOperationsHelper = new DataGridOperationsHelper(this.batchRecords, [], this.selectableSettings, this.checkboxSelectionConfig);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}
}