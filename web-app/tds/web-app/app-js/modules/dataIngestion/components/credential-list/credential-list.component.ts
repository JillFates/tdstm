import {Component, Inject} from '@angular/core';
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

@Component({
	selector: 'credential-list',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/credential-list/credential-list.component.html',
	styles: [`
		#btnCreate { margin-left: 16px; }
		.action-header { width:100%; text-align:center; }
	`]
})
export class CredentialListComponent {

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

	public credentialColumnModel = new CredentialColumnModel();
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: CredentialModel[];
	public selectedRows = [];
	public isRowSelected = (e: RowArgs) => this.selectedRows.indexOf(e.dataItem.id) >= 0;
	public booleanFilterData = BooleanFilterData;
	public defaultBooleanFilterData = DefaultBooleanFilterData;

	constructor(
		private dialogService: UIDialogService,
		@Inject('credentials') credentials: Observable<CredentialModel[]>,
		private permissionService: PermissionService,
		private dataIngestionService: DataIngestionService,
		private prompt: UIPromptService) {
		credentials.subscribe(
			(result) => {
				this.resultSet = result;
				this.gridData = process(this.resultSet, this.state);
			},
			(err) => console.log(err));
	}

	protected filterChange(filter: CompositeFilterDescriptor): void {
		console.log(filter);
		this.state.filter = filter;
		this.gridData = process(this.resultSet, this.state);
	}

	protected sortChange(sort): void {
		this.state.sort = sort;
		this.gridData = process(this.resultSet, this.state);
	}

	protected onFilter(column: any): void {
		let root = this.state.filter || { logic: 'and', filters: [] };

		let [filter] = Flatten(root).filter(x => x.field === column.property);

		if (!column.filter) {
			column.filter = '';
		}

		if (column.type === 'text') {
			if (!filter) {
				root.filters.push({
					field: column.property,
					operator: 'contains',
					value: column.filter,
					ignoreCase: true
				});
			} else {
				filter = root.filters.find((r) => {
					return r['field'] === column.property;
				});
				filter.value = column.filter;
			}
		}

		if (column.type === 'date') {
			if (!filter) {
				root.filters.push({
					field: column.property,
					operator: 'gte',
					value: column.filter,
				});
			} else {
				filter = root.filters.find((r) => {
					return r['field'] === column.property;
				});
				filter.value = column.filter;
			}
		}

		this.filterChange(root);
	}

	protected clearValue(column: any): void {
		column.filter = '';
		if (this.state.filter && this.state.filter.filters.length > 0) {
			const filterIndex = this.state.filter.filters.findIndex((r: any) => r.field === column.property);
			this.state.filter.filters.splice(filterIndex, 1);
			this.filterChange(this.state.filter);
		}
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
		this.openCredentialDialogViewEdit(dataItem, ActionType.EDIT);
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
			this.selectRow(event['dataItem'].id);
			this.openCredentialDialogViewEdit(event['dataItem'], ActionType.VIEW);
		}
	}

	protected reloadData(): void {
		this.dataIngestionService.getCredentials().subscribe(
			(result) => {
				this.resultSet = result;
				this.gridData = process(this.resultSet, this.state);
			},
			(err) => console.log(err));
	}

	/**
	 * Open The Dialog to Create, View or Edit the Api Action
	 * @param {CredentialModel} credentialModel
	 * @param {number} actionType
	 */
	private openCredentialDialogViewEdit(credentialModel: CredentialModel, actionType: number): void {
		this.dialogService.open(CredentialViewEditComponent, [
			{ provide: CredentialModel, useValue: credentialModel },
			{ provide: Number, useValue: actionType }
		], DIALOG_SIZE.XLG, false).then(result => {
			this.reloadData();
			if (actionType === ActionType.CREATE) {
				setTimeout(() => {
					this.selectRow(result.id);
					let lastCredentialModel = this.gridData.data.find((dataItem) => dataItem.id === result.id);
					this.openCredentialDialogViewEdit(lastCredentialModel, ActionType.VIEW);
				}, 500);
			}
		}).catch(result => {
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
}