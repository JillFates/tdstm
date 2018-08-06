import {Component, Inject, OnInit} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {process, CompositeFilterDescriptor, State} from '@progress/kendo-data-query';
import {CellClickEvent, RowArgs, GridDataResult} from '@progress/kendo-angular-grid';

import {DataIngestionService} from '../../service/data-ingestion.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {Permission} from '../../../../shared/model/permission.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {CredentialColumnModel, CredentialModel} from '../../model/credential.model';
import {
	COLUMN_MIN_WIDTH,
	Flatten,
	ActionType,
	BooleanFilterData,
	DefaultBooleanFilterData
} from '../../../../shared/model/data-list-grid.model';
import {CredentialViewEditComponent} from '../credential-view-edit/credential-view-edit.component';
import {DIALOG_SIZE} from '../../../../shared/model/constants';
import {GRID_DEFAULT_PAGINATION_OPTIONS, GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';
import {PreferenceService} from '../../../../shared/services/preference.service';

@Component({
	selector: 'credential-list',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/credential-list/credential-list.component.html',
	styles: [`
		#btnCreate { margin-left: 16px; }
		.action-header { width:100%; text-align:center; }
	`]
})
export class CredentialListComponent implements OnInit {

	private state: State = {
		sort: [{
			dir: 'asc',
			field: 'name'
		}],
		filter: {
			filters: [],
			logic: 'and'
		}
	};
	public skip = 0;
	public pageSize = GRID_DEFAULT_PAGE_SIZE;
	public defaultPageOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	public credentialColumnModel = null;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: CredentialModel[];
	public selectedRows = [];
	public booleanFilterData = BooleanFilterData;
	public defaultBooleanFilterData = DefaultBooleanFilterData;
	private lastCreatedRecordId = 0;
	public dateFormat = '';

	constructor(
		private dialogService: UIDialogService,
		@Inject('credentials') credentials: Observable<CredentialModel[]>,
		private permissionService: PermissionService,
		private dataIngestionService: DataIngestionService,
		private prompt: UIPromptService,
		private preferenceService: PreferenceService) {
			this.state.take = this.pageSize;
			this.state.skip = this.skip;
			credentials
			.subscribe((result) => {
					this.resultSet = result;
					this.gridData = process(this.resultSet, this.state);
				},
				(err) => console.log(err));
	}

	ngOnInit() {
		this.preferenceService.getUserDatePreferenceAsKendoFormat()
			.subscribe((dateFormat) => {
				this.dateFormat = dateFormat;
				this.credentialColumnModel = new CredentialColumnModel(`{0:${dateFormat}}`);
			});
	}

	protected filterChange(filter: CompositeFilterDescriptor): void {
		this.state.filter = filter;
		this.gridData = process(this.resultSet, this.state);
	}

	protected sortChange(sort): void {
		this.state.sort = sort;
		this.gridData = process(this.resultSet, this.state);
	}

	protected onFilter(column: any): void {
		const root = this.dataIngestionService.filterColumn(column, this.state);
		this.filterChange(root);
	}

	protected clearValue(column: any): void {
		this.dataIngestionService.clearFilter(column, this.state);
		this.filterChange(this.state.filter);
	}

	/**
	 * Create a new API Action
	 */
	protected onCreate(): void {
		let creationModel = new CredentialModel();
		this.openCredentialDialogViewEdit(creationModel, ActionType.CREATE);
	}

	/**
	 * Select the current element and open the Edit Dialog
	 * @param {ModalType} type
	 * @param dataItem
	 */
	protected onEdit(dataItem: any): void {
		let credential: CredentialModel = dataItem;
		this.dataIngestionService.getCredential(credential.id).subscribe( (response: CredentialModel) => {
			this.openCredentialDialogViewEdit(response, ActionType.VIEW, credential);
		}, err => console.log(err));
	}

	/**
	 * Delete the selected API Action
	 * @param dataItem
	 */
	protected onDelete(dataItem: any): void {
		this.prompt.open('Confirmation Required', 'Confirm deletion of ' + dataItem.name + ' credential?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.dataIngestionService.deleteCredential(dataItem.id).subscribe(
						(result) => {
							this.reloadData();
						},
						(err) => console.log(err));
				}
			});
	}

	/**
	 * Catch the Selected Row
	 * @param {SelectionEvent} event
	 */
	protected cellClick(event: CellClickEvent): void {
		if (event.columnIndex > 0) {
			let credential: CredentialModel = event['dataItem'] as CredentialModel;
			this.selectRow(credential.id);
			this.dataIngestionService.getCredential(credential.id).subscribe( (response: CredentialModel) => {
				this.openCredentialDialogViewEdit(response, ActionType.VIEW, credential);
			}, err => console.log(err));
		}
	}

	protected reloadData(): void {
		this.dataIngestionService.getCredentials().subscribe(
			(result) => {
				this.resultSet = result;
				this.gridData = process(this.resultSet, this.state);
				setTimeout(() => {
					if (this.lastCreatedRecordId && this.lastCreatedRecordId !== 0) {
						this.selectRow(this.lastCreatedRecordId);
						let lastCredentialModel = this.gridData.data.find((dataItem) => dataItem.id === this.lastCreatedRecordId);
						this.openCredentialDialogViewEdit(lastCredentialModel, ActionType.VIEW, lastCredentialModel);
						this.lastCreatedRecordId = 0;
					}
				}, 500);
			},
			(err) => console.log(err));
	}

	private reloadItem(originalModel: CredentialModel): void {
		this.dataIngestionService.getCredential(originalModel.id).subscribe( (response: CredentialModel) => {
			Object.assign(originalModel, response);
		}, err => console.log(err));
	}

	/**
	 * Open The Dialog to Create, View or Edit the Api Action
	 * @param {CredentialModel} credentialModel
	 * @param {number} actionType
	 */
	private openCredentialDialogViewEdit(credentialModel: CredentialModel, actionType: number, originalModel?: CredentialModel): void {
		this.dialogService.open(CredentialViewEditComponent, [
			{ provide: CredentialModel, useValue: credentialModel },
			{ provide: Number, useValue: actionType }
		], DIALOG_SIZE.XLG, false).then( (result: CredentialModel) => {
			if (result && result.id) {
				if (actionType === ActionType.CREATE) {
					this.lastCreatedRecordId = result.id;
					this.reloadData();
				} else {
					this.reloadItem(originalModel);
				}
			}
		}).catch(result => {
			this.reloadData();
			console.log('Dismissed Dialog');
		});
	}

	private selectRow(dataItemId: number): void {
		this.selectedRows = [];
		this.selectedRows.push(dataItemId);
	}

	// Permissions
	protected isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ActionCreate);
	}

	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ActionEdit);
	}

	protected isDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ActionDelete);
	}

	/**
	 * Make the entire header clickable on Grid
	 * @param event: any
	 */
	public onClickTemplate(event: any): void {
		if (event.target && event.target.parentNode) {
			event.target.parentNode.click();
		}
	}

	public pageChange(event: any): void {
		this.skip = event.skip;
		this.state.skip = this.skip;
		this.state.take = event.take || this.state.take;
		this.pageSize = this.state.take;
		this.gridData = process(this.resultSet, this.state);
	}
}