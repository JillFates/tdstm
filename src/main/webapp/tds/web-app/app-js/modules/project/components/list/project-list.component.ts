// Angular
import {
	AfterContentInit,
	Component, ComponentFactoryResolver, OnDestroy,
	OnInit,
	ViewChild,
} from '@angular/core';
import {
	ActivatedRoute,
	Params,
	Router,
	NavigationEnd,
} from '@angular/router';
// Model
import {ActionType} from '../../../dataScript/model/data-script.model';
import {
	ColumnHeaderData, DialogConfirmAction, DialogService,
	GridComponent,
	GridModel,
	GridRowAction,
	GridSettings,
	HeaderActionButtonData, ModalSize
} from 'tds-component-library';
import {ProjectColumnModel, ProjectModel} from '../../model/project.model';
import {Permission} from '../../../../shared/model/permission.model';
// Component
import {ProjectCreateComponent} from '../create/project-create.component';
import {ProjectViewEditComponent} from '../view-edit/project-view-edit.component';
// Service
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {ProjectService} from '../../service/project.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {CellClickEvent} from '@progress/kendo-angular-grid';

@Component({
	selector: `project-list`,
	templateUrl: 'project-list.component.html',
})
export class ProjectListComponent implements OnInit, AfterContentInit, OnDestroy {

	private gridRowActions: GridRowAction[];

	private headerActions: HeaderActionButtonData[];

	private gridSettings: GridSettings = {
		defaultSort: [{field: 'name', dir: 'asc'}],
		sortSettings: {mode: 'single'},
		selectableSettings: {enabled: true, mode: 'single'},
		filterable: true,
		pageable: true,
		resizable: true,
	};

	private columnModel: ColumnHeaderData[];
	public gridModel: GridModel;
	private dateFormat = '';

	@ViewChild(GridComponent, {static: false}) gridComponent: GridComponent;
	public projectColumnModel = null;

	public showActive: boolean;
	private projectToOpen: string;
	private projectOpen = false;

	private navigationSubscription;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private permissionService: PermissionService,
		private projectService: ProjectService,
		private preferenceService: PreferenceService,
		private notifierService: NotifierService,
		private router: Router,
		private route: ActivatedRoute,
		private translateService: TranslatePipe,
	) {
		this.showActive = this.route.snapshot.queryParams['active'] !== 'completed';
	}

	async ngOnInit() {

		this.gridRowActions = [
			{
				name: 'Edit',
				show: true,
				disabled: !this.isEditAvailable(),
				onClick: this.onEdit,
			},
			{
				name: 'Delete',
				show: true,
				disabled: !this.isEditAvailable(),
				onClick: this.onDelete,
			},
		];

		this.headerActions = [
			{
				icon: 'plus',
				iconClass: 'is-solid',
				title: this.translateService.transform('GLOBAL.CREATE'),
				disabled: !this.isCreateAvailable(),
				show: true,
				onClick: this.onCreateProject,
			},
		];

		this.gridModel = {
			columnModel: this.columnModel,
			gridRowActions: this.gridRowActions,
			gridSettings: this.gridSettings,
			headerActionButtons: this.headerActions,
			loadData: this.loadData,
		};

		this.dateFormat = await this.preferenceService.getUserDatePreferenceAsKendoFormat().toPromise();

		this.columnModel = new ProjectColumnModel(this.dateFormat).columns;

		this.gridModel.columnModel = this.columnModel;

		this.updateBreadcrumbAndTitle();
	}

	ngAfterContentInit() {
		this.projectToOpen = this.route.snapshot.queryParams['show'];
		// The following code Listen to any change made on the route to reload the page
		this.navigationSubscription = this.router.events.subscribe((event: any) => {
			if (event && event.state && event.state && event.state.url.indexOf('/project/list') !== -1) {
				this.projectToOpen = event.state.root.queryParams.show;
			}
			if (event instanceof NavigationEnd && this.projectToOpen && this.projectToOpen.length) {
				setTimeout(() => {
					if (!this.projectOpen) {
						this.openProject(parseInt(this.projectToOpen, 10), ActionType.VIEW);
					}
				}, 500);
			}
		});

		this.route.queryParams.subscribe(params => {
			if (this.projectToOpen && !this.projectOpen) {
				setTimeout(() => {
					if (!this.projectOpen) {
						this.openProject(parseInt(this.projectToOpen, 10), ActionType.VIEW);
					}
				}, 500);
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
			await this.gridComponent.reloadData();
		}
	}

	protected updateBreadcrumbAndTitle(): void {
		this.notifierService.broadcast({
			name: 'notificationHeaderBreadcrumbChange',
			menu: ['Projects', this.showActive ? 'Active' : 'Completed'],
		});
		this.notifierService.broadcast({
			name: 'notificationHeaderTitleChange',
			title: 'Projects' + ' - ' + (this.showActive ? 'Active' : 'Completed'),
		});
	}

	/**
	 * Select the current element and open the Edit Dialog
	 * @param dataItem
	 */
	private onEdit = async (dataItem: ProjectModel): Promise<void> => {
		try {
			if (this.isEditAvailable()) {
				await this.openProject(dataItem.id, ActionType.EDIT, true);
			}
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Open The Dialog to Create, View or Edit the Project
	 * @param {ProjectModel} projectModel
	 * @param {number} actionType
	 */
	private async openProject(projectModelId: number, actionType: ActionType, openFromList = false): Promise<void> {
		try {
			this.projectOpen = true;
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: ProjectViewEditComponent,
				data: {
					projectModelId: projectModelId,
					actionType: actionType,
					openFromList: openFromList
				},
				modalConfiguration: {
					title: 'Project',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).toPromise();
			this.projectOpen = false;
			await this.gridComponent.reloadData();
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * On Create Project
	 */
	private onCreateProject = async (): Promise<void> => {
		try {
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: ProjectCreateComponent,
				data: {},
				modalConfiguration: {
					title: 'Project Create',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			console.error(error);
		}
	}

	protected loadData = async (): Promise<ProjectModel[]> => {
		try {
			const result = await this.projectService.getProjects().toPromise();
			return new Promise((resolve) => {
				let resultSet = this.showActive ? result.activeProjects : result.completedProjects;
				resolve(resultSet);
			});
		} catch (error) {
			console.error(error);
		}
	}

	public async cellClick(event: CellClickEvent): Promise<void> {
		if (event.columnIndex > 0 && this.isEditAvailable()) {
			await this.openProject(event.dataItem.id, ActionType.VIEW);
		}
	}

	/**
	 * Determines if user has the permission to create projects
	 */
	protected isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ProjectCreate);
	}

	private isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ProjectEdit);
	}

	/**
	 * On Delete Project
	 */
	public onDelete = async (dataItem: ProjectModel): Promise<void> => {
		try {
			if (this.isEditAvailable()) {
				const confirmation = await this.dialogService.confirm(
					'Confirmation Required',
					'WARNING: Are you sure you want to delete this project? This cannot be undone.'
				).toPromise();
				if (confirmation.confirm === DialogConfirmAction.CONFIRM) {
					this.projectService.deleteProject(dataItem.id).toPromise();
					await this.gridComponent.reloadData();
				}
			}
		} catch (error) {
			console.error(error);
		}
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
