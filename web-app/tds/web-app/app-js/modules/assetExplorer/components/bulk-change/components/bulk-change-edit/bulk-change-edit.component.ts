import { Component, OnInit } from '@angular/core';
import { UIExtraDialog } from '../../../../../../shared/services/ui-dialog.service';
import {Observable} from 'rxjs';

import {BulkActions, BulkChangeModel} from '../../model/bulk-change.model';
import {UIPromptService} from '../../../../../../shared/directives/ui-prompt.directive';
import {BulkActionResult} from '../../model/bulk-change.model';
import {AssetExplorerService} from '../../../../service/asset-explorer.service';
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
	COLUMN_MIN_WIDTH = 120;
	private readonly CLEAR_ACTION = 'clear';
	protected readonly TYPE_OPTIONS_CONTROL = 'Options';
	protected readonly TYPE_INLIST_CONTROL = 'InList';
	protected readonly TYPE_CUSTOM_FIELD_LIST_CONTROL = 'List';
	protected readonly TYPE_CUSTOM_FIELD_YES_NO = 'YesNo';

	isLoaded: boolean;
	private defaultDomain: IdTextItem = {id: 'COMMON', text: 'Common Fields'};
	tagList: TagModel[] = [];
	yesNoList: IdTextItem[] = [{ id: 'Yes', text: 'Yes'}, { id: 'No', text: 'No'}];
	protected domains: IdTextItem[];
	selectedItems: string[] = [];
	commonFieldSpecs: any[] = [];
	gridSettings: DataGridOperationsHelper;
	affectedAssets: number;
	editRows: {listOptions: Array<any>, fields: Array<BulkEditAction>, selectedValues: {domain: IdTextItem, field: IdTextItem, action: IdTextItem, value: any}[] };
	private listOptions: any;
	protected userDateFormat: string;
	protected userTimeZone: string;

	constructor(
		private bulkChangeModel: BulkChangeModel,
		private promptService: UIPromptService,
		private assetExplorerService: AssetExplorerService,
		private permissionService: PermissionService,
		private customDomainService: CustomDomainService,
		private bulkChangeService: BulkChangeService,
		private tagService: TagService,
		private translatePipe: TranslatePipe,
		private preferenceService: PreferenceService) {
			super('#bulk-change-edit-component');
			this.affectedAssets = this.bulkChangeModel.affected;
			this.domains = [];
			this.listOptions = {};
			this.editRows = {listOptions: [], fields: [], selectedValues: [] };
			this.userDateFormat = this.preferenceService.getUserDateFormatForMomentJS();
			this.userTimeZone = this.preferenceService.getUserTimeZone();
	}

	ngOnInit() {
		this.isLoaded = false;

		Observable.forkJoin(this.customDomainService.getCommonFieldSpecs(), this.tagService.getTags()).subscribe((result: any[]) => {
			const [fields, tags] = result;
			this.commonFieldSpecs = result[0];
			this.domains = this.getDomainList(this.commonFieldSpecs);
			if (tags.status === ApiResponseModel.API_SUCCESS && tags.data) {
				this.tagList = tags.data;
			}
			this.addRow();
			this.gridSettings = new DataGridOperationsHelper(this.editRows.fields);
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
			this.isLoaded = true;
		});
	}

	private addRow(): any {
		let fields = [...this.getFieldsByDomain(this.defaultDomain)];
		this.editRows.fields.push({fields: fields, actions: []});
		this.editRows.selectedValues.push({domain: this.defaultDomain, field: null, action: null, value: null});
	}

	onDomainValueChange(domain: IdTextItem, index: number): void {
		this.editRows.selectedValues[index].domain = domain;
		this.editRows.selectedValues[index].field = null;
		this.editRows.selectedValues[index].action = null;
		this.editRows.selectedValues[index].value = null;
		this.editRows.fields[index].fields = [...this.getFieldsByDomain(domain)];
		this.editRows.fields[index].actions = [];
	}

	addHandler({sender}): void {
		this.addRow();
		this.gridSettings.loadPageData();
	}

	removeHandler({dataItem, rowIndex}): void {
		this.editRows.fields.splice(rowIndex, 1);
		this.editRows.selectedValues.splice(rowIndex, 1);
		this.gridSettings.loadPageData();
	}

	closeDialog(bulkActionResult: BulkActionResult): void {
			this.close(bulkActionResult);
	}

	cancelCloseDialog(bulkActionResult: BulkActionResult): void {
		this.dismiss(bulkActionResult || {action: null, success: false});
	}

	/**
	 * Determine to show control if this belongs to class and clear action is not selected
	 * @param {string} controlType
	 * @param rowIndex
	 * @returns {boolean}
	 */
	canShowControl(controlType: string, rowIndex): boolean {
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

	onTagFilterChange(column, rowIndex, event): void {
		const tagAssets = event.tags || [];

		this.editRows.selectedValues[rowIndex].value =  tagAssets.length ? `[${tagAssets.map((tag) => tag.id).toString()}]` : '[]';
	}

	isAllInputEntered(): boolean {
		return this.editRows.selectedValues.every(row => row.domain && row.field && row.action && (row.value || row.action.id === this.CLEAR_ACTION ))
	}

	onNext(): void {
		this.confirmUpdate()
			.then(this.update.bind(this))
			.then(this.closeDialog.bind(this))
			.catch((err) => console.log(err));
	}

	/**
	 * On Field Selected from dropdown prepare it's control options/values.
	 * List/Options/InList values are setted in here.
	 * @param field
	 * @param {number} index
	 */
	onFieldValueChange(field: any, index: number): void {
		if (!field) {
			return;
		}
		this.editRows.selectedValues[index].action = null;
		this.editRows.selectedValues[index].value = null;
		this.editRows.selectedValues[index].field = field;
		let actions = field.actions.map( action => ({
			id: action, text: this.translatePipe.transform(`ASSET_EXPLORER.BULK_CHANGE.ACTIONS.${action.toUpperCase()}`)
		}));
		this.editRows.fields[index].actions = actions;
		if (this.isFieldControlOptionsList(field.control)) {
			this.editRows.listOptions[index] = this.listOptions[field.id] ? this.listOptions[field.id] : [];
		} else if (field.control === this.TYPE_CUSTOM_FIELD_LIST_CONTROL) {
			let listOptions = field.constraints && field.constraints.values ? field.constraints.values : [];
			listOptions = listOptions.map( item => { return {id: item, text: item} });
			this.editRows.listOptions[index] = listOptions;
		}
	}

	/**
	 * Check if field control is any system Options or InList.
	 * @param fieldControl
	 * @returns {boolean}
	 */
	private isFieldControlOptionsList(fieldControl: any): boolean {
		return fieldControl === this.TYPE_OPTIONS_CONTROL
			|| fieldControl === this.TYPE_INLIST_CONTROL
			|| fieldControl.startsWith(this.TYPE_OPTIONS_CONTROL);
	}

	onActionValueChange(action: IdTextItem, index: number): void {
		this.editRows.selectedValues[index].value = null;
		this.editRows.selectedValues[index].action = action;
	}

	private getDomainList(fields: any[]): IdTextItem[] {
		let domainList = [];
		if (this.assetsDifferFromDomains()) {
			domainList.push(this.defaultDomain);
		} else {
			const firstAssetClass = this.bulkChangeModel.selectedAssets[0].common_assetClass;
			domainList.push({id: firstAssetClass, text: `${StringUtils.toCapitalCase(firstAssetClass, false)} Fields`});
			domainList.push(this.defaultDomain);
		}
		return domainList;
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

	private confirmUpdate(): Promise<boolean> {
		const message = this.translatePipe.transform('ASSET_EXPLORER.BULK_CHANGE.EDIT.CONFIRM_UPDATE', [this.affectedAssets]);

		return new Promise((resolve, reject) =>  {
			this.promptService.open(this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				message,
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'))
				.then((result) => result ? resolve() : reject({action: BulkActions.Edit, success: false, message: 'canceled'}))
		})
	}

	private hasAssetEditPermission(): boolean {
		return this.permissionService.hasPermission(Permission.AssetEdit);
	}

	/**
	 * Set the fields available for the domain to be displayed on the row Fields dropdown
	 * @param {IdTextItem} domain
	 * @returns {IdTextItem[]}
	 */
	private getFieldsByDomain(domain: IdTextItem): IdTextItem[] {
		let fields: Array<any> = [];
		if (!domain) {
			return fields;
		}
		const domainFields =  this.commonFieldSpecs.find((field: any) => field.domain === domain.id.toUpperCase());
		if (domainFields && domainFields.fields) {
			fields = domainFields.fields
				.filter((item) => item.bulkChangeActions && item.bulkChangeActions.length > 0)
				.map((item: any) => ({id: item.field, text: item.label, control: item.control, actions: item.bulkChangeActions, constraints: item.constraints}))
		}
		return fields.sort((a, b) => SortUtils.compareByProperty(a, b, 'text'));
	}

	private update(): Promise<BulkActionResult>  {
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
						resolve({action: BulkActions.Edit, success: true, message: `${this.affectedAssets} Assets edited successfully`});
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
				return originalValue.id;
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