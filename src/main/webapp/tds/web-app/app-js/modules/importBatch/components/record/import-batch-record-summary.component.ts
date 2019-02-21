import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {ImportBatchModel} from '../../model/import-batch.model';
import {ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {PreferenceService, PREFERENCES_LIST, IMPORT_BATCH_PREFERENCES} from '../../../../shared/services/preference.service';
import { DateUtils } from '../../../../shared/utils/date.utils';

@Component({
	selector: 'import-batch-record-summary',
	templateUrl: 'import-batch-record-summary.component.html'
})
export class ImportBatchRecordSummaryComponent {

	@Input('importBatch') importBatch: ImportBatchModel;
	@Input('batchRecord') batchRecord: ImportBatchRecordModel;

	public importBatchPreferences = {};
	public importBatchPrefEnum = IMPORT_BATCH_PREFERENCES;
	public userTimeZone: string;

	constructor(private userPreferenceService: PreferenceService) {
		this.onLoad();
	}

	/**
	 * On Page Load
	 */
	private onLoad(): void {
		// Fetch the user preferences for their TimeZone
		this.userTimeZone = this.userPreferenceService.getUserTimeZone();
		this.userPreferenceService.getSinglePreference(PREFERENCES_LIST.IMPORT_BATCH_PREFERENCES).subscribe( res => {
			if (res) {
					this.importBatchPreferences = JSON.parse(res);
					if (this.importBatchPreferences[IMPORT_BATCH_PREFERENCES.TWISTIE_COLLAPSED] == undefined) {
						this.importBatchPreferences[IMPORT_BATCH_PREFERENCES.TWISTIE_COLLAPSED] = false;
					}
				} else {
				this.importBatchPreferences[IMPORT_BATCH_PREFERENCES.TWISTIE_COLLAPSED] = false;
			}
			}, (error) => { console.error(error) });
	}

	/**
	 * Returns true if batch record got errors.
	 */
	public batchHasErrors(): boolean {
		return this.batchRecord.errorCount > 0 || (this.batchRecord.errorList && this.batchRecord.errorList.length > 0)
	}

	public toggleSummary(): void {
		this.importBatchPreferences[IMPORT_BATCH_PREFERENCES.TWISTIE_COLLAPSED] = !this.importBatchPreferences[IMPORT_BATCH_PREFERENCES.TWISTIE_COLLAPSED];
		this.userPreferenceService.setPreference(PREFERENCES_LIST.IMPORT_BATCH_PREFERENCES, JSON.stringify(this.importBatchPreferences)).subscribe( r => { /**/});
	}
}