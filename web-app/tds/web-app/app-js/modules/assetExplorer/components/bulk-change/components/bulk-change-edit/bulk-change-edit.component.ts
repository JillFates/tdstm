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
	defaultAssetClass: IdTextItem = {id: 'common', text: 'Common Fields'};
	tagList: TagModel[] = [];
	yesNoList: IdTextItem[] = [{ id: '?', text: '?'}, { id: 'Y', text: 'Yes'}, { id: 'N', text: 'No'}];
	actions: IdTextItem[] = [];
	assetClassList: IdTextItem[] = [];
	selectedItems: string[] = [];
	commonFieldSpecs: any[] = [];
	gridColumns: BulkChangeEditColumnsModel;
	gridSettings: DataGridOperationsHelper;
	affectedAssets: number;
	editRows: { actions: BulkEditAction[], selectedValues: {domain: IdTextItem, field: IdTextItem, action: IdTextItem, value: any}[] };

	constructor(
		private bulkChangeModel: BulkChangeModel, private promptService: UIPromptService, private assetExplorerService: AssetExplorerService,
		private permissionService: PermissionService, private customDomainService: CustomDomainService, private bulkChangeService: BulkChangeService,
		private tagService: TagService, private translatePipe: TranslatePipe
	) {
		super('#bulk-change-edit-component');
		this.affectedAssets = this.bulkChangeModel.affected;
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

	closeDialog(bulkActionResult: BulkActionResult): void {
			this.close(bulkActionResult);
	}

	ngOnInit() {
		this.isLoaded = false;
		this.editRows = { actions: [], selectedValues: [] };
		this.gridColumns = new BulkChangeEditColumnsModel();

		Observable.forkJoin(this.bulkChangeService.getActions(), this.customDomainService.getCommonFieldSpecs(), this.tagService.getTags())
			.subscribe((result: any[]) => {
				const [actions, fields, tagAssets] = result;

				this.actions = Object.keys(actions.data['asset-tag-selector'] )
					.map((action) => ({id: action, text: this.translatePipe.transform(`ASSET_EXPLORER.BULK_CHANGE.ACTIONS.${action.toUpperCase()}`) })) ;

				this.commonFieldSpecs = fields;
				this.assetClassList = this.getAssetClassList(this.commonFieldSpecs);

				if (tagAssets.status === ApiResponseModel.API_SUCCESS && tagAssets.data) {
					this.tagList = tagAssets.data;
				}

				this.addRow();
				this.gridSettings = new DataGridOperationsHelper(this.editRows.actions,
					[], // initial sort config.
					{ mode: 'single', checkboxOnly: false},
					{ useColumn: 'id' });
				this.isLoaded = true;
			});
	}

	cancelCloseDialog(bulkActionResult: BulkActionResult): void {
		this.dismiss(bulkActionResult || {action: null, success: false});
	}

	// show control if this belongs to class and clear action is not selected
	canShowControl(controlType: string, rowIndex): boolean {
		const selectedValue = this.editRows.selectedValues[rowIndex];

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

	onDomainValueChange(domain: IdTextItem, index: number): void {
		const {actions, selectedValues} = this.editRows;
		selectedValues[index].domain = domain;

		actions[index].fields =  this.getFieldsByDomain(domain);
		selectedValues[index].field = null;
	}

	onFieldValueChange(field: IdTextItem, index: number): void {
		const {selectedValues} = this.editRows;

		selectedValues[index].field = field;
	}

	onActionValueChange(action: IdTextItem, index: number): void {
		const {selectedValues} = this.editRows;

		selectedValues[index].action = action;
	}

	private addRow(): any {
		this.editRows.actions.push({domains: this.assetClassList , actions: [...this.actions], fields: [] });
		this.editRows.selectedValues.push({domain: this.defaultAssetClass, field: null, action: null, value: null});

		//  get the fields for the default domain
		this.editRows.actions[this.editRows.actions.length - 1].fields =  this.getFieldsByDomain(this.defaultAssetClass);
	}

	private getAssetClassList(fields: any[]): IdTextItem[] {
		const assetClassList = fields
			.map((field) => field.domain.toLowerCase())
			.map((domain): IdTextItem => ( {id: domain, text: `${StringUtils.toCapitalCase(domain, false)} Fields`}  ));

		return assetClassList;
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
		let fields: IdTextItem[] = [];
		if (!domain) {
			return fields;
		}

		const domainFields =  this.commonFieldSpecs.find((field: any) => field.domain === domain.id.toUpperCase());
		if (domainFields && domainFields.fields) {
			fields = domainFields.fields
				.filter((item) => item.control === 'asset-tag-selector') // REMOVE THIS LiNE WHEN EDITION SERVICES ARE IMPLEMENTED FOR ALL FIELDS
				.map((item: any) => ({id: item.field, text: item.label, control: item.control}))
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