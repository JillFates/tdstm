import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {ImportBatchModel} from '../../model/import-batch.model';
import {ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {PreferenceService} from '../../../../shared/services/preference.service';

@Component({
	selector: 'dependency-batch-record-detail-summary',
	templateUrl: '../tds/web-app/app-js/modules/dependencyBatch/components/dependency-batch-record-detail-summary/dependency-batch-record-detail-summary.component.html'
})
export class DependencyBatchRecordDetailSummaryComponent {

	@Input('importBatch') importBatch: ImportBatchModel;
	@Input('batchRecord') batchRecord: ImportBatchRecordModel;

	protected summaryCollapsed = false;

	constructor(private userPreferenceService: PreferenceService) {
		// Silence is golden
	}

	/**
	 * Returns true if batch record got errors.
	 */
	protected batchHasErrors(): boolean {
		return this.batchRecord.errorCount > 0 || (this.batchRecord.errorList && this.batchRecord.errorList.length > 0)
	}

	protected toggleSummary(): void {
		this.summaryCollapsed = !this.summaryCollapsed;
	}
}