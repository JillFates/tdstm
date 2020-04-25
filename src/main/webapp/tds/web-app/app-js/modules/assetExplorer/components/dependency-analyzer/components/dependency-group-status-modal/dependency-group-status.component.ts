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

@Component({
	selector: 'dependency-group-status-modal',
	templateUrl: 'dependency-group-status.component.html'
})
export class DependencyGroupStatusComponent extends Dialog implements OnInit {
	@Input() data: any;
	public modalTitle: string;

	constructor(
		private translatePipe: TranslatePipe,
	) {
		super();
	}

	ngOnInit(): void {
		// empty block
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
