import {Component, OnInit, ViewChild} from '@angular/core';
import {BehaviorSubject, Observable, ReplaySubject} from 'rxjs';
import {distinct, map, skip} from 'rxjs/operators';
import {ActivatedRoute} from '@angular/router';
import {Location} from '@angular/common';

import {TaskService} from '../../service/task.service';
import {DiagramLayoutComponent} from '../../../../shared/components/diagram-layout/diagram-layout.component';
import {IGraphNode, IGraphTask, TASK_OPTION_LABEL} from '../../model/graph-task.model';
import {FA_ICONS} from '../../../../shared/constants/fontawesome-icons';
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
import {IMoveEvent} from '../../model/move-event.model';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {UserContextService} from '../../../auth/service/user-context.service';
import {ReportsService} from '../../../reports/service/reports.service';
import {TaskDetailModel} from '../../model/task-detail.model';
import {TaskDetailComponent} from '../detail/task-detail.component';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {TaskEditComponent} from '../edit/task-edit.component';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {TaskActionEvents} from '../common/constants/task-action-events.constant';
import {TaskStatus} from '../../model/task-edit-create.model';
import {ITaskEvent} from '../../model/task-event.model';
import {
	ContainerComp,
	IDiagramContextMenuOption
} from '../../../../shared/components/diagram-layout/model/diagram-context-menu.model';
import {CTX_MENU_ICONS_PATH} from '../common/constants/task-icon-path';
import {Permission} from '../../../../shared/model/permission.model';
import {ILinkPath} from '../../../../shared/components/diagram-layout/model/diagram-layout.model';
import {DIALOG_SIZE, ModalType} from '../../../../shared/model/constants';
import {AssetShowComponent} from '../../../assetExplorer/components/asset/asset-show.component';
import {AssetExplorerModule} from '../../../assetExplorer/asset-explorer.module';
import {TaskEditCreateModelHelper} from '../common/task-edit-create-model.helper';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {clone} from 'ramda';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {AlertType} from '../../../../shared/model/alert.model';

@Component({
	selector: 'tds-neighborhood',
	templateUrl: './neighborhood.component.html'
})
export class NeighborhoodComponent implements OnInit {
	tasks: IGraphNode[];
	nodeData$: BehaviorSubject<any[]> = new BehaviorSubject([]);
	links$: BehaviorSubject<any[]> = new BehaviorSubject([]);
	ctxMenuOpts$: ReplaySubject<IDiagramContextMenuOption> = new ReplaySubject(2);
	@ViewChild('graph') graph: DiagramLayoutComponent;
	@ViewChild('eventsDropdown') eventsDropdown: DropDownListComponent;
	@ViewChild('teamHighlightDropdown') teamHighlightDropdown: DropDownListComponent;
	statusTypes = {
		started: 'start',
		pause: 'hold',
		clock: 'clock',
		unknown: 'unknown',
		pending: 'pending',
		ready: 'ready',
		forward: 'forward',
		completed: 'completed'
	};
	opened: boolean;
	filterText: string;
	textFilter: BehaviorSubject<string> = new BehaviorSubject<string>('');
	icons = FA_ICONS;
	selectedEvent: IMoveEvent;
	eventList$: Observable<IMoveEvent[]>;
	isEventDropdownOpen: boolean;
	isTeamHighlightDropdownOpen: boolean;
	TASK_MANAGER_REFRESH_TIMER: string = PREFERENCES_LIST.TASK_MANAGER_REFRESH_TIMER;
	userContext: UserContextModel;
	viewUnpublished: boolean;
	myTasks: boolean;
	minimizeAutoTasks: boolean;
	urlParams: any;
	teamHighlights$: BehaviorSubject<string[]> = new BehaviorSubject<string[]>([]);
	selectedTeamHighlight: string;
	currentUser$: ReplaySubject<any> = new ReplaySubject(1);
	ctxMenuIcons = CTX_MENU_ICONS_PATH;

	constructor(
			private taskService: TaskService,
			private activatedRoute: ActivatedRoute,
			private userContextService: UserContextService,
			private reportService: ReportsService,
			private preferenceService: PreferenceService,
			private dialogService: UIDialogService,
			private notifierService: NotifierService,
			private location: Location,
			private translatePipe: TranslatePipe
		) {
				this.activatedRoute.queryParams.subscribe(params => {
					if (params) { this.urlParams = Object.assign({}, params); }
				});
	}

	ngOnInit() {
		this.loadAll();
	}

	loadAll(): void {
		this.subscribeToHighlightFilter();
		this.loadUserContext();
		this.loadFilters();
		this.loadEventList();
		this.eventsDropdownOpened();
		this.eventsDropdownClosed();
		this.subscribeToEvents();
		// this.teamHighlightDropdownOpened();
		// this.teamHighlightDropdownClosed();
	}

	subscribeToEvents(): void {
		this.notifierService
			.on(TaskActionEvents.START, data => this.start({name: data.name, task: data.node}));
		this.notifierService
			.on(TaskActionEvents.HOLD, data => this.hold({name: data.name, task: data.node}));
		this.notifierService
			.on(TaskActionEvents.DONE, data => this.done({name: data.name, task: data.node}));
		this.notifierService
			.on(TaskActionEvents.RESET, data => this.reset({name: data.name, task: data.node}));
		this.notifierService
			.on(TaskActionEvents.INVOKE, data => this.invoke({name: data.name, task: data.node}));
		this.notifierService
			.on(TaskActionEvents.SHOW, data => this.showTaskDetails({name: data.name, task: data.node}));
		this.notifierService
			.on(TaskActionEvents.EDIT, data => this.editTask({name: data.name, task: data.node}));
		this.notifierService
			.on(TaskActionEvents.NEIGHBORHOOD, data => this.onNeighborhood({name: data.name, task: data.node}));
		this.notifierService
			.on(TaskActionEvents.ASSIGN_TO_ME, data => this.assignToMe({name: data.name, task: data.node}));
		this.notifierService
			.on(TaskActionEvents.SHOW_ASSET_DETAIL, data =>
				this.showAssetDetail({name: data.name, task: data.node}));
	}

	/**
	 * Load user preferences to filter tasks and then load tasks
 	**/
	loadFilters(): void {
		this.preferenceService.getPreferences(
			PREFERENCES_LIST.MY_TASKS,
			PREFERENCES_LIST.MINIMIZE_AUTO_TASKS,
			PREFERENCES_LIST.VIEW_UNPUBLISHED
		).subscribe(res => {
				const {myTasks, minimizeAutoTasks, VIEW_UNPUBLISHED} = res;
				this.myTasks = (myTasks && myTasks === 'true' || myTasks === '1');
				this.minimizeAutoTasks = (minimizeAutoTasks && (minimizeAutoTasks === 'true' || minimizeAutoTasks === '1'));
				this.viewUnpublished = (VIEW_UNPUBLISHED && (VIEW_UNPUBLISHED === 'true' || VIEW_UNPUBLISHED === '1'));

				// If this a neighborhood view from the task manager, then load tasks from task service, otherwise load tasks
				// from selected event
				if (this.urlParams && this.urlParams.taskId) {
					this.loadTasks(this.urlParams.taskId);
				} else {
					this.loadFromSelectedEvent(this.selectedEvent.id);
				}
			});
	}

	/**
	 * Load user context
	 **/
	loadUserContext(): void {
		this.userContextService.getUserContext().subscribe((userContext: UserContextModel) => {
			this.userContext = userContext;
			this.selectedEvent = userContext.event;
		});
	}

	/**
		* Load tasks
		* @param {number} taskId to load tasks from
	 **/
	loadTasks(taskId: number): void {

		const filters = {
			myTasks: this.myTasks ? '1' : '0',
			minimizeAutoTasks: this.minimizeAutoTasks ? '1' : '0',
			viewUnpublished: this.viewUnpublished ? '1' : '0'
		};
		this.taskService.findTask(taskId, filters)
			.subscribe((res: IGraphNode[]) => {
				if (res && res.length > 0) {
					console.log('tasks:', res);
					this.tasks = res;
					this.generateModel();
				}
			});
	}

	/**
	 * Load events to fill events dropdown
	 **/
	loadEventList() {
			console.log('evenList');
			this.eventList$ = this.reportService
				.getEventList()
				.pipe(map(res => res.data));
	}

	/**
	 * Load tasks
	 * @param {number} moveEventId of moveEvent to load tasks from
	 **/
	loadFromSelectedEvent(moveEventId?: number): void {
		if (this.tasks) { return; }

		const filters = {
			myTasks: this.myTasks ? '1' : '0',
			minimizeAutoTasks: this.minimizeAutoTasks ? '1' : '0',
			viewUnpublished: this.viewUnpublished ? '1' : '0'
		};
		this.taskService.findTasksByMoveEventId(this.selectedEvent.id, filters)
		.subscribe(res => {
			this.tasks = res.tasks
				.map(t =>
					({task: t, successors: t.successors.includes(',')
							? t.successors.split(',') : [t.successors]}));
			this.generateModel();
		});
	}

	/**
	 * load tasks with new filter criteria
	 **/
	onMyTasksFilterChange(): void {
		this.checkboxFilterChange();
	}

	/**
	 * load tasks with new filter criteria
	 **/
	onMinimizeAutoTasksFilterChange(): void {
		this.checkboxFilterChange();
	}

	/**
	 * load tasks with new filter criteria
	 **/
	onViewUnpublishedFilterChange(): void {
		this.checkboxFilterChange();
	}

	checkboxFilterChange(): void {
		console.log(this.tasks, !!this.tasks.find(t => t.task.id === Number(this.urlParams.taskId)));
		// If actual task used by diagram comes from task manager neighborhood button, then reload
		// from tasks endpoint, else load tasks from the actual selected event
		if (this.urlParams && this.urlParams.taskId
			&& !!this.tasks.find(t => t.task.id === Number(this.urlParams.taskId))) {
			this.loadTasks(this.urlParams.taskId);
		} else {
			this.loadFromSelectedEvent(this.selectedEvent.id)
		}
	}

	/**
	 * generate model to be used by diagram with task specific data
	 **/
	generateModel(): void {
		if (!this.tasks) { return; }
		const nodeDataArr = [];
		const linksPath = [];

		const tasksCopy = this.tasks.slice();

		// Add tasks to nodeDataArray constant
		// and create linksPath object from taskNumber and successors
		tasksCopy.map((t: IGraphNode | any) => {
			t.task.key = t.task.taskNumber;
			if (!t.task.successors) { t.task.successors = t.successors; }
			nodeDataArr.push(t.task);
			linksPath.push(...this.getLinksPath(t.task.taskNumber, t.successors))
		});

		this.nodeData$.next(nodeDataArr);
		this.links$.next(linksPath);
		this.currentUser$.next(this.userContext.user);
		this.ctxMenuOpts$.next(this.ctxMenuOptions());
	}

	ctxMenuOptions(): IDiagramContextMenuOption {
		return  {
			containerComp: ContainerComp.NEIGHBORHOOD,
			fields: [
				{
					label: TASK_OPTION_LABEL.HOLD,
					event: TaskActionEvents.HOLD,
					icon: this.ctxMenuIcons.hold,
					status: TaskStatus.HOLD,
					permission: Permission.TaskChangeStatus
				},
				{
					label: TASK_OPTION_LABEL.START,
					event: TaskActionEvents.START,
					icon: this.ctxMenuIcons.start,
					status: TaskStatus.STARTED,
					permission: Permission.TaskChangeStatus
				},
				{
					label: TASK_OPTION_LABEL.DONE,
					event: TaskActionEvents.DONE,
					icon: this.ctxMenuIcons.done,
					status: TaskStatus.COMPLETED,
					permission: Permission.TaskChangeStatus
				},
				{
					label: TASK_OPTION_LABEL.RESET,
					event: TaskActionEvents.RESET,
					icon: this.ctxMenuIcons.reset,
					permission: Permission.TaskChangeStatus
				},
				{
					label: TASK_OPTION_LABEL.INVOKE,
					event: TaskActionEvents.INVOKE,
					icon: this.ctxMenuIcons.invoke,
					permission: Permission.TaskChangeStatus
				},
				{
					label: TASK_OPTION_LABEL.NEIGHBORHOOD,
					event: TaskActionEvents.NEIGHBORHOOD,
					icon: this.ctxMenuIcons.neighborhood,
					status: 'neighborhood',
					permission: Permission.TaskChangeStatus
				},
				{
					label: TASK_OPTION_LABEL.ASSET_DETAILS,
					event: TaskActionEvents.SHOW_ASSET_DETAIL,
					icon: this.ctxMenuIcons.assetDetail,
					permission: Permission.AssetView
				},
				{
					label: TASK_OPTION_LABEL.VIEW,
					event: TaskActionEvents.SHOW,
					icon: this.ctxMenuIcons.view,
					permission: Permission.TaskView
				},
				{
					label: TASK_OPTION_LABEL.EDIT,
					event: TaskActionEvents.EDIT,
					icon: this.ctxMenuIcons.edit,
					permission: Permission.TaskEdit
				},
				{
					label: TASK_OPTION_LABEL.ASSIGN_TO_ME,
					event: TaskActionEvents.ASSIGN_TO_ME,
					icon: this.ctxMenuIcons.assignToMe,
					permission: Permission.TaskEdit
				}
			]
		};
	}

	/**
	 * Load events to fill events dropdown
	 **/
	getLinksPath(taskNumber: string | number, successors: number[]): ILinkPath[] {
		if (successors && successors.length > 0) {
			return successors.map(dep => ({
				from: taskNumber,
				to: dep
			}));
		}
		return [];
	}

	/**
	 * TreeLayout to use (if selected) by the diagram
	 **/
	treeLayout(): void {
		this.graph.setTreeLayout();
	}

	/**
	 * LayeredDigraphLayout to use (if selected) by the diagram
	 **/
	layeredDigraphLayout(): void {
		this.graph.setLayeredDigraphLayout();
	}

	/**
	 * ForceDirectedLayout to use (if selected) by the diagram
	 **/
	forceDirectedLayout(): void {
		this.graph.setForceDirectedLayout();
	}

	/**
	 * highlight all nodes on the diagram
	 **/
	highlightAll(): void {
		this.graph.highlightAllNodes();
	}

	/**
	 * highlight nodes by category name on the diagram
	 **/
	highlightByCategory(category: string): void {
		const matches = [category];
		this.graph.highlightNodesByCategory(matches);
	}

	/**
	 * highlight nodes by status type on the diagram
	 **/
	highlightByStatus(status: string): void {
		const matches = [status];
		this.graph.highlightNodesByStatus(matches);
	}

	/**
	 * highlight nodes by team on the diagram
	 **/
	highlightByTeam(team: string): void {
		const matches = [team];
		this.graph.highlightNodesByTeam(matches);
	}

	/**
	 * Zoom in on the diagram
	 **/
	zoomIn() {
		this.graph.zoomIn();
	}

	/**
	 * Zoom out on the diagram
	 **/
	zoomOut() {
		this.graph.zoomOut();
	}

	/**
	 * When events dropdown is opened remove z-index from minimap so that the options are visible
	 **/
	eventsDropdownOpened(): void {
		this.eventsDropdown.open.subscribe(() => {
			this.isEventDropdownOpen = false;
			this.graph.resetOverviewIndex();
		})
	}

	/**
	 * When events dropdown is closed reset z-index on minimap so that it's visible on top of th diagram
	 **/
	eventsDropdownClosed(): void {
		this.eventsDropdown.close.subscribe(() => {
			this.isEventDropdownOpen = false;
			this.graph.restoreOverviewIndex();
		})
	}

	/**
	 * When Team Highlight dropdown is opened remove z-index from minimap so that the options are visible
	 **/
	teamHighlightDropdownOpened(): void {
		this.teamHighlightDropdown.open.subscribe(() => {
			this.isTeamHighlightDropdownOpen = false;
			this.graph.resetOverviewIndex();
		})
	}

	/**
	 * When Team Highlight dropdown is closed reset z-index on minimap so that it's visible on top of th diagram
	 **/
	teamHighlightDropdownClosed(): void {
		this.teamHighlightDropdown.close.subscribe(() => {
			this.isTeamHighlightDropdownOpen = false;
			this.graph.restoreOverviewIndex();
		})
	}

	/**
	 * When highlight filter change update search
	 **/
	highlightFilterChange(): void {
		this.textFilter.next(this.filterText);
	}

	/**
	 * Highlight filter subscription
	 **/
	subscribeToHighlightFilter(): void {
		this.textFilter
			.pipe(
				skip(2),
				distinct()
			).subscribe(text => {
				this.graph.highlightNodesByText(text);
		});
	}

	refreshDiagram(): void {
		this.loadAll();
	}

	/**
	 * Put the task on start status
	 **/
	start(data?: ITaskEvent): void {
		if (data) {
			console.log('start data: ', data);
			const payload = {
				id: `${data.task.id}`,
				status: TaskStatus.STARTED,
				currentStatus: data.task.status
			};

			this.taskService.updateTaskStatus(payload)
				.subscribe((result) => {
					if (result) {
						this.updateGraphNode(result.assetComment);
					}
				});
		}
	}

	/**
	 * Put the task on hold status
	 **/
	hold(data?: ITaskEvent): void {

		if (data) {
			console.log('start data: ', data);
			const payload = {
				id: `${data.task.id}`,
				status: TaskStatus.HOLD,
				currentStatus: data.task.status
			};

			this.taskService.updateTaskStatus(payload)
				.subscribe((result) => {
					if (result) {
						this.updateGraphNode(result.assetComment);
					}
				});
		}

	}

	/**
	 * Put the task on done status
	 **/
	done(data?: ITaskEvent): void {

		if (data) {
			console.log('start data: ', data);
			const payload = {
				id: `${data.task.id}`,
				status: TaskStatus.COMPLETED,
				currentStatus: data.task.status
			};

			this.taskService.updateTaskStatus(payload)
				.subscribe((result) => {
					if (result) {
						this.updateGraphNode(result.assetComment);
					}
				});
		}

	}

	/**
	 * Put the task on invoke status
	 **/
	invoke(data?: ITaskEvent): void {
		this.taskService.invokeAction(`${data.task.id}`)
			.subscribe((result) => {
				console.log('result: ', result);
			});
	}

	/**
	 * Put the task on reset status
	 **/
	reset(data?: ITaskEvent): void {

		this.taskService.resetTaskAction(Number(data.task.id))
			.subscribe((result) => {
				console.log('result: ', result);
			});

	}

	/**
	 * Show task detail context menu option
	 **/
	showTaskDetails(data?: ITaskEvent): void {
		if (data.task) { data.task.predecessorList = data.task.predecessors || [1, 2, 3] }
		if (data.task) { data.task.successorList = data.task.successors || [] }

		let taskDetailModel: TaskDetailModel = {
			id: `${data.task.id}`,
			modal: {
				title: 'Task Detail'
			},
			detail: {
				currentUserId: this.userContext.user.id
			}
		};

		this.dialogService.extra(TaskDetailComponent, [
			{provide: TaskDetailModel, useValue: taskDetailModel}
		], false, false)
			.then(result => {
				console.log('result: ', result);
				this.updateGraphNode(result.assetComment);
			}).catch(result => {
			if (result) {
				console.log('catch: ', result);
			}
		});
	}

	/**
	 * Open the Task Edit Modal
	 * @param data
	 */
	editTask(data?: ITaskEvent): void {
		let taskDetailModel: TaskDetailModel = new TaskDetailModel();

		this.taskService.getTaskDetails(`${data.task.id}`)
			.subscribe((res) => {
				let modelHelper = new TaskEditCreateModelHelper(
					this.userContext.user,
					this.userContext.dateFormat,
					this.taskService, this.dialogService,
					this.translatePipe);
				taskDetailModel.detail = res;
				taskDetailModel.modal = {
					title: 'Task Edit',
					type: ModalType.EDIT
				};

				let model = modelHelper.getModelForDetails(taskDetailModel);
				model.instructionLink = modelHelper.getInstructionsLink(taskDetailModel.detail);
				model.durationText = DateUtils.formatDuration(model.duration, model.durationScale);
				model.modal = taskDetailModel.modal;

				this.dialogService.extra(TaskEditComponent, [
					{provide: TaskDetailModel, useValue: clone(model)}
				], false, false)
					.then(result => {
						if (result) {
							this.updateGraphNode(result.assetComment);
						}

					}).catch(result => {
					if (result) {
						console.error('Error: ', result)
					}
				});

			});
	}

	/**
	 * Show the asset popup detail.
	 * @param data: ITaskEvent
	 */
	showAssetDetail(data: ITaskEvent): void {
		const assetId = data.task.assetEntity.id;
		if (assetId) {
			this.taskService.getClassForAsset(`${assetId}`).subscribe(res => {
				if (res.assetClass) {
					this.dialogService.open(AssetShowComponent,
						[UIDialogService,
							{ provide: 'ID', useValue: data.task.assetEntity.id },
							{ provide: 'ASSET', useValue: res.assetClass },
							{ provide: 'AssetExplorerModule', useValue: AssetExplorerModule }
						], DIALOG_SIZE.LG).then(result => {
						// console.log('success: ' + result);
					}).catch(result => {
						console.error('rejected: ' + result);
					});
				} else {
					this.notifierService.broadcast({
						name: AlertType.DANGER,
						message: 'Invalid asset type'
					});
				}
			})
		}
	}

	/**
	 * Open the neighborhood window
	 */
	onNeighborhood(data?: ITaskEvent): void {
		const currentUrl = this.location.path().replace(this.urlParams.taskId, `${data.task.id}`);
		this.location.go(currentUrl);
		this.urlParams.taskId = data.task.id;
		this.loadTasks(Number(data.task.id));
	}

	/**
	 * Assign the task to the current user
	 */
	assignToMe(data?: ITaskEvent): void {
		if (data) {
			const payload = {
				id: `${data.task.id}`,
				status: data.task.status
			};

			this.taskService.assignToMe(payload)
				.subscribe((result) => {
					if (result) {
						data.task.assignedTo = result.assignedToName;
						this.updateGraphNode(data.task);
					}
				});
		}
	}

	updateGraphNode(node: IGraphTask): void {
		this.graph.updateNode(node);
	}

}
