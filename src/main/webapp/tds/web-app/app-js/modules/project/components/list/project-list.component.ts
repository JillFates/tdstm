import {AfterContentInit, Component, ElementRef, OnInit, Renderer2} from '@angular/core';
import {CompositeFilterDescriptor, process, State} from '@progress/kendo-data-query';
import {GRID_DEFAULT_PAGE_SIZE, GRID_DEFAULT_PAGINATION_OPTIONS} from '../../../../shared/model/constants';
import {ActionType, COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
import {GridDataResult} from '@progress/kendo-angular-grid';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {ActivatedRoute, Params, Router} from '@angular/router';
import {ProjectService} from '../../service/project.service';
import {ProjectColumnModel, ProjectModel} from '../../model/project.model';
import {BooleanFilterData, DefaultBooleanFilterData} from '../../../../shared/model/data-list-grid.model';
import {ProjectCreateComponent} from '../create/project-create.component';
import {ProjectViewEditComponent} from '../view-edit/project-view-edit.component';

declare var jQuery: any;

@Component({
	selector: `project-list`,
	templateUrl: 'project-list.component.html',
})
export class ProjectListComponent implements OnInit, AfterContentInit {
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
	public projectColumnModel = null;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: ProjectModel[];
	public canEditProject;
	public dateFormat = '';
	public booleanFilterData = BooleanFilterData;
	public defaultBooleanFilterData = DefaultBooleanFilterData;
	public showActive: boolean;

	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private projectService: ProjectService,
		private prompt: UIPromptService,
		private preferenceService: PreferenceService,
		private router: Router,
		private route: ActivatedRoute,
		private elementRef: ElementRef,
		private renderer: Renderer2) {
		this.state.take = this.pageSize;
		this.state.skip = this.skip;
		this.showActive = this.route.snapshot.queryParams['active'] !== 'completed';
		this.resultSet = this.showActive ? this.route.snapshot.data['projects'].activeProjects : this.route.snapshot.data['projects'].completedProjects;
		this.gridData = process(this.resultSet, this.state);
	}

	ngOnInit() {
		this.preferenceService.getUserDatePreferenceAsKendoFormat()
			.subscribe((dateFormat) => {
				this.dateFormat = dateFormat;
				this.projectColumnModel = new ProjectColumnModel(`{0:${dateFormat}}`);
			});
		this.canEditProject = this.permissionService.hasPermission('ProjectEdit');
	}

	ngAfterContentInit() {
		if (this.route.snapshot.queryParams['show']) {
			setTimeout(() => {
				this.showProject(this.route.snapshot.queryParams['show']);
			});
		}
	}

	protected toggleShowActive(): void {
		this.showActive = !this.showActive;
		const queryParams: Params = { active: this.showActive ? 'active' : 'completed' };
		this.router.navigate([], { relativeTo: this.route, queryParams: queryParams });
		this.reloadData();
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
		const root = this.projectService.filterColumn(column, this.state);
		this.filterChange(root);
	}

	protected clearValue(column: any): void {
		this.projectService.clearFilter(column, this.state);
		this.filterChange(this.state.filter);
	}

	protected showProject(id): void {
		this.dialogService.open(ProjectViewEditComponent,
			[{provide: 'id', useValue: id}]).then(result => {
			this.reloadData();
		}).catch(result => {
			this.reloadData();
		});
	}

	protected openCreateProject(): void {
		this.dialogService.open(ProjectCreateComponent,
			[]).then(result => {
			this.reloadData();
		}).catch(result => {
			this.reloadData();
		});
	}

	protected reloadData(): void {
		this.projectService.getProjects().subscribe(
			(result) => {
				this.resultSet = this.showActive ? result.activeProjects : result.completedProjects;
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