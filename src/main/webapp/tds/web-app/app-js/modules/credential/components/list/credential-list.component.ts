// Angular
import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
// Services
import { CredentialService } from '../../service/credential.service';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { UserContextService } from '../../../auth/service/user-context.service';
import { DateUtils } from '../../../../shared/utils/date.utils';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
// Components
import { CredentialViewEditComponent } from '../view-edit/credential-view-edit.component';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { DataGridOperationsHelper } from '../../../../shared/utils/data-grid-operations.helper';
// Models
import { Permission } from '../../../../shared/model/permission.model';
import {
	CredentialColumnModel,
	CredentialModel,
} from '../../model/credential.model';
import { UserContextModel } from '../../../auth/model/user-context.model';
import { DIALOG_SIZE } from '../../../../shared/model/constants';
import {
	GRID_DEFAULT_PAGINATION_OPTIONS,
	GRID_DEFAULT_PAGE_SIZE,
} from '../../../../shared/model/constants';
import {
	COLUMN_MIN_WIDTH,
	ActionType,
	BooleanFilterData,
	DefaultBooleanFilterData,
} from '../../../../shared/model/data-list-grid.model';
// Kendo
import {
	process,
	CompositeFilterDescriptor,
	State,
} from '@progress/kendo-data-query';
import { CellClickEvent, GridDataResult } from '@progress/kendo-angular-grid';
import { ReplaySubject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import {
	COMMON_SHRUNK_COLUMNS_WIDTH,
	COMMON_SHRUNK_COLUMNS,
} from '../../../../shared/constants/common-shrunk-columns';
import {HeaderActionButtonData} from 'tds-component-library';

@Component({
	selector: 'credential-list',
	templateUrl: 'credential-list.component.html',
	styles: [
		`
			#btnCreate {
				margin-left: 16px;
			}
			.action-header {
				width: 100%;
				text-align: center;
			}
		`,
	],
})
export class CredentialListComponent implements OnInit, OnDestroy {
	public disableClearFilters: Function;
	public headerActionButtons: HeaderActionButtonData[];
	protected gridColumns: any[];

	protected state: State = {
		sort: [
			{
				dir: 'asc',
				field: 'name',
			},
		],
		filter: {
			filters: [],
			logic: 'and',
		},
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
	commonShrunkColumns = COMMON_SHRUNK_COLUMNS;
	commonShrunkColumnWidth = COMMON_SHRUNK_COLUMNS_WIDTH;
	unsubscribeOnDestroy$: ReplaySubject<void> = new ReplaySubject(1);
	protected showFilters = false;
	private dataGridOperationsHelper: DataGridOperationsHelper;

	constructor(
		private route: ActivatedRoute,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private credentialService: CredentialService,
		private prompt: UIPromptService,
		private userContext: UserContextService,
		private translateService: TranslatePipe
	) {
		// use partially datagrid operations helper, for the moment just to know the number of filters selected
		// in the future this view should be refactored to use the data grid operations helper
		this.dataGridOperationsHelper = new DataGridOperationsHelper([]);

		this.state.take = this.pageSize;
		this.state.skip = this.skip;
		this.resultSet = this.route.snapshot.data['credentials'];
		this.gridData = process(this.resultSet, this.state);
	}

	ngOnInit() {
		this.disableClearFilters = this.onDisableClearFilter.bind(this);
		this.headerActionButtons = [
			{
				icon: 'plus-circle',
				iconClass: 'is-solid',
				title: this.translateService.transform('CREDENTIAL.CREATE_CREDENTIAL'),
				disabled: !this.isCreateAvailable(),
				show: true,
				onClick: this.onCreate.bind(this),
			},
		];

		this.userContext
			.getUserContext()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((userContext: UserContextModel) => {
				this.dateFormat = DateUtils.translateDateFormatToKendoFormat(
					userContext.dateFormat
				);
				this.credentialColumnModel = new CredentialColumnModel(
					`{0:${this.dateFormat}}`
				);
				this.gridColumns = this.credentialColumnModel.columns.filter(
					column => column.type !== 'action'
				);
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
		const root = this.credentialService.filterColumn(column, this.state);
		this.filterChange(root);
	}

	protected clearValue(column: any): void {
		this.credentialService.clearFilter(column, this.state);
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
		this.credentialService
			.getCredential(credential.id)
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(
				(response: CredentialModel) => {
					this.openCredentialDialogViewEdit(
						response,
						ActionType.EDIT,
						credential
					);
				},
				err => console.log(err)
			);
	}

	/**
	 * Delete the selected API Action
	 * @param dataItem
	 */
	protected onDelete(dataItem: any): void {
		this.prompt
			.open(
				'Confirmation Required',
				'Confirm deletion of ' + dataItem.name + ' credential?',
				'Yes',
				'No'
			)
			.then(res => {
				if (res) {
					this.credentialService
						.deleteCredential(dataItem.id)
						.pipe(takeUntil(this.unsubscribeOnDestroy$))
						.subscribe(
							result => {
								this.reloadData();
							},
							err => console.log(err)
						);
				}
			});
	}

	/**
	 * Catch the Selected Row
	 * @param {SelectionEvent} event
	 */
	protected cellClick(event: CellClickEvent): void {
		if (event.columnIndex > 0) {
			let credential: CredentialModel = event[
				'dataItem'
			] as CredentialModel;
			this.selectRow(credential.id);
			this.credentialService
				.getCredential(credential.id)
				.pipe(takeUntil(this.unsubscribeOnDestroy$))
				.subscribe(
					(response: CredentialModel) => {
						this.openCredentialDialogViewEdit(
							response,
							ActionType.VIEW,
							credential
						);
					},
					err => console.log(err)
				);
		}
	}

	protected reloadData(): void {
		this.credentialService
			.getCredentials()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(
				result => {
					this.resultSet = result;
					this.gridData = process(this.resultSet, this.state);
					setTimeout(() => {
						if (
							this.lastCreatedRecordId &&
							this.lastCreatedRecordId !== 0
						) {
							this.selectRow(this.lastCreatedRecordId);
							let lastCredentialModel = this.gridData.data.find(
								dataItem =>
									dataItem.id === this.lastCreatedRecordId
							);
							this.openCredentialDialogViewEdit(
								lastCredentialModel,
								ActionType.VIEW,
								lastCredentialModel
							);
							this.lastCreatedRecordId = 0;
						}
					}, 500);
				},
				err => console.log(err)
			);
	}

	private reloadItem(originalModel: CredentialModel): void {
		this.credentialService
			.getCredential(originalModel.id)
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(
				(response: CredentialModel) => {
					Object.assign(originalModel, response);
				},
				err => console.log(err)
			);
	}

	/**
	 * Open The Dialog to Create, View or Edit the Api Action
	 * @param {CredentialModel} credentialModel
	 * @param {number} actionType
	 */
	private openCredentialDialogViewEdit(
		credentialModel: CredentialModel,
		actionType: number,
		originalModel?: CredentialModel
	): void {
		this.dialogService
			.open(
				CredentialViewEditComponent,
				[
					{ provide: CredentialModel, useValue: credentialModel },
					{ provide: Number, useValue: actionType },
				],
				DIALOG_SIZE.XLG,
				false
			)
			.then((result: CredentialModel) => {
				if (result && result.id) {
					if (actionType === ActionType.CREATE) {
						this.lastCreatedRecordId = result.id;
						this.reloadData();
					} else {
						this.reloadItem(originalModel);
					}
				}
			})
			.catch(result => {
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
	 * Set on/off the filter icon indicator
	 */
	protected toggleFilter(): void {
		this.showFilters = !this.showFilters;
	}

	/**
	 * Returns the number of distinct currently selected filters
	 */
	protected filterCount(): number {
		return this.dataGridOperationsHelper.getFilterCounter(this.state);
	}

	/**
	 * Determines if there is almost 1 filter selected
	 */
	protected hasFilterApplied(): boolean {
		return this.state.filter.filters.length > 0;
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
	 * Clear all filters
	 */
	protected clearAllFilters(): void {
		this.showFilters = false;
		this.dataGridOperationsHelper.clearAllFilters(this.credentialColumnModel.columns, this.state);
		this.reloadData();
	}

	/**
	 * Disable clear filters
	 */
	private onDisableClearFilter(): boolean {
		return this.filterCount() === 0;
	}
}
