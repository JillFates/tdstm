// Angular
import {
	Component,
	OnInit,
	Input
} from '@angular/core';
// Model

// Component

// Service

import {Dialog} from 'tds-component-library';
// Other
import {TranslatePipe} from '../../../../../../shared/pipes/translate.pipe';
import {DependencyAnalyzerService} from '../../service/dependency-analyzer.service';

@Component({
	selector: 'regenerate-progress-dialog',
	templateUrl: 'regenerate-progress-dialog.component.html'
})
export class RegenerateProgressDialogComponent extends Dialog implements OnInit {
	@Input() data: any;
	public modalTitle: string;
	public statusKey;
	public value = 0;

	constructor(
		private translatePipe: TranslatePipe,
		private dependencyAnalyzerService: DependencyAnalyzerService
	) {
		super();
	}

	ngOnInit(): void {
		if (this.data) {
			this.statusKey = this.data.regenKey;
			this.checkData();
		}
	}

	/**
	 * Gets the status of the data that's being processed
	 * Calls itself every half second to get the status
	 * */
	checkData() {
		this.dependencyAnalyzerService.getGeneratedDataStatus(this.statusKey).subscribe( (response: any) => {
		this.value = response.data.percentComp;
		if (this.value < 100) {
			setTimeout( () => {
				this.checkData();
			}, 500);
		}
		});
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		this.onCancelClose();
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
