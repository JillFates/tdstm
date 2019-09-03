import {Component, ViewChild} from '@angular/core';
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
import {AkaChanges} from '../../../../../../../shared/components/aka/model/aka.model';
import {NgForm} from '@angular/forms';

@Component({
	selector: 'model-device-edit',
	templateUrl: 'model-device-edit.component.html'
})
export class ModelDeviceEditComponent extends UIExtraDialog {
	@ViewChild('form') protected form: NgForm;
	private hasAkaValidationErrors = false;
	public model: any;
	public manufacturer = null;
	public dateFormat = '';
	public assetType = null;
	public bladeHeight = ['Half', 'Full'];
	public modelStatus = ['new', 'full', 'valid'];
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
		this.model.manufacturerId = value.id;
		this.onCustomControlChange();
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
		// searchModel.query = `manufacturerId=${this.manufacturer.id}`;
		searchModel.query = `manufacturerId=`;
		return this.assetExplorerService.getAssetTypesForComboBox(searchModel);
	};

	/**
	 * On Asset Types combobox change.
	 * @param value
	 */
	onAssetTypeValueChange(value: any): void {
		this.assetType = value;
		this.model.assetType = this.assetType && this.assetType.id || null;
		this.onCustomControlChange();
	};

	/**
	 * validate input errors
	 */
	onAkaValidationErrors(hasAkaValidationErrors: boolean): void {
		this.hasAkaValidationErrors = hasAkaValidationErrors;
	}

	/**
	 * On changes on aka collection
	 */
	public onAkaChange(akaChanges: AkaChanges): void {
		this.model.akaChanges = akaChanges;
		this.onCustomControlChange();
	}

	public onCustomControlChange(): void {
		if (this.form.controls && this.form.controls.customControls) {
			this.form.controls.customControls.markAsDirty();
		}
	}

	/**
	 * On change connectors
	 */
	public onConnectorsChange(connectors: any): void {
		this.model.connectors = connectors;
		this.onCustomControlChange();
	}

	onSave() {
		const payload = {
			id: this.model.id,
			modelName: this.model.Name,
			assetType: this.model.assetType,
			usize: this.model.usize,
			modelHeight: this.model.modelHeight,
			modelWidth: this.model.modelWidth,
			modelDepth: this.model.modelDepth,
			modelWeight: this.model.modelWeight,
			layoutStyle: this.model.layoutStyle,
			productLine: this.model.productLine,
			modelFamily: this.model.modelFamily,
			endOfLifeStatus: this.model.endOfLifeStatus,
			powerNameplate: this.model.powerNamePlate,
			powerDesign: this.model.powerDesign,
			powerUse: this.model.powerUse,
			powerType: this.model.powerType,
			cpuType: this.model.cpuType,
			cpuCount: this.model.cpuCount,
			bladeRows: this.model.bladeRows || '',
			bladeCount: this.model.bladeCount || '',
			bladeLabelCount: this.model.bladeLabelCount || '',
			bladeHeight: this.model.bladeHeight,
			memorySize: this.model.memorySize,
			storageSize: this.model.storageSize,
			description: this.model.description,
			sourceURL: this.model.sourceURL,
			modelStatus: this.model.modelStatus,
			manufacturerId: this.model.manufacturerId,  // ---------------
			endOfLifeDate: this.model.endOfLifeDate,
			sourceTDS: this.model.sourceTDS ? 1 : 0,
			roomObject: this.model.roomObject ? 1 : 0,
			aka: this.model.akaChanges || [],
			connectors: this.model.connectors || [],
			connectorsCount: 0,
		};

		console.log(payload);
	}

	onPowerChange(power: any) {
		this.onCustomControlChange();
	}

}