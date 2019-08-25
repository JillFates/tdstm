import { Component } from '@angular/core';
import { clone } from 'ramda';

import { UIExtraDialog } from '../../../../../../../shared/services/ui-dialog.service';
import { UIPromptService } from '../../../../../../../shared/directives/ui-prompt.directive';
import {ComboBoxSearchModel} from '../../../../../../../shared/components/combo-box/model/combobox-search-param.model';
import {Observable} from 'rxjs/index';
import {ComboBoxSearchResultModel} from '../../../../../../../shared/components/combo-box/model/combobox-search-result.model';
import {AssetExplorerService} from '../../../../../../assetManager/service/asset-explorer.service';
import {DeviceModel} from '../../model/device-model.model';
import {DeviceManufacturer} from '../../../manufacturer/model/device-manufacturer.model';
import {PowerModel, PowerUnits} from '../../../../../../../shared/components/power/model/power.model';
import {PreferenceService} from '../../../../../../../shared/services/preference.service';
import {UserContextService} from '../../../../../../auth/service/user-context.service';
import {takeUntil} from 'rxjs/operators';
import {CredentialColumnModel} from '../../../../../../credential/model/credential.model';
import {UserContextModel} from '../../../../../../auth/model/user-context.model';
import {DateUtils} from '../../../../../../../shared/utils/date.utils';

@Component({
	selector: 'model-device-edit',
	templateUrl: 'model-device-edit.component.html'
})
export class ModelDeviceEditComponent extends UIExtraDialog {
	public model: any;
	public manufacturer = null;
	public dateFormat = '';
	public assetType = null;
	public bladeHeight = ['Half', 'Full'];
	public usize: number[] = [];
	public powerUnits = PowerUnits;
	public powerModel: PowerModel;
	constructor(
		private assetExplorerService: AssetExplorerService,
		// private preferenceService: PreferenceService,
		private prompt: UIPromptService,
		private deviceModel: DeviceModel,
		// private userContext: UserContextService,
		private deviceManufacturer: DeviceManufacturer) {
		super('#model-device-edit-component');
		console.log(deviceModel);
		this.model = clone(deviceModel);

		this.manufacturer = {
			id: this.deviceManufacturer.id,
			text: this.deviceManufacturer.name
		};
		this.assetType = {
			id: this.deviceModel.assetType,
			text: this.deviceModel.assetType
		};
		this.powerModel = {
			namePlate: this.model.powerNameplate,
			design: this.model.powerDesign,
			use: this.model.powerUse,
			unit: this.model.powerType
		};

		this.model.endOfLifeDate =  DateUtils.stringDateToDate(this.model.endOfLifeDate);

		/*
		this.model.endOfLifeDate = this.model.endOfLifeDate ? new Date(this.model.endOfLifeDate) : null;
		this.preferenceService.getUserDatePreferenceAsKendoFormat()
			.subscribe((format) => {
				console.log(format);
				this.dateFormat = format;
			});
		*/

		this.usize = Array.from(Array(53).keys()).filter((num) => num > 0);
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
		searchModel.query = `assetType=${this.deviceModel.assetType || ''}`;
		return this.assetExplorerService.getManufacturersForComboBox(searchModel);
	}

	/**
	 * Function that handles the request of the Asset Types tds-combobox
	 * @param {ComboBoxSearchModel} searchModel
	 * @returns {Observable<ComboBoxSearchResultModel>}
	 */
	searchAssetTypes = (searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel>  => {
		searchModel.query = `manufacturerId=${this.manufacturer.id}`;
		return this.assetExplorerService.getAssetTypesForComboBox(searchModel);
	};

	/**
	 * On Asset Types combobox change.
	 * @param value
	 */
	onAssetTypeValueChange(value: any): void {
		console.log('on asset type value change');
	};

}