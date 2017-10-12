import { Component, Inject } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';

export function DatabaseEditComponent(template, model) {
	@Component({
		selector: `database-edit`,
		template: template,
		providers: [
			{ provide: 'model', useValue: model }
		]
	}) class DatabaseShowComponent {
		constructor(
			@Inject('model') private model: any,
			private activeDialog: UIActiveDialogService) {

		}

		cancelCloseDialog(): void {
			this.activeDialog.close();
		}

	}
	return DatabaseShowComponent;
}