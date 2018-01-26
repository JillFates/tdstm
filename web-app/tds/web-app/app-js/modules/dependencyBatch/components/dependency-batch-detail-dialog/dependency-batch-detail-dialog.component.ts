
import {Component, OnInit} from '@angular/core';
import {DependencyBatchModel} from '../../model/dependency-batch.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {DependencyBatchService} from '../../service/dependency-batch.service';

@Component({
	selector: 'dependency-batch-detail-dialog',
	templateUrl: '../tds/web-app/app-js/modules/depedendencyBatch/components/dependency-batch-detail-dialog/dependency-batch-detail.component.html'
})
export class DependencyBatchDetailDialogComponent implements OnInit {

	// public dependencyBatchModel: DependencyBatchModel;

	constructor(
		private dependencyBatchModel: DependencyBatchModel,
		private promptService: UIPromptService,
		private dependencyBatchService: DependencyBatchService) {
	}

	ngOnInit(): void {
		// silence is golden.
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	protected cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel').then(result => {
				if (result) {
					this.activeDialog.dismiss();
				}
			});
		} else {
			this.activeDialog.dismiss();
		}
	}
	 */
}