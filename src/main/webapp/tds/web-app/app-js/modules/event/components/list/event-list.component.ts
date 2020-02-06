// Angular
import {
	AfterContentInit,
	Component,
	ElementRef,
	OnInit,
	Renderer2,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
// Store
import { Store } from '@ngxs/store';
// Actions
import { SetEvent } from '../../action/event.actions';
// Services
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { Permission } from '../../../../shared/model/permission.model';
import { EventsService } from '../../service/events.service';
import {
	PreferenceService,
	PREFERENCES_LIST,
} from '../../../../shared/services/preference.service';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
// Components
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { DataGridOperationsHelper } from '../../../../shared/utils/data-grid-operations.helper';
import { HeaderActionButtonData } from 'tds-component-library';
// Models
import {
	COLUMN_MIN_WIDTH,
	ActionType,
} from '../../../dataScript/model/data-script.model';
import {
	GRID_DEFAULT_PAGINATION_OPTIONS,
	GRID_DEFAULT_PAGE_SIZE,
} from '../../../../shared/model/constants';
// Kendo
import {
	CompositeFilterDescriptor,
	State,
	process,
} from '@progress/kendo-data-query';
import {
	CellClickEvent,
	GridDataResult,
	PageChangeEvent,
} from '@progress/kendo-angular-grid';
import { EventColumnModel, EventModel } from '../../model/event.model';
import { EventCreateComponent } from '../create/event-create.component';
import { EventViewEditComponent } from '../view-edit/event-view-edit.component';

declare var jQuery: any;

@Component({
	selector: 'event-list',
	templateUrl: 'event-list.component.html',
})
export class EventListComponent implements OnInit, AfterContentInit {
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
	public eventColumnModel = null;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: EventModel[];
	public selectedRows = [];
	public dateFormat = '';
	public canEditEvent = false;
	protected showFilters = false;
	private dataGridOperationsHelper: DataGridOperationsHelper;

	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private eventsService: EventsService,
		private prompt: UIPromptService,
		private route: ActivatedRoute,
		private elementRef: ElementRef,
		private renderer: Renderer2,
		private store: Store,
		private preferenceService: PreferenceService,
		private translateService: TranslatePipe,
	) {
		// use partially datagrid operations helper, for the moment just to know the number of filters selected
		// in the future this view should be refactored to use the data grid operations helper
		this.dataGridOperationsHelper = new DataGridOperationsHelper([]);

		this.state.take = this.pageSize;
		this.state.skip = this.skip;
		this.resultSet = this.route.snapshot.data['events'];
		this.gridData = process(this.resultSet, this.state);
	}

	ngOnInit() {
		this.disableClearFilters = this.onDisableClearFilter.bind(this);
		this.headerActionButtons = [
			{
				icon: 'plus-circle',
				iconClass: 'is-solid',
				title: this.translateService.transform('EVENT.CREATE_EVENT'),
				disabled: !this.isCreateAvailable(),
				show: true,
				onClick: this.openEventDialogCreate.bind(this),
			},
		];

		this.preferenceService
			.getPreference(PREFERENCES_LIST.CURR_TZ)
			.subscribe();
		this.preferenceService
			.getUserDatePreferenceAsKendoFormat()
			.subscribe(dateFormat => {
				this.dateFormat = dateFormat;
				this.eventColumnModel = new EventColumnModel(
					`{0:${dateFormat}}`
				);
				this.gridColumns = this.eventColumnModel.columns.filter(
					column => column.type !== 'action'
				);
			});
		this.canEditEvent = this.permissionService.hasPermission('EventEdit');
	}

	ngAfterContentInit() {
		if (this.route.snapshot.queryParams['show']) {
			let { id, name } = this.resultSet.find((bundle: any) => {
				return bundle.id === parseInt(this.route.snapshot.queryParams['show'], 0)
			});
			setTimeout(() => {
				this.showEvent(id, name);
			});
		}
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
		const root = this.eventsService.filterColumn(column, this.state);
		this.filterChange(root);
	}

	protected clearValue(column: any): void {
		this.eventsService.clearFilter(column, this.state);
		this.filterChange(this.state.filter);
	}

	/**
	 * Catch the Selected Row
	 * @param {SelectionEvent} event
	 */
	protected cellClick(event: CellClickEvent): void {
		this.selectRow(event.dataItem.id);
	}

	protected reloadData(): void {
		this.eventsService.getEventsForList().subscribe(
			result => {
				this.resultSet = result;
				this.gridData = process(this.resultSet, this.state);
				setTimeout(() => this.forceDisplayLastRowAddedToGrid(), 100);
			},
			err => console.log(err)
		);
	}

	/**
	 * This work as a temporary fix.
	 * TODO: talk when Jorge Morayta get's back to do a proper/better fix.
	 */
	private forceDisplayLastRowAddedToGrid(): void {
		const lastIndex = this.gridData.data.length - 1;
		let target = this.elementRef.nativeElement.querySelector(
			`tr[data-kendo-grid-item-index="${lastIndex}"]`
		);
		if (target) {
			this.renderer.setStyle(target, 'height', '36px');
		}
	}

	protected openEventDialogCreate(): void {
		this.dialogService
			.open(EventCreateComponent, [])
			.then(result => {
				// update the list to reflect changes, it keeps the filter
				this.reloadData();
			})
			.catch(result => {
				console.log('Dismissed Dialog');
			});
	}

	protected selectRow(dataItemId: number): void {
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
	 * Open and show the selected Event
	 * Also it set into the local the new value
	 * @param id
	 */
	protected showEvent(id, name): void {
		this.store.dispatch(new SetEvent({ id: id, name: name }));
		this.dialogService
			.open(EventViewEditComponent, [{ provide: 'id', useValue: id }])
			.then(result => {
				this.reloadData();
			})
			.catch(result => {
				this.reloadData();
			});
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
	 * Clear all filters
	 */
	protected clearAllFilters(): void {
		this.showFilters = false;
		this.dataGridOperationsHelper.clearAllFilters(this.eventColumnModel.columns, this.state);
		this.reloadData();
	}

	/**
	 * Disable clear filters
	 */
	private onDisableClearFilter(): boolean {
		return this.filterCount() === 0;
	}

	/**
	 * Determines if user has the permission to create projects
	 */
	protected isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ProjectCreate);
	}
}
