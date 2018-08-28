import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {Component} from '@angular/core';
import {PROMPT_DEFAULT_MESSAGE_KEY, PROMPT_DEFAULT_TITLE_KEY} from '../../../../shared/model/constants';

@Component({
	selector: 'import-batch-record-fields-info',
	templateUrl: '../tds/web-app/app-js/modules/importBatch/components/record/import-batch-record-fields-info.component.html'
})
export class ImportBatchRecordFieldsInfoComponent extends UIExtraDialog {

	constructor() {
		super('#import-batch-record-fields-info');
	}

	/**
	 * On close dialog.
	 */
	protected onCancelCloseDialog(): void {
		this.close();
	}
}