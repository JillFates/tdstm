import { Component } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';

export function ApplicationShowComponent(template) {
	@Component({
		selector: `database-show`,
		template: template
	}) class ApplicationShowComponent {
		constructor(private activeDialog: UIActiveDialogService) {

		}

		cancelCloseDialog(): void {
			this.activeDialog.close();
		}

	}
	return ApplicationShowComponent;
}