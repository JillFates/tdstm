import { Component, Inject, OnInit } from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';
import { PreferenceService } from '../../../../shared/services/preference.service';
import { DateUtils} from '../../../../shared/utils/date.utils';
import * as R from 'ramda';

declare var jQuery: any;

export function DatabaseEditComponent(template, editModel) {

	@Component({
		selector: `database-edit`,
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	}) class DatabaseShowComponent implements OnInit {

		dateFormat: string;

		constructor(
			@Inject('model') private model: any,
			private activeDialog: UIActiveDialogService,
			private preference: PreferenceService) {
			this.dateFormat = this.preference.preferences['CURR_DT_FORMAT'];
			this.dateFormat = this.dateFormat.toLowerCase().replace(/m/g, 'M');

			this.model.asset = R.clone(editModel.asset);
			this.model.asset.retireDate = DateUtils.compose(this.model.asset.retireDate);
			this.model.asset.maintExpDate = DateUtils.compose(this.model.asset.maintExpDate);

			// TODO: Create Object Util to initialiaze variables based on their class model definition
			if (this.model.asset.scale === null) {
				this.model.asset.scale = {
					name: ''
				};
			}
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