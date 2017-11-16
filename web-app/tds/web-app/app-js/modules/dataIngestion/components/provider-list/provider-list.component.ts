import {Component, Inject} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {filterBy, CompositeFilterDescriptor} from '@progress/kendo-data-query';
import {CellClickEvent, RowArgs} from '@progress/kendo-angular-grid';

import {DataIngestionService} from '../../service/data-ingestion.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {COLUMN_MIN_WIDTH, Flatten, ActionType} from '../../model/data-script.model';
import {ProviderModel, ProviderColumnModel} from '../../model/provider.model';
import {ProviderViewEditComponent} from '../provider-view-edit/provider-view-edit.component';

@Component({
	selector: 'provider-list',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/provider-list/provider-list.component.html',
	styles: [`
        #btnCreateProvider { margin-left: 16px; }
	`]
})
export class ProviderListComponent {

	public filter: CompositeFilterDescriptor;
	public providerColumnModel = new ProviderColumnModel();
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: any[];
	public resultSet: ProviderModel[];
	public selectedRows = [];
	public isRowSelected = (e: RowArgs) => this.selectedRows.indexOf(e.dataItem.id) >= 0;

	constructor(
		private dialogService: UIDialogService,
		@Inject('providers') providers: Observable<ProviderModel[]>,
		private permissionService: PermissionService,
		private dataIngestionService: DataIngestionService,
		private prompt: UIPromptService) {
		providers.subscribe(
			(result) => {
				this.resultSet = result;
				this.gridData = filterBy(this.resultSet, this.filter);
			},
			(err) => console.log(err));
	}

	protected filterChange(filter: CompositeFilterDescriptor): void {
		this.filter = filter;
		this.gridData = filterBy(this.resultSet, filter);
	}

	protected onFilter(column: any): void {
		let root = this.filter || { logic: 'and', filters: []};

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

	protected clearValue(column: any, value?: any): void {
		column.filter = '';
		if (this.filter && this.filter.filters.length > 0) {
			const filterIndex = this.filter.filters.findIndex((r: any) => r.field === column.property);
			this.filter.filters.splice(filterIndex, 1);
			this.filterChange(this.filter);
		}
	}

	protected onCreateProvider(): void {
		let providerModel: ProviderModel = {
			name: '',
			description: '',
			comment: ''
		};
		this.openProviderDialogViewEdit(providerModel, ActionType.CREATE);
	}

	/**
	 * Select the current element and open the Edit Dialog
	 * @param dataItem
	 */
	protected onEditProvider(dataItem: any): void {
		this.openProviderDialogViewEdit(dataItem, ActionType.EDIT);
	}

	/**
	 * Delete the selected Provider
	 * @param dataItem
	 */
	protected onDeleteProvider(dataItem: any): void {
		this.prompt.open('Confirmation Required', 'There are associated Datasources. Deleting this will not delete historical imports. Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.dataIngestionService.deleteProvider(dataItem.id).subscribe(
						(result) => {
							this.reloadProviders();
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
			this.openProviderDialogViewEdit(event['dataItem'], ActionType.VIEW);
		}
	}

	protected reloadProviders(): void {
		this.dataIngestionService.getProviders().subscribe(
			(result) => {
				this.resultSet = result;
				this.gridData = filterBy(this.resultSet, this.filter);
			},
			(err) => console.log(err));
	}

	/**
	 * Open The Dialog to Create, View or Edit the Provider
	 * @param {ProviderModel} providerModel
	 * @param {number} actionType
	 */
	private openProviderDialogViewEdit(providerModel: ProviderModel, actionType: number): void {
		this.dialogService.open(ProviderViewEditComponent, [
			{ provide: ProviderModel, useValue: providerModel },
			{ provide: Number, useValue: actionType}
		]).then(result => {
			// update the list to reflect changes, it keeps the filter
			this.reloadProviders();
			if (actionType === ActionType.CREATE) {
				setTimeout(() => {
					this.selectRow(result.provider.id);
					this.openProviderDialogViewEdit(result.provider, ActionType.VIEW);
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
}