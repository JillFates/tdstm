// Angular
import {Component, ElementRef, Inject, OnInit, Renderer2} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
// Services
import {ProviderService} from '../../service/provider.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UserContextService} from '../../../security/services/user-context.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
// Components
import {ProviderViewEditComponent} from '../view-edit/provider-view-edit.component';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {ProviderAssociatedComponent} from '../provider-associated/provider-associated.component';
// Models
import {COLUMN_MIN_WIDTH, ActionType} from '../../../dataScript/model/data-script.model';
import {ProviderModel, ProviderColumnModel} from '../../model/provider.model';
import {GRID_DEFAULT_PAGINATION_OPTIONS, GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';
import {UserContextModel} from '../../../security/model/user-context.model';
import {ProviderAssociatedModel} from '../../model/provider-associated.model';
// Kendo
import {CompositeFilterDescriptor, State, process} from '@progress/kendo-data-query';
import {CellClickEvent, GridDataResult, PageChangeEvent} from '@progress/kendo-angular-grid';

declare var jQuery: any;

@Component({
	selector: 'provider-list',
	templateUrl: 'provider-list.component.html',
	styles: [`
        #btnCreateProvider { margin-left: 16px; }
	`]
})
export class ProviderListComponent implements OnInit {
	protected gridColumns: any[];

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
	public providerColumnModel = null;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: ProviderModel[];
	public selectedRows = [];
	public dateFormat = '';

	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private providerService: ProviderService,
		private prompt: UIPromptService,
		private route: ActivatedRoute,
		private elementRef: ElementRef,
		private renderer: Renderer2,
		private userContext: UserContextService) {
		this.state.take = this.pageSize;
		this.state.skip = this.skip;
		this.resultSet = this.route.snapshot.data['providers'];
		this.gridData = process(this.resultSet, this.state);
	}

	ngOnInit() {
		this.userContext.getUserContext()
			.subscribe((userContext: UserContextModel) => {
				this.dateFormat = DateUtils.translateDateFormatToKendoFormat(userContext.dateFormat);
				this.providerColumnModel = new ProviderColumnModel(`{0:${this.dateFormat}}`);
				this.gridColumns = this.providerColumnModel.columns.filter((column) => column.type !== 'action');
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
		const root = this.providerService.filterColumn(column, this.state);
		this.filterChange(root);
	}

	protected clearValue(column: any): void {
		this.providerService.clearFilter(column, this.state);
		this.filterChange(this.state.filter);
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
	protected onEdit(dataItem: any): void {
		this.openProviderDialogViewEdit(dataItem, ActionType.EDIT);
	}

	/**
	 * Delete the selected Provider
	 * @param dataItem
	 */
	protected onDelete(dataItem: any): void {
		this.providerService.deleteContext(dataItem.id).subscribe((result: any) => {
			this.dialogService.extra(ProviderAssociatedComponent,
				[{provide: ProviderAssociatedModel, useValue: result}],
				false, false)
				.then((toDelete: any) => {
					if (toDelete) {
						this.providerService.deleteProvider(dataItem.id).subscribe(
							(result) => {
								this.reloadData();
							},
							(err) => console.log(err));
					}
				})
				.catch(error => console.log('Closed'));
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

	protected reloadData(): void {
		this.providerService.getProviders().subscribe(
			(result) => {
				this.resultSet = result;
				this.gridData = process(this.resultSet, this.state);
				setTimeout(() => this.forceDisplayLastRowAddedToGrid() , 100);
			},
			(err) => console.log(err));
	}

	/**
	 * This work as a temporary fix.
	 * TODO: talk when Jorge Morayta get's back to do a proper/better fix.
	 */
	private forceDisplayLastRowAddedToGrid(): void {
		const lastIndex = this.gridData.data.length - 1;
		let target = this.elementRef.nativeElement.querySelector(`tr[data-kendo-grid-item-index="${lastIndex}"]`);
		this.renderer.setStyle(target, 'height', '36px');
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
			this.reloadData();
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	private selectRow(dataItemId: number): void {
		this.selectedRows = [];
		this.selectedRows.push(dataItemId);
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

	/**
	 * Manage Pagination
	 * @param {PageChangeEvent} event
	 */
	public pageChange(event: any): void {
		this.skip = event.skip;
		this.state.skip = this.skip;
		this.state.take = event.take || this.state.take;
		this.pageSize = this.state.take;
		this.gridData = process(this.resultSet, this.state);
		// Adjusting the locked column(s) height to prevent cut-off issues.
		jQuery('.k-grid-content-locked').addClass('element-height-100-per-i');
	}
}