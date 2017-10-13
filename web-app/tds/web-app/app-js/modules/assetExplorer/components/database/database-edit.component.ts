import { Component, Inject, OnInit} from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';

declare var jQuery: any;

export function DatabaseEditComponent(template, editModel) {
	@Component({
		selector: `database-edit`,
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	}) class DatabaseShowComponent implements OnInit {
		constructor(
			@Inject('model') private model: any,
			private activeDialog: UIActiveDialogService) {
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