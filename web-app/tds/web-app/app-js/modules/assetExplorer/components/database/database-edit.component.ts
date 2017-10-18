import { Component, Inject, OnInit } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';
import { CleanObjectPipe } from '../../../../shared/pipes/clean-object.pipe';
import { DATABASE_DEFAULT_VALUES, DatabaseModel } from './database.model';
declare var jQuery: any;

export function DatabaseEditComponent(template, editModel) {
	const cleanPipe = new CleanObjectPipe();
	editModel.asset = cleanPipe.transform(editModel.asset);
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
			this.model.asset = Object.assign({}, DATABASE_DEFAULT_VALUES, this.model.asset) as DatabaseModel;
			console.log(this.model.asset);
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