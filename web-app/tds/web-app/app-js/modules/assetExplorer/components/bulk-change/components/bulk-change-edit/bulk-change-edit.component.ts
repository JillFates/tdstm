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
import {BulkEditAction, ListOption} from '../../model/bulk-change.model';
import {SortUtils} from '../../../../../../shared/utils/sort.utils';
import {StringUtils} from '../../../../../../shared/utils/string.utils';
import {BulkChangeService} from '../../../../service/bulk-change.service';
import {TagService} from '../../../../../assetTags/service/tag.service';
import {TagModel} from '../../../../../assetTags/model/tag.model';
import {ApiResponseModel} from '../../../../../../shared/model/ApiResponseModel';

@Component({
	selector: 'tds-bulk-change-edit',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/bulk-change/components/bulk-change-edit/bulk-change-edit.component.html'
})
export class BulkChangeEditComponent extends UIExtraDialog implements OnInit {
	COLUMN_MIN_WIDTH = 120;
	tagList: TagModel[] = [];
	yesNoList: ListOption[] = [];
	actions: ListOption[] = [];
	assetClassList: ListOption[] = [];
	selectedItems: string[] = [];
	commonFieldSpecs: any[] = [];
	gridColumns: BulkChangeEditColumnsModel;
	gridSettings: DataGridOperationsHelper;
	affectedAssets: number;
	editRows: { actions: BulkEditAction[], selectedValues: {domain: ListOption, field: ListOption, action: ListOption, value: any}[] };

	constructor(private bulkChangeModel: BulkChangeModel,
				private promptService: UIPromptService,
				private assetExplorerService: AssetExplorerService,
				private permissionService: PermissionService,
				private customDomainService: CustomDomainService,
				private bulkChangeService: BulkChangeService,
				private tagService: TagService) {
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
		this.yesNoList = [
			{ id: '?', text: '?'},
			{ id: 'Y', text: 'Yes'},
			{ id: 'N', text: 'No'}
		];

		this.actions = [
			{ id: 'add', text: 'Add to existing'},
			{ id: 'clear', text: 'Clear field'},
			{ id: 'replace', text: 'Replace with'},
			{ id: 'remove', text: 'Remove these'}
		];
		this.editRows = { actions: [], selectedValues: [] };
		this.addRow();

		this.customDomainService.getCommonFieldSpecs()
			.subscribe((results) => {
				this.commonFieldSpecs = results;
			});

		this.tagService.getTags()
			.subscribe((result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS && result.data) {
					this.tagList = result.data;
				}
			}, error => console.log('error on GET Tag List', error));

		this.assetClassList = ['common', 'application', 'database', 'device', 'storage']
			.map((domain): ListOption => ( {id: domain, text: `${StringUtils.toCapitalCase(domain, false)} Fields`}  ));

		this.gridColumns = new BulkChangeEditColumnsModel();
		this.gridSettings = new DataGridOperationsHelper(this.editRows.actions,
			[], // initial sort config.
			{ mode: 'single', checkboxOnly: false}, // selectable config.
			{ useColumn: 'id' }); // checkbox config.
	}

	/**
	 * Close the Dialog
	 */
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

	onDomainChange(domain: ListOption, index: number) {
		const {actions, selectedValues} = this.editRows;

		actions[index].fields =  this.getFieldsByDomain(domain);
		selectedValues[index].field = null
	}

	getFieldsByDomain(domain: ListOption): ListOption[] {
		let fields: ListOption[] = [];
		if (!domain) {
			return fields;
		}

		const domainFields =  this.commonFieldSpecs.find((field: any) => field.domain === domain.id.toUpperCase());
		if (domainFields && domainFields.fields) {
			fields = domainFields.fields.map((item: any) => ({id: item.field, text: item.label, control: item.control}));
		}

		return fields.sort((a, b) => SortUtils.compareByProperty(a, b, 'text'));
	}

	isControl(controlType: string, rowIndex): boolean {
		return this.editRows.selectedValues[rowIndex] && this.editRows.selectedValues[rowIndex].field && this.editRows.selectedValues[rowIndex].field['control'] ===  controlType;
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
				return {
					fieldName: row.field.id,
					action: row.action.id,
					value: row.value || '[]'
				}
			});

		this.bulkChangeService.update(1, this.bulkChangeModel.selectedItems , edits)
			.subscribe((result) => {
				console.log('Getting the results');
				console.log(result);
			}, (error) => {
				console.log('here we have an error');
				console.log(error);
			});

		console.log('EDITS ARE');
		console.log(edits);
		console.log('--------------');
	}

}