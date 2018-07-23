import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { UIExtraDialog } from '../../../../../../shared/services/ui-dialog.service';

import {BulkChangeModel} from '../../model/bulk-change.model';
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
	CLEAR_ACTION: string;
	tagList: TagModel[] = [];
	yesNoList: IdTextItem[] = [];
	actions: IdTextItem[] = [];
	assetClassList: IdTextItem[] = [];
	selectedItems: string[] = [];
	commonFieldSpecs: any[] = [];
	gridColumns: BulkChangeEditColumnsModel;
	gridSettings: DataGridOperationsHelper;
	affectedAssets: number;
	editRows: { actions: BulkEditAction[], selectedValues: {domain: IdTextItem, field: IdTextItem, action: IdTextItem, value: any}[] };

	constructor(private bulkChangeModel: BulkChangeModel, private promptService: UIPromptService, private assetExplorerService: AssetExplorerService,
				private permissionService: PermissionService, private customDomainService: CustomDomainService, private bulkChangeService: BulkChangeService, private tagService: TagService, private translatePipe: TranslatePipe) {
		super('#bulk-change-edit-component');
		this.affectedAssets = this.bulkChangeModel.selectedItems.length;
		console.log('Selected items');
	}

	addRow(): any {
		this.editRows.actions.push({domain: 'APPLICATION', actions: [...this.actions], fields: [] });
		this.editRows.selectedValues.push({domain: null, field: null, action: null, value: null});
	}

	addHandler({sender}): void {
		this.addRow();

		this.gridSettings.loadPageData();
	}

	removeHandler({dataItem, rowIndex}): void {
		this.editRows.actions.splice(rowIndex, 1);
		this.editRows.selectedValues.splice(rowIndex, 1);
		this.gridSettings.loadPageData();
	}

	ngOnInit() {
		this.CLEAR_ACTION = 'clear';
		this.yesNoList = [ { id: '?', text: '?'}, { id: 'Y', text: 'Yes'}, { id: 'N', text: 'No'}];
		this.editRows = { actions: [], selectedValues: [] };
		this.customDomainService.getCommonFieldSpecs()
			.subscribe((results) => {
				this.commonFieldSpecs = results;
			});

		this.bulkChangeService.getActions()
			.map((res: any) => Object.keys(res.data.tags))
			.subscribe((actions: string[]) => {
				this.actions = actions
					.map((action) => ({id: action, text: this.translatePipe.transform(`ASSET_EXPLORER.BULK_CHANGE.ACTIONS.${action.toUpperCase()}`) })) ;
				this.addRow();
				this.gridSettings = new DataGridOperationsHelper(this.editRows.actions,
					[], // initial sort config.
					{ mode: 'single', checkboxOnly: false}, // selectable config.
					{ useColumn: 'id' }); // checkbox config.
			});

		this.tagService.getTags()
			.subscribe((result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS && result.data) {
					this.tagList = result.data;
				}
			}, error => console.log('error on GET Tag List', error));

		this.assetClassList = ['common', 'application', 'database', 'device', 'storage']
			.map((domain): IdTextItem => ( {id: domain, text: `${StringUtils.toCapitalCase(domain, false)} Fields`}  ));

		this.gridColumns = new BulkChangeEditColumnsModel();

	}

	cancelCloseDialog(bulkActionResult: BulkActionResult): void {
		this.dismiss(bulkActionResult || {action: null, success: false});
	}

	closeDialog(bulkActionResult: BulkActionResult): void {
		this.close(bulkActionResult);
	}

	onNext() {
		this.update();
	}

	hasAssetDeletePermission(): boolean {
		return this.permissionService.hasPermission(Permission.AssetDelete);
	}

	onDomainChange(domain: IdTextItem, index: number) {
		const {actions, selectedValues} = this.editRows;

		actions[index].fields =  this.getFieldsByDomain(domain);
		selectedValues[index].field = null
	}

	getFieldsByDomain(domain: IdTextItem): IdTextItem[] {
		let fields: IdTextItem[] = [];
		if (!domain) {
			return fields;
		}

		const domainFields =  this.commonFieldSpecs.find((field: any) => field.domain === domain.id.toUpperCase());
		if (domainFields && domainFields.fields) {
			fields = domainFields.fields.map((item: any) => ({id: item.field, text: item.label, control: item.control}));
		}

		return fields.sort((a, b) => SortUtils.compareByProperty(a, b, 'text'));
	}

	// show control if this belongs to class and clear action is not selected
	canShowControl(controlType: string, rowIndex): boolean {
		const selectedValue = this.editRows.selectedValues[rowIndex];

		const isTypeOfControl = (selectedValue && selectedValue.field && selectedValue.field['control'] ===  controlType)
		const isClearAction = (selectedValue.action === null  || selectedValue.action.id === this.CLEAR_ACTION);

		return isTypeOfControl && !isClearAction;
	}

	onTagFilterChange(column, rowIndex, event): void {
		const tags = event.tags || [];

		this.editRows.selectedValues[rowIndex].value =  tags.length ? `[${tags.map((tag) => tag.id).toString()}]` : '[]';
		console.log(column);
		console.log(event);
	}

	update() {
		const edits = this.editRows.selectedValues
			.map((row) => {
				const value = row.action.id === this.CLEAR_ACTION ? null : row.value;

				return {
					fieldName: row.field.id,
					action: row.action.id,
					value: value || '[]'
				}
			});

		this.bulkChangeService.update(1, this.bulkChangeModel.selectedItems , edits)
			.subscribe((result) => {
				console.log(result);
			}, (error) => {
				console.log(error);
			});
	}

	isAllInputEntered(): boolean {
		return this.editRows.selectedValues.every(row => row.domain && row.field && row.action && (row.value || row.action.id === this.CLEAR_ACTION ))
	}
}