import {AssetCommonEdit} from '../../asset/asset-common-edit';
import {ComboBoxSearchModel} from '../../../../../shared/components/combo-box/model/combobox-search-param.model';
import {ComboBoxSearchResultModel} from '../../../../../shared/components/combo-box/model/combobox-search-result.model';
import {Observable} from 'rxjs/Observable';

export class DeviceCommonComponent extends AssetCommonEdit {

	protected showRackFields = true;
	protected showRackSourceInput: 'none'|'new'|'select' = 'none';
	protected showRackTargetInput: 'none'|'new'|'select' = 'none';
	protected rackSourceOptions: Array<any> = [];
	protected rackTargetOptions: Array<any> =   [];
	protected showBladeFields = true;
	protected showBladeSourceInput: 'none'|'new'|'select' = 'none';
	protected showBladeTargetInput: 'none'|'new'|'select' = 'none';
	protected bladeSourceOptions: Array<any> = [];
	protected bladeTargetOptions: Array<any> = [];
	private readonly SET_TO_NULL = '0';
	private readonly CREATE_NEW = '-1';

	protected prepareModelRequestToSave(modelRequest: any): void {
		// currentAssetType, manufacturerId, modelId
		modelRequest.asset.manufacturerId = null;
		if ( this.model.asset.manufacturerSelectValue.id > 0 ) {
			modelRequest.asset.manufacturerId = this.model.asset.manufacturerSelectValue.id.toString();
		}
		modelRequest.asset.currentAssetType = null;
		if (this.model.asset.assetTypeSelectValue.id) {
			modelRequest.asset.currentAssetType = this.model.asset.assetTypeSelectValue.id.toString();
		}
		modelRequest.asset.modelId = null;
		if (this.model.asset.modelSelectValue.id > 0) {
			modelRequest.asset.modelId = this.model.asset.modelSelectValue.id.toString();
		}

		// roomSourceId, roomSource(new room)
		modelRequest.asset.roomSourceId = this.SET_TO_NULL;
		if (this.model.asset.roomSource && this.model.asset.roomSource.id > 0) {
			modelRequest.asset.roomSourceId = this.model.asset.roomSource.id.toString();
		}
		modelRequest.asset.roomSource = this.model.asset.newRoomSource;
		if (this.model.asset.newRoomSource) {
			modelRequest.asset.roomSourceId = this.CREATE_NEW;
		}
		delete modelRequest.asset.newRoomSource;

		// roomTargetId, roomTarget(new room)
		modelRequest.asset.roomTargetId = this.SET_TO_NULL;
		if (this.model.asset.roomTarget && this.model.asset.roomTarget.id > 0) {
			modelRequest.asset.roomTargetId = this.model.asset.roomTarget.id.toString();
		}
		modelRequest.asset.roomTarget = this.model.asset.newRoomTarget;
		if (this.model.asset.newRoomTarget) {
			modelRequest.asset.roomTargetId = this.CREATE_NEW;
		}
		delete modelRequest.asset.newRoomTarget;

		// rackSourceId, rackSource (new rack)
		modelRequest.asset.rackSourceId = this.SET_TO_NULL;
		if (this.model.asset.rackSource && this.model.asset.rackSource.id > 0) {
			modelRequest.asset.rackSourceId = this.model.asset.rackSource.id.toString();
		}
		modelRequest.asset.rackSource = this.model.asset.newRackSource;
		if (this.model.asset.newRackSource) {
			modelRequest.asset.rackSourceId = this.CREATE_NEW;
		}
		delete modelRequest.asset.newRackSource;

		// rackTargetId, rackTarget(new rack)
		modelRequest.asset.rackTargetId = this.SET_TO_NULL;
		if (this.model.asset.rackTarget && this.model.asset.rackTarget.id > 0) {
			modelRequest.asset.rackTargetId = this.model.asset.rackTarget.id.toString();
		}
		modelRequest.asset.rackTarget = this.model.asset.newRackTarget;
		if (this.model.asset.newRackTarget) {
			modelRequest.asset.rackTargetId = this.CREATE_NEW;
		}
		delete modelRequest.asset.newRackTarget;

		// sourceChassis
		if (this.model.asset.sourceChassis && this.model.asset.sourceChassis.id > 0) {
			modelRequest.asset.sourceChassis = this.model.asset.sourceChassis.id.toString();
		} else {
			modelRequest.asset.sourceChassis = this.SET_TO_NULL;
		}

		// targetChassis
		if (this.model.asset.targetChassis && this.model.asset.targetChassis.id > 0) {
			modelRequest.asset.targetChassis = this.model.asset.targetChassis.id.toString();
		} else {
			modelRequest.asset.targetChassis = this.SET_TO_NULL;
		}

		// Scale Format
		modelRequest.asset.scale = (modelRequest.asset.scale.name.value) ? modelRequest.asset.scale.name.value : modelRequest.asset.scale.name;
	}

	/**
	 * Taken from entity.crud.js
	 */
	protected toggleAssetTypeFields(): void {
		const assetType = this.model.asset.assetTypeSelectValue.id;
		switch (assetType) {
			case 'Blade': /* TODO: should 'Blade Chassis' also be considered here ??*/
				this.showHideRackFields(false); // pub.hideRackFields();
				this.showHideBladeFields(true); // pub.showChassisFields();
				break;
			case 'VM':
				this.showHideRackFields(false); // pub.hideRackFields();
				this.showHideBladeFields(false); // pub.hideChassisFields();
				break;
			default:
				// Rack-able device
				this.showHideRackFields(true); // pub.showRackFields();
				this.showHideBladeFields(false); // pub.hideChassisFields();
		}
	}

	/**
	 * Show/Hide Rack fields logic.
	 * @param {boolean} show
	 */
	protected showHideRackFields(show: boolean): void {
		this.showRackFields = show; // pub.showRackFields();
		if (!show) {
			this.showRackSourceInput = 'none';
			this.showRackTargetInput = 'none';
		} else {
			this.populateRackSelect();
		}
	}

	/**
	 * Calls endpoints to populate Rack selects.
	 */
	protected populateRackSelect(): void {
		// source room field
		const roomId = this.model.asset.roomSource ? this.model.asset.roomSource.id : null;
		if (roomId && roomId > 0) {
			this.showRackSourceInput = 'select';
			this.assetExplorerService.getRacksForRoom(roomId, 'S').subscribe(response => {
				this.rackSourceOptions = response.data;
			});
		} else if (roomId && roomId === -1) { /* -1 (New Room)*/
			this.showRackSourceInput = 'new';
		} else {
			this.showRackSourceInput = 'none';
		}

		// target room fields
		const targetRoomId = this.model.asset.roomTarget ? this.model.asset.roomTarget.id : null;
		if (targetRoomId && targetRoomId > 0) {
			this.showRackTargetInput = 'select';
			this.assetExplorerService.getRacksForRoom(targetRoomId, 'T').subscribe(response => {
				this.rackTargetOptions = response.data;
			});
		} else if (targetRoomId && targetRoomId === -1) { /* -1 (New Room)*/
			this.showRackTargetInput = 'new';
		} else {
			this.showRackTargetInput = 'none';
		}
	}

	/**
	 * Show/Hide Blade(Chassis) fields logic.
	 * @param {boolean} show
	 */
	protected showHideBladeFields(show: boolean): void {
		this.showBladeFields = show; // pub.showRackFields();
		if (!show) {
			this.showBladeSourceInput = 'none';
			this.showBladeTargetInput = 'none';
		} else {
			this.populateBladeSelect();
		}
	}

	/**
	 * Calls endpoints to populate Blade(Chassis) selects
	 */
	protected populateBladeSelect(): void {
		// source room field
		const roomId = this.model.asset.roomSource ? this.model.asset.roomSource.id : null;
		if (roomId && roomId > 0) {
			this.showBladeSourceInput = 'select';
			this.assetExplorerService.getChassisForRoom(roomId).subscribe(response => {
				this.bladeSourceOptions = response.data;
			});
		} else {
			this.showBladeSourceInput = 'none';
		}

		// target room field
		const targetRoomId = this.model.asset.roomTarget ? this.model.asset.roomTarget.id : null;
		if (targetRoomId && targetRoomId > 0) {
			this.showBladeTargetInput = 'select';
			this.assetExplorerService.getChassisForRoom(targetRoomId).subscribe(response => {
				this.bladeTargetOptions = response.data;
			});
		} else {
			this.showBladeTargetInput = 'none';
		}
	}

	/**
	 * Function that handles the request of the Asset Types tds-combobox
	 * @param {ComboBoxSearchModel} searchModel
	 * @returns {Observable<ComboBoxSearchResultModel>}
	 */
	protected searchAssetTypes = (searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel>  => {
		searchModel.query = `manufacturerId=${this.model.asset.manufacturerSelectValue.id ? this.model.asset.manufacturerSelectValue.id : ''}`;
		return this.assetExplorerService.getAssetTypesForComboBox(searchModel);
	}

	/**
	 * Function that handles the request of the Manufacturers tds-combobox
	 * @param {ComboBoxSearchModel} searchModel
	 * @returns {Observable<ComboBoxSearchResultModel>}
	 */
	protected searchManufacturers = (searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> => {
		searchModel.query = `assetType=${this.model.asset.assetTypeSelectValue.id ? this.model.asset.assetTypeSelectValue.id : ''}`;
		return this.assetExplorerService.getManufacturersForComboBox(searchModel);
	}

	/**
	 * Function that handles the request of the Models tds-combobox
	 * @param {ComboBoxSearchModel} searchModel
	 * @returns {Observable<ComboBoxSearchResultModel>}
	 */
	protected searchModels = (searchModel: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> => {
		searchModel.query = `manufacturerId=${this.model.asset.manufacturerSelectValue.id ? this.model.asset.manufacturerSelectValue.id : ''}
			&assetType=${this.model.asset.assetTypeSelectValue.id ? this.model.asset.assetTypeSelectValue.id : ''}`;
		return this.assetExplorerService.getModelsForComboBox(searchModel);
	}

	/**
	 * On Asset Types combobox change.
	 * @param value
	 */
	protected onAssetTypeValueChange(value: any): void {
		if (!value) {
			value = {id: null};
		}
		this.model.asset.assetTypeSelectValue = value;
		this.model.asset.modelSelectValue = {id: null};
		this.toggleAssetTypeFields();
	}

	/**
	 * On Manufacturers combobox change.
	 * @param value
	 */
	protected onManufacturerValueChange(value: any): void {
		if (!value) {
			value = {id: null};
		}
		this.model.asset.manufacturerSelectValue = value;
		this.model.asset.modelSelectValue = {id: null};
	}

	/**
	 * On Models combobox change.
	 * @param value
	 */
	protected onModelValueChange(value: any): void {
		this.model.asset.assetTypeSelectValue.id = value.assetType;
		this.model.asset.manufacturerSelectValue.id = value.manufacturerId;
		this.model.asset.modelSelectValue.id = value.id;
		this.model.asset.modelSelectValue.text = value.text;
		this.toggleAssetTypeFields();
	}

	/**
	 * On Source Room dropdown select change.
	 * @param event
	 */
	protected onRoomSourceValueChange(event: any): void {
		this.toggleAssetTypeFields();
	}

	/**
	 * On Target Room dropdown select change.
	 * @param event
	 */
	protected onRoomTargetValueChange(event: any): void {
		this.toggleAssetTypeFields();
	}
}