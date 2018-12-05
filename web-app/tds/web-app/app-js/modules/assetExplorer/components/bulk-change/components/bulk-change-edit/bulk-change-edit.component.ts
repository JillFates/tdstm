import { Component, OnInit } from '@angular/core';
import { UIExtraDialog } from '../../../../../../shared/services/ui-dialog.service';
import {Observable} from 'rxjs';

import {BulkActions, BulkChangeModel} from '../../model/bulk-change.model';
import {UIPromptService} from '../../../../../../shared/directives/ui-prompt.directive';
import {BulkActionResult} from '../../model/bulk-change.model';
import {CustomDomainService} from '../../../../../fieldSettings/service/custom-domain.service';
import {Permission} from '../../../../../../shared/model/permission.model';
import {PermissionService} from '../../../../../../shared/services/permission.service';
import {DataGridOperationsHelper} from '../../../../../../shared/utils/data-grid-operations.helper';
import {BulkEditAction, IdTextItem} from '../../model/bulk-change.model';
import {SortUtils} from '../../../../../../shared/utils/sort.utils';
import {StringUtils} from '../../../../../../shared/utils/string.utils';
import {BulkChangeService} from '../../../../service/bulk-change.service';
import {TagService} from '../../../../../assetTags/service/tag.service';
import {TagModel} from '../../../../../assetTags/model/tag.model';
import {ApiResponseModel} from '../../../../../../shared/model/ApiResponseModel';
import {TranslatePipe} from '../../../../../../shared/pipes/translate.pipe';
import {PreferenceService} from '../../../../../../shared/services/preference.service';

@Component({
	selector: 'tds-bulk-change-edit',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/bulk-change/components/bulk-change-edit/bulk-change-edit.component.html'
})
export class BulkChangeEditComponent extends UIExtraDialog implements OnInit {
	private readonly CLEAR_ACTION = 'clear';

	protected readonly TYPE_OPTIONS_CONTROL = 'Options';
	protected readonly TYPE_INLIST_CONTROL = 'InList';
	protected readonly TYPE_CUSTOM_FIELD_LIST_CONTROL = 'List';
	protected readonly TYPE_CUSTOM_FIELD_YES_NO = 'YesNo';
	protected readonly TYPE_REFERENCE_CONTROL = 'Reference';
	private readonly DEFAULT_DOMAIN: IdTextItem = { id: 'COMMON', text: 'Common Fields' };

	protected tagList: Array<TagModel>;
	protected yesNoList: IdTextItem[] = [{ id: 'Yes', text: 'Yes'}, { id: 'No', text: 'No'}];
	protected domains: IdTextItem[];
	protected gridSettings: DataGridOperationsHelper;
	protected editRows: {selectedValues: Array<{domain: IdTextItem, field: IdTextItem, action: IdTextItem, value: any}> };
	protected listOptions: any;
	protected availableFields: Array<any>;
	protected userDateFormat: string;
	protected userTimeZone: string;
	protected fieldActionsMap: any = {};

	constructor(
		protected bulkChangeModel: BulkChangeModel,
		private promptService: UIPromptService,
		private permissionService: PermissionService,
		private customDomainService: CustomDomainService,
		private bulkChangeService: BulkChangeService,
		private tagService: TagService,
		private translatePipe: TranslatePipe,
		private preferenceService: PreferenceService) {
			super('#bulk-change-edit-component');
			this.availableFields = [];
			this.domains = [];
			this.listOptions = {};
			this.tagList = [];
			this.editRows = {selectedValues: [] };
			this.userDateFormat = this.preferenceService.getUserDateFormatForMomentJS();
			this.userTimeZone = this.preferenceService.getUserTimeZone();
	}

	ngOnInit() {
		this.loadLists();
	}

	/**
	 * Loads the Common Field Specs first, then the system lists & options for the field dropdowns.
	 */
	private loadLists(): void {
		this.customDomainService.getCommonFieldSpecs().subscribe((result: any[]) => {
			this.buildDomainsList();
			this.buildFieldsList(result);
			this.gridSettings = new DataGridOperationsHelper(this.editRows.selectedValues);
			this.addHandler();
		});
	}

	/**
	 * On Grid Add button clicked.
	 * @param {any} sender
	 */
	protected addHandler(): void {
		this.addRow();
		this.gridSettings.loadPageData();
	}

	/**
	 * On Grid Remove button clicked.
	 * @param {any} dataItem
	 * @param {any} rowIndex
	 */
	protected removeHandler({dataItem, rowIndex}): void {
		// this.editRows.fields.splice(rowIndex, 1);
		this.editRows.selectedValues.splice(rowIndex, 1);
		this.gridSettings.loadPageData();
	}

	/**
	 * On Field Name Selected from dropdown prepare it's control options/values.
	 * List/Options/InList values are setted in here.
	 * @param field
	 * @param {number} index
	 */
	protected onFieldValueChange(field: any, index: number): void {
		if (!field) {
			return;
		}
		this.editRows.selectedValues[index].action = null;
		this.editRows.selectedValues[index].value = null;
		this.editRows.selectedValues[index].field = field;
	}

	/**
	 * On Action dropdown value changes.
	 * @param {IdTextItem} action
	 * @param {number} index
	 */
	protected onActionValueChange(action: IdTextItem, index: number): void {
		this.editRows.selectedValues[index].value = null;
		this.editRows.selectedValues[index].action = action;
	}

	/**
	 * On Asset Tag Selector Value changes.
	 * @param column
	 * @param rowIndex
	 * @param event
	 */
	protected onTagFilterChange(column, rowIndex, event): void {
		const tagAssets = event.tags || [];

		this.editRows.selectedValues[rowIndex].value =  tagAssets.length ? `[${tagAssets.map((tag) => tag.id).toString()}]` : '[]';
	}

	/**
	 * On Next button clicked.
	 */
	onNext(): void {
		this.confirmUpdate()
			.then(this.doBulkUpdate.bind(this))
			.then(this.cancelCloseDialog.bind(this))
			.catch((err) => console.log(err));
	}

	protected cancelCloseDialog(bulkActionResult: BulkActionResult): void {
		if (!bulkActionResult) {
			bulkActionResult = {action: null, success: false};
		}
		this.dismiss(bulkActionResult);
	}

	/**
	 * Determine to show control if this belongs to class and clear action is not selected
	 * @param {string} controlType
	 * @param rowIndex
	 * @returns {boolean}
	 */
	protected canShowControl(controlType: string, rowIndex): boolean {
		const selectedValue = this.editRows.selectedValues[rowIndex];
		if (!selectedValue.field || !selectedValue.action) {
			return;
		}
		let isTypeOfControl = (selectedValue && selectedValue.field && selectedValue.field['control'] ===  controlType);
		if (!isTypeOfControl && this.isFieldControlOptionsList(controlType)) {
			isTypeOfControl = (selectedValue && selectedValue.field && this.isFieldControlOptionsList(selectedValue.field['control']));
		}
		const isClearAction = (selectedValue.action === null  || selectedValue.action.id === this.CLEAR_ACTION);
		return isTypeOfControl && !isClearAction;
	}

	/**
	 * Determines if all fields has a value populated for it's field.
	 * Used to disable the form if returns false.
	 * @returns {boolean} True | False
	 */
	protected isAllInputEntered(): boolean {
		return this.editRows.selectedValues.every(row => row.field && row.action && (row.value || row.action.id === this.CLEAR_ACTION ))
	}

	/**
	 * Adds a new empty row to the grid.
	 * @returns {any}
	 */
	private addRow(): any {
		this.editRows.selectedValues.push({domain: this.DEFAULT_DOMAIN, field: null, action: null, value: null});
	}

	/**
	 * Check if field control is any system Options or InList where the select options will be
	 * from the this.listOptions.
	 * @param fieldControl -
	 * @returns {boolean}
	 */
	private isFieldControlOptionsList(fieldControlName: String): boolean {
		return fieldControlName === this.TYPE_OPTIONS_CONTROL
			|| fieldControlName === this.TYPE_INLIST_CONTROL
			|| fieldControlName.startsWith(this.TYPE_OPTIONS_CONTROL)
			|| fieldControlName === this.TYPE_REFERENCE_CONTROL;
	}

	/**
	 * Build Domain (Class List).
	 * @param {any[]} fields
	 * @returns {IdTextItem[]}
	 */
	private buildDomainsList(): void {
		let domainList = [];
		if (this.assetsDifferFromDomains()) {
			domainList.push(this.DEFAULT_DOMAIN);
		} else {
			const firstAssetClass = this.bulkChangeModel.selectedAssets[0].common_assetClass;
			domainList.push({id: firstAssetClass, text: `${StringUtils.toCapitalCase(firstAssetClass, false)} Fields`});
			domainList.push(this.DEFAULT_DOMAIN);
		}
		this.domains = domainList;
	}

	/**
	 * Set the fields available for the domain to be displayed on the row Fields dropdown
	 * @param {IdTextItem} domain
	 * @returns {IdTextItem[]}
	 */
	private buildFieldsList(fieldSpecs): void {
		this.availableFields = [];
		// for each domain combine it's fields
		this.domains.forEach( (domainItem: IdTextItem) => {
			const domainFields =  fieldSpecs.find((field: any) => field.domain === domainItem.id.toUpperCase());
			if (domainFields && domainFields.fields) {
				// store the fields on the main availableFields list
				let fields = domainFields.fields
					.filter((field) => field.bulkChangeActions && field.bulkChangeActions.length > 0)
					.map((field: any) => ({id: field.field, text: field.label, control: field.control, actions: field.bulkChangeActions, constraints: field.constraints}));
				this.availableFields.push(...fields);
				// store the field-actions map relationship
				this.availableFields.forEach( field => {
					this.fieldActionsMap[field.id] = field.actions.map( action => ({
						id: action, text: this.translatePipe.transform(`ASSET_EXPLORER.BULK_CHANGE.ACTIONS.${action.toUpperCase()}`)
					}));
					// check if field has list values and store it on the listOptions map.
					this.listOptions[field.id] = [];
					if (field.constraints && field.constraints.values) {
						this.listOptions[field.id] = field.constraints.values;
					}
				});
			}
		});
		this.availableFields = this.availableFields.sort( (a, b) => SortUtils.compareByProperty(a, b, 'text'));
		this.getAndBuildSystemListOptions();
	}

	/**
	 * Gets the System List Options and stores it on the listOptions map.
	 */
	private getAndBuildSystemListOptions(): void {
		// Get Tag List
		this.tagService.getTags().subscribe( (result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS && result.data) {
				this.tagList = result.data;
			}
		}, error => console.error(error));
		// Get system list options and set it as available.
		this.bulkChangeService.getAssetListOptions(this.domains[0].id === 'COMMON' ? 'DEVICE' : this.domains[0].id).subscribe( result => {
			this.listOptions['planStatus'] = result.planStatusOptions.map(item => { return {id: item, text: item} });
			this.listOptions['validation'] = result.validationOptions.map(item => { return {id: item, text: item} });
			this.listOptions['moveBundle'] = result.dependencyMap.moveBundleList.map(item => { return {id: item.id.toString(), text: item.name} });
			if (this.domains[0].id === 'DEVICE') {
				this.listOptions['railType'] = result.railTypeOption.map(item => { return {id: item, text: item} });
			}
			if (this.domains[0].id === 'APPLICATION') {
				this.listOptions['criticality'] = result.criticalityOptions.map(item => { return {id: item, text: item} });
			}
		}, error => console.error(error));
	}

	/**
	 * Determines if Assets selected belong to differents domain.
	 * @returns {boolean}
	 */
	private assetsDifferFromDomains(): boolean {
		if (this.bulkChangeModel.selectedAssets.length <= 1) {
			return false;
		}
		const firstAssetClass = this.bulkChangeModel.selectedAssets[0].common_assetClass;
		let areDifferent = false;
		this.bulkChangeModel.selectedAssets.forEach(asset => {
			if (firstAssetClass !== asset.common_assetClass) {
				areDifferent = true;
				return;
			}
		});
		return areDifferent;
	}

	/**
	 * Bulk Update confirmation popup. Launched when Next button gets clicked.
	 * @returns {Promise<boolean>}
	 */
	private confirmUpdate(): Promise<boolean> {
		const message = this.translatePipe.transform('ASSET_EXPLORER.BULK_CHANGE.EDIT.CONFIRM_UPDATE', [this.bulkChangeModel.affected]);
		return new Promise((resolve, reject) =>  {
			this.promptService.open(this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				message,
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'))
				.then((result) => result ? resolve() : reject({action: BulkActions.Edit, success: false, message: 'canceled'}))
		})
	}

	/**
	 * TODO dontiveros: This check needs to be at the displaying of the Bulk Change html button level.
	 * TODO dontiveros: on asset-explorer-view-grid.component.html file
	 * Checks for Asset Edit permission.
	 * @returns {boolean}
	 */
	private hasAssetEditPermission(): boolean {
		return this.permissionService.hasPermission(Permission.AssetEdit);
	}

	/**
	 * Calls Service to perform the bulk update based on all values in the form.
	 * @returns {Promise<BulkActionResult>}
	 */
	private doBulkUpdate(): Promise<BulkActionResult>  {
		return new Promise((resolve, reject) =>  {
			const edits = this.editRows.selectedValues.map((row: any) => {
					let value = this.getUpdateValueForBulkAction(row.field.id, row.value, row.action.id, row.field.control);
					return {
						fieldName: row.field.id,
						action: row.action.id,
						value: value
					}
				});

			if (this.hasAssetEditPermission()) {
				this.bulkChangeService.bulkUpdate(this.bulkChangeModel.selectedItems , edits, this.domains[0].id)
					.subscribe((result) => {
						resolve({action: BulkActions.Edit, success: true, message: `${this.bulkChangeModel.affected} Assets edited successfully`});
					}, (err) => {
						reject({action: BulkActions.Edit, success: false, message: err.message || err})
					});
			} else {
				reject({action: BulkActions.Edit, success: false, message: 'Forbidden operation' });
			}
		})
	}

	/**
	 * Determines the ouput value that should go in the request payload for the bulkupdate of a particular type of field.
	 * @param {string} fieldName
	 * @param originalValue
	 * @param {string} action
	 * @param {string} control
	 * @returns {string}
	 */
	private getUpdateValueForBulkAction(fieldName: string, originalValue: any, action: string, control: string): string {
			// let value = action === this.CLEAR_ACTION ? null : originalValue;
			if (this.isFieldControlOptionsList(control)
				|| control === this.TYPE_CUSTOM_FIELD_LIST_CONTROL
				|| control === this.TYPE_CUSTOM_FIELD_YES_NO) {
				if (action === this.CLEAR_ACTION) {
					return null;
				}
				// this handles the Control 'List' values for custom fields.
				return originalValue.id ? originalValue.id : originalValue;
			} else if (fieldName === 'tagAssets') {
				if (action === this.CLEAR_ACTION) {
					return '[]';
				} else {
					return originalValue;
				}
			} else if (action === this.CLEAR_ACTION) {
				return null;
			} else {
				return originalValue;
			}
	}
}