import { Component, OnInit } from '@angular/core';
import { UIExtraDialog } from '../../../../../../shared/services/ui-dialog.service';
import {Observable} from 'rxjs/Observable';

import {BulkActions, BulkChangeModel} from '../../model/bulk-change.model';
import {UIPromptService} from '../../../../../../shared/directives/ui-prompt.directive';
import {BulkActionResult} from '../../model/bulk-change.model';
import {AssetExplorerService} from '../../../../service/asset-explorer.service';
import {CustomDomainService} from '../../../../../fieldSettings/service/custom-domain.service';
import {Permission} from '../../../../../../shared/model/permission.model';
import {PermissionService} from '../../../../../../shared/services/permission.service';
import {BulkChangeEditColumnsModel} from '../../model/bulk-change-edit-columns.model';
import {DataGridOperationsHelper} from '../../../../../../shared/utils/data-grid-operations.helper';
import {BulkEditAction, IdTextItem} from '../../model/bulk-change.model';
import {SortUtils} from '../../../../../../shared/utils/sort.utils';
import {StringUtils} from '../../../../../../shared/utils/string.utils';
import {BulkChangeService} from '../../../../service/bulk-change.service';
import {TagService} from '../../../../../assetTags/service/tag.service';
import {TagModel} from '../../../../../assetTags/model/tag.model';
import {ApiResponseModel} from '../../../../../../shared/model/ApiResponseModel';
import {TranslatePipe} from '../../../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'tds-bulk-change-edit',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/bulk-change/components/bulk-change-edit/bulk-change-edit.component.html'
})
export class BulkChangeEditComponent extends UIExtraDialog implements OnInit {
	COLUMN_MIN_WIDTH = 120;
	CLEAR_ACTION = 'clear';
	isLoaded: boolean;
	private defaultDomain: IdTextItem = {id: 'common', text: 'Common Fields'};
	tagList: TagModel[] = [];
	yesNoList: IdTextItem[] = [{ id: '?', text: '?'}, { id: 'Y', text: 'Yes'}, { id: 'N', text: 'No'}];
	protected domains: IdTextItem[];
	selectedItems: string[] = [];
	commonFieldSpecs: any[] = [];
	gridColumns: BulkChangeEditColumnsModel;
	gridSettings: DataGridOperationsHelper;
	affectedAssets: number;
	editRows: {options: Array<BulkEditAction>, actions: BulkEditAction[], selectedValues: {domain: IdTextItem, field: IdTextItem, action: IdTextItem, value: any}[] };

	constructor(
		private bulkChangeModel: BulkChangeModel,
		private promptService: UIPromptService,
		private assetExplorerService: AssetExplorerService,
		private permissionService: PermissionService,
		private customDomainService: CustomDomainService,
		private bulkChangeService: BulkChangeService,
		private tagService: TagService,
		private translatePipe: TranslatePipe) {
			super('#bulk-change-edit-component');
			this.affectedAssets = this.bulkChangeModel.affected;
			this.domains = [];
			this.editRows = {options: [], actions: [], selectedValues: [] };
			this.gridColumns = new BulkChangeEditColumnsModel();
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
			this.gridSettings = new DataGridOperationsHelper(this.editRows.options);
			this.isLoaded = true;
		});
	}

	private addRow(): any {
		let fields = [...this.getFieldsByDomain(this.defaultDomain)];
		this.editRows.options.push({fields: fields, actions: []});
		this.editRows.selectedValues.push({domain: this.defaultDomain, field: null, action: null, value: null});
	}

	onDomainValueChange(domain: IdTextItem, index: number): void {
		this.editRows.selectedValues[index].domain = domain;
		this.editRows.selectedValues[index].field = null;
		this.editRows.selectedValues[index].action = null;
		this.editRows.options[index].fields = [...this.getFieldsByDomain(domain)];
		this.editRows.options[index].actions = [];
	}

	addHandler({sender}): void {
		this.addRow();
		this.gridSettings.loadPageData();
	}

	removeHandler({dataItem, rowIndex}): void {
		this.editRows.options.splice(rowIndex, 1);
		this.editRows.selectedValues.splice(rowIndex, 1);
		this.gridSettings.loadPageData();
	}

	closeDialog(bulkActionResult: BulkActionResult): void {
			this.close(bulkActionResult);
	}

	cancelCloseDialog(bulkActionResult: BulkActionResult): void {
		this.dismiss(bulkActionResult || {action: null, success: false});
	}

	// show control if this belongs to class and clear action is not selected
	canShowControl(controlType: string, rowIndex): boolean {
		const selectedValue = this.editRows.selectedValues[rowIndex];
		if (!selectedValue.field || !selectedValue.action) {
			return;
		}

		const isTypeOfControl = (selectedValue && selectedValue.field && selectedValue.field['control'] ===  controlType)
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

	onFieldValueChange(field: any, index: number): void {
		if (!field) {
			return;
		}
		this.editRows.selectedValues[index].field = field;
		let actions = field.actions.map( action => ({
			id: action, text: this.translatePipe.transform(`ASSET_EXPLORER.BULK_CHANGE.ACTIONS.${action.toUpperCase()}`)
		}));
		this.editRows.options[index].actions = actions;
	}

	onActionValueChange(action: IdTextItem, index: number): void {
		this.editRows.selectedValues[index].action = action;
	}

	private getDomainList(fields: any[]): IdTextItem[] {
		const domainList = fields
			.map((field) => field.domain.toLowerCase())
			.map((domain): IdTextItem => ( {id: domain.toUpperCase(), text: `${StringUtils.toCapitalCase(domain, false)} Fields`}  ));

		return domainList;
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

	private getFieldsByDomain(domain: IdTextItem): IdTextItem[] {
		let fields: Array<any> = [];
		if (!domain) {
			return fields;
		}
		const domainFields =  this.commonFieldSpecs.find((field: any) => field.domain === domain.id.toUpperCase());
		if (domainFields && domainFields.fields) {
			fields = domainFields.fields
				.filter((item) => item.bulkChangeActions && item.bulkChangeActions.length > 0)
				.map((item: any) => ({id: item.field, text: item.label, control: item.control, actions: item.bulkChangeActions}))
		}
		return fields.sort((a, b) => SortUtils.compareByProperty(a, b, 'text'));
	}

	private update(): Promise<BulkActionResult>  {
		return new Promise((resolve, reject) =>  {
			const edits = this.editRows.selectedValues
				.map((row) => {
					const value = row.action.id === this.CLEAR_ACTION ? null : row.value;

					return {
						fieldName: row.field.id,
						action: row.action.id,
						value: value || '[]'
					}
				});

			if (this.hasAssetEditPermission()) {
				this.bulkChangeService.bulkUpdate(this.bulkChangeModel.selectedItems , edits)
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
}