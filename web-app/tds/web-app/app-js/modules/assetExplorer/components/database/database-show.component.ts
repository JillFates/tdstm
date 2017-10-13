import { Component, OnInit } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';

declare var jQuery: any;

export function DatabaseShowComponent(template) {
	@Component({
		selector: `database-show`,
		template: template
	}) class DatabaseShowComponent implements OnInit {
		constructor(private activeDialog: UIActiveDialogService) {
			jQuery('[data-toggle="popover"]').popover();
		}

		/**
		 * Initiates The Injected Component
		 */
		ngOnInit(): void {
			jQuery('[data-toggle="popover"]').popover();
		}

		cancelCloseDialog(): void {
			this.activeDialog.close();
		}

	}
	return DatabaseShowComponent;
}