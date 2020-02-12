import {
	AfterContentInit,
	Component,
	ElementRef, OnDestroy,
	OnInit,
	Renderer2,
} from '@angular/core';
import {
	GRID_DEFAULT_PAGE_SIZE,
	GRID_DEFAULT_PAGINATION_OPTIONS,
} from '../../../../shared/model/constants';
import {
	ActionType,
	COLUMN_MIN_WIDTH,
} from '../../../dataScript/model/data-script.model';
import { SelectableSettings} from '@progress/kendo-angular-grid';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { PreferenceService } from '../../../../shared/services/preference.service';
import { DataGridOperationsHelper, fixContentWrapper } from '../../../../shared/utils/data-grid-operations.helper';
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

@Component({
	selector: `project-list`,
	templateUrl: 'project-list.component.html',
})
export class ProjectListComponent implements OnInit, OnDestroy,  AfterContentInit {
	protected gridColumns: any[];
	public disableClearFilters: Function;
	public headerActionButtons: HeaderActionButtonData[];
	public pageSize = GRID_DEFAULT_PAGE_SIZE;
	public defaultPageOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	public projectColumnModel = null;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
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
	private selectableSettings: SelectableSettings = { mode: 'single', checkboxOnly: true};
	public dataGridOperationsHelper: DataGridOperationsHelper;
	private initialSort: any = [{
		dir: 'asc',
		field: 'name'
	}];
	private checkboxSelectionConfig = null;

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
		this.showActive =
			this.route.snapshot.queryParams['active'] !== 'completed';
		this.resultSet = this.showActive
			? this.route.snapshot.data['projects'].activeProjects
			: this.route.snapshot.data['projects'].completedProjects;

		this.dataGridOperationsHelper = new DataGridOperationsHelper(
			this.resultSet,
			this.initialSort,
			this.selectableSettings,
			this.checkboxSelectionConfig,
			this.pageSize);
	}

	ngOnInit() {
		fixContentWrapper();
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
				this.gridColumns = this.projectColumnModel.columns.filter(
					column => column.type !== 'action'
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
		this.projectService.getProjects()
			.subscribe(
				result => {
					let resultSet = this.showActive
						? result.activeProjects
						: result.completedProjects;
					this.dataGridOperationsHelper.reloadData(resultSet);
					setTimeout(
						() => this.forceDisplayLastRowAddedToGrid(),
						100
					);
				},
				err => console.log(err)
			);
	}

	/**
	 * This work as a temporary fix.
	 * TODO: talk when Jorge Morayta get's back to do a proper/better fix.
	 */
	private forceDisplayLastRowAddedToGrid(): void {
		const lastIndex = this.dataGridOperationsHelper.gridData.data.length - 1;
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
	 * Returns the number of distinct currently selected filters
	 */
	protected filterCount(): number {
		return this.dataGridOperationsHelper.getFilterCounter();
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
		this.showFilters = false;
		this.dataGridOperationsHelper.clearAllFilters(this.gridColumns);
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
