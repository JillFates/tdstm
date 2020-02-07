// Angular
import {Component, HostListener, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
// Services
import {APIActionService} from '../../service/api-action.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import { DataGridOperationsHelper } from '../../../../shared/utils/data-grid-operations.helper';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Components
import {APIActionViewEditComponent} from '../view-edit/api-action-view-edit.component';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import { HeaderActionButtonData } from 'tds-component-library';
// Models
import {GRID_DEFAULT_PAGINATION_OPTIONS, GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';
import {APIActionColumnModel, APIActionModel} from '../../model/api-action.model';
import {Permission} from '../../../../shared/model/permission.model';
import {GridColumnModel} from '../../../../shared/model/data-list-grid.model';
import {
	COLUMN_MIN_WIDTH,
	ActionType,
	BooleanFilterData,
	DefaultBooleanFilterData
} from '../../../../shared/model/data-list-grid.model';
import {DIALOG_SIZE, INTERVAL} from '../../../../shared/model/constants';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {APIActionType} from '../../model/api-action.model';
// Kendo
import {process, CompositeFilterDescriptor, State} from '@progress/kendo-data-query';
import {CellClickEvent, GridDataResult} from '@progress/kendo-angular-grid';
import {ReplaySubject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {COMMON_SHRUNK_COLUMNS, COMMON_SHRUNK_COLUMNS_WIDTH} from '../../../../shared/constants/common-shrunk-columns';
import {pathOr} from 'ramda';

@Component({
	selector: 'api-action-list',
	templateUrl: 'api-action-list.component.html',
	styles: [`
		#btnCreate { margin-left: 16px; }
		.action-header { width:100%; text-align:center; }
	`]
})
export class APIActionListComponent implements OnInit, OnDestroy {
	public disableClearFilters: Function;
	public headerActionButtons: HeaderActionButtonData[];
	public dataGridOperationsHelpter: DataGridOperationsHelper;
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
	public apiActionColumnModel: APIActionColumnModel = null;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: APIActionModel[];
	public selectedRows = [];
	public booleanFilterData = BooleanFilterData;
	public defaultBooleanFilterData = DefaultBooleanFilterData;
	private interval = INTERVAL;
	private openLastItemId = 0;
	public dateFormat = '';
	protected createActionText = '';
	protected hasEarlyAccessTMRPermission: boolean;
	commonShrunkColumns = COMMON_SHRUNK_COLUMNS.filter((column: string) => !['Created', 'Last Updated'].includes(column) );
	commonShrunkColumnWidth = COMMON_SHRUNK_COLUMNS_WIDTH;
	unsubscribeOnDestroy$: ReplaySubject<void> = new ReplaySubject(1);
	public isFiltering  = false;
	GRID_DEFAULT_PAGINATION_OPTIONS = GRID_DEFAULT_PAGINATION_OPTIONS;

	constructor(
		private route: ActivatedRoute,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private apiActionService: APIActionService,
		private prompt: UIPromptService,
		private userContext: UserContextService,
		private translate: TranslatePipe) {
		this.hasEarlyAccessTMRPermission = this.permissionService.hasPermission(Permission.EarlyAccessTMR);
		this.state.take = this.pageSize;
		this.state.skip = this.skip;
		this.resultSet = this.route.snapshot.data['apiActions'];
		this.gridData = process(this.resultSet, this.state);
		this.createActionText = this.translate.transform('API_ACTION.CREATE_ACTION');
	}

	ngOnInit() {
		this.disableClearFilters = this.onDisableClearFilter.bind(this);
		this.dataGridOperationsHelpter = new DataGridOperationsHelper([]);
		this.headerActionButtons = [
			{
				icon: 'plus',
				iconClass: 'is-solid',
				title: this.translate.transform('GLOBAL.CREATE'),
				disabled: !this.isCreateAvailable(),
				show: true,
				onClick: this.onCreate.bind(this),
			},
		];

		this.userContext.getUserContext()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((userContext: UserContextModel) => {
				this.dateFormat = DateUtils.translateDateFormatToKendoFormat(userContext.dateFormat);
				this.apiActionColumnModel = new APIActionColumnModel(`{0:${this.dateFormat}}`);
				this.gridColumns = this.apiActionColumnModel.columns.filter((column) => column.type !== 'action');
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

	protected onFilter(value: string | Date, column: any): void {
		column.filter = value;
		const root = GridColumnModel.filterColumn(column, this.state);
		// clear (string/date) values
		root.filters = (root.filters || []).filter((filter: any) => filter.value !== '');
		// clear boolean filter value
		if (column.filter === '' && column.type === 'boolean') {
			root.filters = root.filters.filter((filter: any) => filter.field !== column.property);
		}
		this.filterChange(root);
	}

	/**
	 * Create a new API Action
	 */
	protected onCreate(): void {
		let apiActionModel = new APIActionModel();
		this.openAPIActionDialogViewEdit(apiActionModel, ActionType.CREATE);
	}

	/**
	 * Select the current element and open the Edit Dialog
	 * @param {ModalType} type
	 * @param dataItem
	 */
	protected onEdit(dataItem: any): void {
		let apiAction: APIActionModel = dataItem as APIActionModel;
		this.apiActionService.getAPIAction(apiAction.id)
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((response: APIActionModel) => {
			this.openAPIActionDialogViewEdit(response, ActionType.EDIT, apiAction);
		}, error => console.log(error));
	}

	/**
	 * Delete the selected API Action
	 * @param dataItem
	 */
	protected onDelete(dataItem: any): void {
		this.prompt.open('Confirmation Required', 'Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.apiActionService.deleteAPIAction(dataItem.id)
						.pipe(takeUntil(this.unsubscribeOnDestroy$))
						.subscribe(
						(result) => {
							this.reloadData();
						},
						(err) => console.log(err));
				}
			});
	}

	/**
	 * Catch the Selected Row
	 * @param {SelectionEvent} dataItem
	 */
	protected cellClick(dataItem: any): void {
		let apiAction: APIActionModel = dataItem as APIActionModel;
		this.selectRow(apiAction.id);
		this.apiActionService.getAPIAction(apiAction.id)
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((response: APIActionModel) => {
				this.openAPIActionDialogViewEdit(response, ActionType.VIEW, apiAction);
		}, error => console.log(error));
	}

	protected reloadData(): void {
		this.apiActionService.getAPIActions()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(
			(result) => {
				this.resultSet = result;
				this.gridData = process(this.resultSet, this.state);

				if (this.openLastItemId !== 0) {
					setTimeout(() => {
						this.selectRow(this.openLastItemId);
						let lastApiActionModel = this.gridData.data.find((dataItem) => dataItem.id === this.openLastItemId);
						this.openLastItemId = 0;
						this.openAPIActionDialogViewEdit(lastApiActionModel, ActionType.VIEW, lastApiActionModel);
					}, 700);
				}
			},
			(err) => console.log(err));
	}

	private reloadItem(originalModel: APIActionModel): void {
		this.apiActionService.getAPIAction(originalModel.id)
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((response: APIActionModel) => {
				Object.assign(originalModel, response);
		}, error => console.log(error));
	}

	/**
	 * Open The Dialog to Create, View or Edit the Api Action
	 * @param {APIActionModel} apiActionModel
	 * @param {number} actionType
	 */
	private openAPIActionDialogViewEdit(
		apiActionModel: APIActionModel,
		actionType: number,
		originalModel?: APIActionModel): void {
		this.dialogService.open(APIActionViewEditComponent, [
			{ provide: APIActionModel, useValue: apiActionModel },
			{ provide: Number, useValue: actionType }
		], DIALOG_SIZE.XLG, false).then(result => {
			if (result) {
				if (actionType === ActionType.CREATE) {
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

	/**
	 * unsubscribe from all subscriptions on destroy hook.
	 * @HostListener decorator ensures the OnDestroy hook is called on events like
	 * Page refresh, Tab close, Browser close, navigation to another view.
	 */
	@HostListener('window:beforeunload')
	ngOnDestroy(): void {
		this.unsubscribeOnDestroy$.next();
		this.unsubscribeOnDestroy$.complete();
	}

	/**
	 * Toggle the flag to show/hide grid filters
	 */
	public toggleFiltering() {
		this.isFiltering = !this.isFiltering;
	}

	/**
	 * Get the current number of filters selected
	 */
	public filterCounter(): number {
		return GridColumnModel.getFilterCounter(this.state);
	}

	/**
	 * Clear all filters
	 */
	protected clearAllFilters(): void {
		this.dataGridOperationsHelpter.clearAllFilters(this.apiActionColumnModel.columns, this.state);
		this.reloadData();
	}

	/**
	 * Disable clear filters
	 */
	private onDisableClearFilter(): boolean {
		return this.filterCounter() === 0;
	}

}
