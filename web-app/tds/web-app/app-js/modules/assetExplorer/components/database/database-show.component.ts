import { Component } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';

export function DatabaseShowComponent(template) {
	@Component({
		selector: `database-show`,
		template: template
	}) class DatabaseShowComponent {
		constructor(private activeDialog: UIActiveDialogService) {

		}

		cancelCloseDialog(): void {
			this.activeDialog.close();
		}

	}
	return DatabaseShowComponent;
}