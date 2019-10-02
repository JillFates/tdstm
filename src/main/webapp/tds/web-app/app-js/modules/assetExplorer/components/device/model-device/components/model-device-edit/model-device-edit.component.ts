import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import { clone } from 'ramda';

import {UIExtraDialog} from '../../../../../../../shared/services/ui-dialog.service';
import { UIPromptService } from '../../../../../../../shared/directives/ui-prompt.directive';
import {ComboBoxSearchModel} from '../../../../../../../shared/components/combo-box/model/combobox-search-param.model';
import {Observable} from 'rxjs/index';
import {ComboBoxSearchResultModel} from '../../../../../../../shared/components/combo-box/model/combobox-search-result.model';
import {AssetExplorerService} from '../../../../../../assetManager/service/asset-explorer.service';
import {DeviceModel} from '../../model/device-model.model';
import {DeviceManufacturer} from '../../../manufacturer/model/device-manufacturer.model';
import {PowerModel, PowerUnits} from '../../../../../../../shared/components/power/model/power.model';
import {Permission} from '../../../../../../../shared/model/permission.model';
import {PreferenceService} from '../../../../../../../shared/services/preference.service';
import {UserContextService} from '../../../../../../auth/service/user-context.service';
import {takeUntil} from 'rxjs/operators';
import {CredentialColumnModel} from '../../../../../../credential/model/credential.model';
import {UserContextModel} from '../../../../../../auth/model/user-context.model';
import {DateUtils} from '../../../../../../../shared/utils/date.utils';
import {AkaChanges} from '../../../../../../../shared/components/aka/model/aka.model';
import {NgForm} from '@angular/forms';
import {PermissionService} from '../../../../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../../../../shared/pipes/translate.pipe';
import {ModelService} from '../../../../../service/model.service';
import {ConnectorComponent} from '../../../../../../../shared/components/connector/connector.component';
import {AkaComponent} from '../../../../../../../shared/components/aka/aka.component';

@Component({
	selector: 'model-device-edit',
	templateUrl: 'model-device-edit.component.html'
})
export class ModelDeviceEditComponent extends UIExtraDialog implements OnInit, AfterViewInit {
	@ViewChild('form', {static: false}) protected form: NgForm;
	@ViewChild(ConnectorComponent, {static: false}) protected connectors: ConnectorComponent;
	@ViewChild(AkaComponent, {static: false}) protected akas: AkaComponent;
	private hasAkaValidationErrors = false;
	public hasDeleteModelPermission: boolean;
	public hasEditModelPermission: boolean;
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
		private modelService: ModelService,
		private permissionService: PermissionService,
		private translatePipe: TranslatePipe,
		private assetExplorerService: AssetExplorerService,
		private prompt: UIPromptService,
		private deviceModel: DeviceModel,
		private deviceManufacturer: DeviceManufacturer) {
		super('#model-device-edit-component');
		console.log(deviceModel);
		this.model = clone(deviceModel);

		this.manufacturer = {
			id: this.deviceManufacturer.id,
			text: this.deviceManufacturer.name
		};
		this.model.manufacturerName = this.deviceManufacturer.name;

		this.assetType = {
			id: this.deviceModel.assetType,
			text: this.deviceModel.assetType
		};
		this.powerModel = {
			powerNameplate: this.model.powerNameplate,
			design: this.model.powerDesign,
			use: this.model.powerUse,
			unit: this.model.powerType
		};

		this.model.endOfLifeDate =  DateUtils.stringDateToDate(this.model.endOfLifeDate);
		this.usize = Array.from(Array(53).keys()).filter((num) => num > 0);
	}

	ngAfterViewInit() {
		this.connectors.reportChanges();
	}

	ngOnInit() {
		this.hasDeleteModelPermission = this.permissionService.hasPermission(Permission.ModelDelete);
		this.hasEditModelPermission = this.permissionService.hasPermission(Permission.ModelEdit);
	}

	/**
	 * On cancel edition show a prompt to the user, this action will loose the changes
	 */
	protected cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.prompt.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'),
			)
			.then(confirm => {
				if (confirm) {
					this.dismiss();
				}
			})
			.catch((error) => console.log(error));
		} else {
			this.dismiss();
		}
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
		this.model.manufacturerName = value.text;
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

	/**
	 * On custom controls changes mark the state as dirty
	 */
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

	/**
	 * Build the payload and call the endpoint to persist the changes
	 */
	onSave() {
		const payload = {
			aka: this.model.akaChanges || [],
			assetType: this.model.assetType,
			bladeCount: this.model.bladeCount || 0,
			bladeHeight: this.model.bladeHeight,
			bladeLabelCount: this.model.bladeLabelCount || 0,
			bladeRows: this.model.bladeRows || 0,
			connectors: this.model.connectors || [],
			connectorsCount: 0,
			cpuCount: this.model.cpuCount,
			cpuType: this.model.cpuType,
			description: this.model.description,
			endOfLifeDate: this.model.endOfLifeDate,
			endOfLifeStatus: this.model.endOfLifeStatus,
			id: this.model.id,
			layoutStyle: this.model.layoutStyle,
			manufacturer: this.model.manufacturerId,
			memorySize: this.model.memorySize,
			depth: this.model.modelDepth,
			modelFamily: this.model.modelFamily,
			height: this.model.modelHeight,
			modelName: this.model.modelName,
			modelStatus: this.model.modelStatus,
			weight: this.model.modelWeight,
			width: this.model.modelWidth,
			powerDesign: this.model.powerDesign,
			powerNameplate: this.model.powerNameplate,
			powerType: this.model.powerType,
			powerUse: this.model.powerUse,
			productLine: this.model.productLine,
			roomObject: this.model.roomObject,
			sourceTDS: this.model.sourceTDS ? 1 : 0,
			sourceURL: this.model.sourceURL,
			storageSize: this.model.storageSize,
			usize: this.model.usize,
			useImage: 0
		};

		this.modelService.updateModel(payload)
			.subscribe((response: any) => {
				if (response && response.status !== 'error') {
					this.close(payload);
				}
			});
	}

	/**
	 * Changing values in the power time notify the change to the custom controls
	 * @param power
	 */
	onPowerChange(power: any) {
		if (power.unit) {
			this.model.powerNameplate = power.powerNameplate;
			this.model.powerDesign = power.design;
			this.model.powerUse = power.use;
			this.model.powerType = power.unit;
			this.onCustomControlChange();
		}
	}

	/**
	 * Returns the form dirty state
	 * @returns {boolean}
	 */
	public isDirty(): boolean {
		return this.form.dirty;
	}

	/**
	 * Prompt confirm delete a task
	 * delegate operation to host component
	 */
	deleteModel(): void {
		this.prompt.open(
			this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED')	,
			this.translatePipe.transform('MODEL.DELETE_MODEL')	,
			this.translatePipe.transform('GLOBAL.CONFIRM'),
			this.translatePipe.transform('GLOBAL.CANCEL'))
		.then(result => {
			if (result) {
				this.modelService.deleteModel(this.model.id)
					.subscribe((response) => {
						if (response && response.status !== 'error') {
							this.close(null);
						}
					})
			}
		});
	}

	/**
	 * Determine if the state for an specific controls is valid
	 * @param controlName Name of the control to be checked
	 * @returns {boolean}
	 */
	isValidControl(controlName): boolean {
		if (!this.form || !this.form.controls[controlName]) {
			return true;
		}

		return this.form.controls[controlName].valid;

	}

}
