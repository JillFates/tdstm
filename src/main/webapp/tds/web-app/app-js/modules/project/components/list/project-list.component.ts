import {
	AfterContentInit,
	Component,
	ElementRef, OnDestroy,
	OnInit,
	Renderer2,
} from '@angular/core';
import {
	CompositeFilterDescriptor,
	process,
	State,
} from '@progress/kendo-data-query';
import {
	GRID_DEFAULT_PAGE_SIZE,
	GRID_DEFAULT_PAGINATION_OPTIONS,
} from '../../../../shared/model/constants';
import {
	ActionType,
	COLUMN_MIN_WIDTH,
} from '../../../dataScript/model/data-script.model';
import { GridDataResult } from '@progress/kendo-angular-grid';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { PreferenceService } from '../../../../shared/services/preference.service';
import { DataGridOperationsHelper } from '../../../../shared/utils/data-grid-operations.helper';
import { HeaderActionButtonData } from 'tds-component-library';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import {
	ActivatedRoute,
	Params,
	Router,
	NavigationEnd,
} from '@angular/router';
import { ProjectService } from '../../service/project.service';
import { ProjectColumnModel, ProjectModel } from '../../model/project.model';
import {
	BooleanFilterData,
	DefaultBooleanFilterData,
} from '../../../../shared/model/data-list-grid.model';
import { ProjectCreateComponent } from '../create/project-create.component';
import { ProjectViewEditComponent } from '../view-edit/project-view-edit.component';
import { NotifierService } from '../../../../shared/services/notifier.service';
import {Permission} from '../../../../shared/model/permission.model';

declare var jQuery: any;

@Component({
	selector: `project-list`,
	templateUrl: 'project-list.component.html',
})
export class ProjectListComponent implements OnInit, OnDestroy,  AfterContentInit {
	public disableClearFilters: Function;
	public headerActionButtons: HeaderActionButtonData[];
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
	public projectColumnModel = null;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: ProjectModel[];
	protected navigationSubscription;
	public canEditProject;
	public dateFormat = '';
	public booleanFilterData = BooleanFilterData;
	public defaultBooleanFilterData = DefaultBooleanFilterData;
	public showActive: boolean;
	private projectToOpen: string;
	private projectOpen = false;
	protected showFilters = false;
	private dataGridOperationsHelper: DataGridOperationsHelper;

	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private projectService: ProjectService,
		private prompt: UIPromptService,
		private preferenceService: PreferenceService,
		private notifierService: NotifierService,
		private router: Router,
		private route: ActivatedRoute,
		private elementRef: ElementRef,
		private renderer: Renderer2,
		private translateService: TranslatePipe,
	) {
		this.state.take = this.pageSize;
		this.state.skip = this.skip;
		this.showActive =
			this.route.snapshot.queryParams['active'] !== 'completed';
		this.resultSet = this.showActive
			? this.route.snapshot.data['projects'].activeProjects
			: this.route.snapshot.data['projects'].completedProjects;
		this.gridData = process(this.resultSet, this.state);

		// use partially datagrid operations helper, for the moment just to know the number of filters selected
		// in the future this view should be refactored to use the data grid operations helper
		this.dataGridOperationsHelper = new DataGridOperationsHelper([]);
	}

	ngOnInit() {
		this.disableClearFilters = this.onDisableClearFilter.bind(this);
		this.headerActionButtons = [
			{
				icon: 'plus',
				iconClass: 'is-solid',
				title: this.translateService.transform('GLOBAL.CREATE'),
				disabled: !this.isCreateAvailable(),
				show: true,
				onClick: this.openCreateProject.bind(this),
			},
		];

		this.preferenceService
			.getUserDatePreferenceAsKendoFormat()
			.subscribe(dateFormat => {
				this.dateFormat = dateFormat;
				this.projectColumnModel = new ProjectColumnModel(
					`{0:${dateFormat}}`
				);
			});
		this.updateBreadcrumbAndTitle();
		this.canEditProject = this.permissionService.hasPermission(
			'ProjectEdit'
		);
	}

	ngAfterContentInit() {
		this.projectToOpen = this.route.snapshot.queryParams['show'];
		// The following code Listen to any change made on the route to reload the page
		this.navigationSubscription = this.router.events.subscribe((event: any) => {
			if (event && event.state && event.state && event.state.url.indexOf('/project/list') !== -1) {
				this.projectToOpen = event.state.root.queryParams.show;
			}
			if (event instanceof NavigationEnd && this.projectToOpen && this.projectToOpen.length && !this.projectOpen) {
				this.showProject(this.projectToOpen);
			}
		});

		this.route.queryParams.subscribe(params => {
			if (this.projectToOpen && !this.projectOpen) {
				setTimeout(() => {
					this.showProject(this.projectToOpen);
				});
			}
		});
	}

	protected async setShowActive(to: boolean): Promise<void> {
		if (this.showActive !== to) {
			this.showActive = to;
			this.projectToOpen = null;
			const queryParams: Params = {
				active: this.showActive ? 'active' : 'completed',
			};
			await this.router.navigate([], {
				relativeTo: this.route,
				queryParams: queryParams,
			});
			this.updateBreadcrumbAndTitle();
			this.reloadData();
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

	protected onFilter(column: any, event: any = null): void {
		column.filter = event;
		if (!event) {
			this.clearValue(column);
		} else {
			const root = this.projectService.filterColumn(column, this.state);
			this.filterChange(root);
		}
	}

	protected clearValue(column: any): void {
		this.projectService.clearFilter(column, this.state);
		this.filterChange(this.state.filter);
	}

	protected updateBreadcrumbAndTitle(): void {
		this.notifierService.broadcast({
			name: 'notificationHeaderBreadcrumbChange',
			menu: ['Projects', this.showActive ? 'Active' : 'Completed'],
		});
		this.notifierService.broadcast({
			name: 'notificationHeaderTitleChange',
			title:
				'Projects' + ' - ' + (this.showActive ? 'Active' : 'Completed'),
		});
	}

	protected showProject(id): void {
		if (!this.projectOpen) {
			this.projectOpen = true;
			this.dialogService
				.open(ProjectViewEditComponent, [
					{ provide: 'id', useValue: id },
				])
				.then(result => {
					this.projectOpen = false;
					this.reloadData();
				})
				.catch(result => {
					this.projectOpen = false;
					this.reloadData();
				});
		}
	}

	protected openCreateProject(): void {
		this.dialogService
			.open(ProjectCreateComponent, [])
			.then(result => {
				this.reloadData();
			})
			.catch(result => {
				this.reloadData();
			});
	}

	protected reloadData(): void {
		this.projectService.getProjects().subscribe(
			result => {
				this.resultSet = this.showActive
					? result.activeProjects
					: result.completedProjects;
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
		this.renderer.setStyle(target, 'height', '23px');
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
	 * Set on/off the filter icon indicator
	 */
	protected toggleFilter(): void {
		this.showFilters = !this.showFilters;
	}

	/**
	 * Clear all filters
	 */
	protected clearAllFilters(): void {
		this.dataGridOperationsHelper.clearAllFilters(this.projectColumnModel.columns, this.state);
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

	/**
	 * Ensure the listener is not available after moving away from this component
	 */
	ngOnDestroy(): void {
		if (this.navigationSubscription) {
			this.navigationSubscription.unsubscribe();
		}
	}
}
