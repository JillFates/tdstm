import {AfterContentInit, Component, ElementRef, OnInit, Renderer2} from '@angular/core';
import {CompositeFilterDescriptor, process, State} from '@progress/kendo-data-query';
// Store
import {Store} from '@ngxs/store';
// Actions
import {SetBundle} from '../../action/bundle.actions';
import {GRID_DEFAULT_PAGE_SIZE, GRID_DEFAULT_PAGINATION_OPTIONS} from '../../../../shared/model/constants';
import {ActionType, COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
import {GridDataResult} from '@progress/kendo-angular-grid';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {ActivatedRoute} from '@angular/router';
import {BundleService} from '../../service/bundle.service';
import {BundleColumnModel, BundleModel} from '../../model/bundle.model';
import {BooleanFilterData, DefaultBooleanFilterData} from '../../../../shared/model/data-list-grid.model';
import {BundleCreateComponent} from '../create/bundle-create.component';
import {BundleViewEditComponent} from '../view-edit/bundle-view-edit.component';
import { DataGridOperationsHelper } from '../../../../shared/utils/data-grid-operations.helper';
import { HeaderActionButtonData } from 'tds-component-library';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import { Permission } from '../../../../shared/model/permission.model';

declare var jQuery: any;

@Component({
	selector: `bundle-list`,
	templateUrl: 'bundle-list.component.html',
})
export class BundleListComponent implements OnInit, AfterContentInit {
	public disableClearFilters: Function;
	public headerActionButtons: HeaderActionButtonData[];
	protected state: State = {
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
	public bundleColumnModel = null;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: BundleModel[];
	public canEditBundle;
	public dateFormat = '';
	public booleanFilterData = BooleanFilterData;
	public defaultBooleanFilterData = DefaultBooleanFilterData;
	public showFilters = false;
	private dataGridOperationsHelper: DataGridOperationsHelper;

	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private bundleService: BundleService,
		private prompt: UIPromptService,
		private preferenceService: PreferenceService,
		private route: ActivatedRoute,
		private elementRef: ElementRef,
		private store: Store,
		private renderer: Renderer2,
		private translateService: TranslatePipe) {
		// use partially datagrid operations helper, for the moment just to know the number of filters selected
		// in the future this view should be refactored to use the data grid operations helper
		this.dataGridOperationsHelper = new DataGridOperationsHelper([]);

		this.state.take = this.pageSize;
		this.state.skip = this.skip;
		this.resultSet = this.route.snapshot.data['bundles'];
		this.gridData = process(this.resultSet, this.state);
	}

	ngOnInit() {
		this.disableClearFilters = this.onDisableClearFilter.bind(this);
		this.headerActionButtons = [
			{
				icon: 'plus-circle',
				iconClass: 'is-solid',
				title: this.translateService.transform('PLANNING.BUNDLES.CREATE_BUNDLE'),
				disabled: !this.isCreateAvailable(),
				show: true,
				onClick: this.openCreateBundle.bind(this),
			},
		];

		this.preferenceService.getUserDatePreferenceAsKendoFormat()
			.subscribe((dateFormat) => {
				this.dateFormat = dateFormat;
				this.bundleColumnModel = new BundleColumnModel(`{0:${dateFormat}}`);
			});
		this.canEditBundle = this.permissionService.hasPermission('BundleEdit');
	}

	ngAfterContentInit() {
		if (this.route.snapshot.queryParams['show']) {
			let {id, name} = this.resultSet.find((bundle: any) => {
				return bundle.id === parseInt(this.route.snapshot.queryParams['show'], 0);
			});
			setTimeout(() => {
				this.showBundle(id, name);
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
		const root = this.bundleService.filterColumn(column, this.state);
		this.filterChange(root);
	}

	protected clearValue(column: any): void {
		this.bundleService.clearFilter(column, this.state);
		this.filterChange(this.state.filter);
	}

	protected showBundle(id, name): void {
		this.store.dispatch(new SetBundle({id: id, name: name}));
		this.dialogService.open(BundleViewEditComponent,
			[{provide: 'id', useValue: id}]).then(result => {
			this.reloadData();
		}).catch(result => {
			this.reloadData();
		});
	}

	protected openCreateBundle(): void {
		this.dialogService.open(BundleCreateComponent,
			[]).then(result => {
			this.reloadData();
		}).catch(result => {
			this.reloadData();
		});
	}

	protected reloadData(): void {
		this.bundleService.getBundles().subscribe(
			(result) => {
				this.resultSet = result;
				this.gridData = process(this.resultSet, this.state);
				setTimeout(() => this.forceDisplayLastRowAddedToGrid() , 100);
			},
			(err) => console.log(err));
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
	 * This work as a temporary fix.
	 * TODO: talk when Jorge Morayta get's back to do a proper/better fix.
	 */
	private forceDisplayLastRowAddedToGrid(): void {
		const lastIndex = this.gridData.data.length - 1;
		let target = this.elementRef.nativeElement.querySelector(`tr[data-kendo-grid-item-index="${lastIndex}"]`);
		this.renderer.setStyle(target, 'height', '36px');
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

	/**
	 * Clear all filters
	 */
	protected clearAllFilters(): void {
		this.showFilters = false;
		this.dataGridOperationsHelper.clearAllFilters(this.bundleColumnModel.columns, this.state);
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
