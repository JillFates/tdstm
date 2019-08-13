import { Component } from '@angular/core';
import { UIExtraDialog } from '../../../../../../../shared/services/ui-dialog.service';
import { UIPromptService } from '../../../../../../../shared/directives/ui-prompt.directive';
import {ComboBoxSearchModel} from '../../../../../../../shared/components/combo-box/model/combobox-search-param.model';
import {Observable} from 'rxjs/index';
import {ComboBoxSearchResultModel} from '../../../../../../../shared/components/combo-box/model/combobox-search-result.model';
import {AssetExplorerService} from '../../../../../../assetManager/service/asset-explorer.service';

@Component({
	selector: 'model-device-edit',
	templateUrl: 'model-device-edit.component.html'
})
export class ModelDeviceEditComponent extends UIExtraDialog {
	constructor(private prompt: UIPromptService) {
		super('#model-device-edit-component');
	}

	/***
	 * Close the Active Dialog
	 */
	public cancelCloseDialog(): void {
		this.dismiss();
	}

	/**
	 * On EscKey Pressed close the dialog.
	 */
	onEscKeyPressed(): void {
		this.cancelCloseDialog();
	}

	/**
	 * On Manufacturers combobox change.
	 * @param value
	 */
	protected onManufacturerValueChange(value: any): void {
		if (!value) {
			value = {id: null};
		}

		console.log('On Manufacturer value change:', value);
		// this.model.asset.manufacturerSelectValue = value;
		// this.model.asset.modelSelectValue = {id: null};
	}

	/**
	 * Function that handles the request of the Manufacturers tds-combobox
	 * @param {ComboBoxSearchModel} searchModel
	 * @returns {Observable<ComboBoxSearchResultModel>}
	 */
	protected searchManufacturers = (searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> => {
		// searchModel.query = `assetType=${this.model.asset.assetTypeSelectValue.id ? this.model.asset.assetTypeSelectValue.id : ''}`;
		// return this.assetExplorerService.getManufacturersForComboBox(searchModel);
		return null;
	}
}