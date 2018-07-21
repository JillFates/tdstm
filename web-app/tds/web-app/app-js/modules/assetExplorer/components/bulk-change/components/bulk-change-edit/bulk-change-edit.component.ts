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

@Component({
	selector: 'tds-bulk-change-edit',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/bulk-change/components/bulk-change-edit/bulk-change-edit.component.html'
})
export class BulkChangeEditComponent extends UIExtraDialog implements OnInit {
	actions: ListOption[];
	assetClassList: ListOption[];
	selectedItems: string[] = [];
	gridColumns: BulkChangeEditColumnsModel;
	gridSettings: DataGridOperationsHelper;
	affectedAssets: number;
	commonFieldSpecs: any[] = [];
	editRows: { actions: BulkEditAction[], selectedValues: {domain: ListOption, field: ListOption, action: ListOption}[]};

	constructor(private bulkChangeModel: BulkChangeModel,
				private promptService: UIPromptService,
				private assetExplorerService: AssetExplorerService,
				private permissionService: PermissionService,
				private customDomainService: CustomDomainService) {
		super('#bulk-change-edit-component');
		this.affectedAssets = this.bulkChangeModel.selectedItems.length;
		console.log('Selected items');
	}

	addHandler({sender}): void {
		this.editRows.actions.push({domain: 'APPLICATION', actions: [...this.actions], fields: [] });
		this.editRows.selectedValues.push({domain: null, field: null, action: null});
		this.gridSettings.loadPageData();
	}

	removeHandler({dataItem, rowIndex}): void {
		this.editRows.actions.splice(rowIndex, 1);
		this.editRows.selectedValues.splice(rowIndex, 1);
		this.gridSettings.loadPageData();
	}


	ngOnInit() {
		this.actions = [
			{ id: 'add', text: 'Add to existing'},
			{ id: 'clear', text: 'Clear field'},
			{ id: 'replace', text: 'Replace with'},
			{ id: 'remove', text: 'Remove these'}
		];
		this.editRows = { actions: [], selectedValues: [] };

		this.customDomainService.getCommonFieldSpecs()
			.subscribe((results) => {
				console.log('The results are');
				console.log(results);
				this.commonFieldSpecs = results;
			});

		this.assetClassList = ['COMMON', 'APPLICATION', 'DATABASE', 'DEVICE', 'STORAGE']
			.map((domain): ListOption => ({id: domain, text: `${domain} fields`}));

		this.gridColumns = new BulkChangeEditColumnsModel();
		this.gridSettings = new DataGridOperationsHelper(this.editRows.actions,
			[], // initial sort config.
			{ mode: 'single', checkboxOnly: false}, // selectable config.
			{ useColumn: 'id' }); // checkbox config.
		// this.addEditAction();
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
		alert('Editing');
	}

	hasAssetDeletePermission(): boolean {
		return this.permissionService.hasPermission(Permission.AssetDelete);
	}

	onDomainChange(domain: ListOption, index: number) {
		const {actions, selectedValues} = this.editRows;

		actions[index].fields =  this.getFieldsByDomain(domain);
		selectedValues[index].field = null
	}

	/*
	onFieldChange(field: ListOption) {
		console.log('changing field');
		console.log(field);
	}
	*/

	getFieldsByDomain(domain: ListOption): ListOption[] {
		let fields: ListOption[] = [];
		if (!domain) {
			return fields;
		}

		const domainFields =  this.commonFieldSpecs.find((field: any) => field.domain === domain.id);

		if (domainFields && domainFields.fields) {
			fields = domainFields.fields.map((item: any) => ({id: item.field, text: item.label}));
		}
		console.log('FIELDS');
		console.log(fields);

		return fields;
	}
}