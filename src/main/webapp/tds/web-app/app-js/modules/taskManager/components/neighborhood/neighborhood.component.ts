import {Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {BehaviorSubject, Observable, ReplaySubject} from 'rxjs';
import {distinctUntilChanged, map, skip, takeUntil, timeout} from 'rxjs/operators';
import {ActivatedRoute} from '@angular/router';
import {Location} from '@angular/common';
import {clone} from 'ramda';
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
import {DropDownButtonComponent} from '@progress/kendo-angular-buttons/dist/es2015/dropdownbutton/dropdownbutton.component';

import {TaskService} from '../../service/task.service';
import {IGraphTask, TASK_TOOLTIP_FIELDS} from '../../model/graph-task.model';
import {FA_ICONS} from '../../../../shared/constants/fontawesome-icons';
import {IMoveEvent} from '../../model/move-event.model';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {UserContextService} from '../../../auth/service/user-context.service';
import {ReportsService} from '../../../reports/service/reports.service';
import {TaskDetailModel} from '../../model/task-detail.model';
import {TaskDetailComponent} from '../detail/task-detail.component';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {TaskEditCreateComponent} from '../edit-create/task-edit-create.component';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {TaskActionEvents} from '../common/constants/task-action-events.constant';
import {TaskStatus} from '../../model/task-edit-create.model';
import {ITaskEvent} from '../../model/task-event.model';
import {ASSET_ICONS_PATH, CTX_MENU_ICONS_PATH, STATE_ICONS_PATH} from '../common/constants/task-icon-path';
import {ILinkPath} from '../../../../shared/components/diagram-layout/model/legacy-diagram-layout.model';
import {DIALOG_SIZE, ModalType} from '../../../../shared/model/constants';
import {AssetShowComponent} from '../../../assetExplorer/components/asset/asset-show.component';
import {AssetExplorerModule} from '../../../assetExplorer/asset-explorer.module';
import {TaskEditCreateModelHelper} from '../common/task-edit-create-model.helper';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {AlertType} from '../../../../shared/model/alert.model';
import {Title} from '@angular/platform-browser';
import {TaskTeam} from '../common/constants/task-team.constant';
import {DiagramEventAction} from '../../../../shared/components/diagram-layout/model/legacy-diagram-event.constant';
import {DiagramLayoutService} from '../../../../shared/services/diagram-layout.service';
import {SetEvent} from '../../../event/action/event.actions';
import {Store} from '@ngxs/store';
import {TaskGraphDiagramHelper} from './task-graph-diagram.helper';
import {Diagram, Node} from 'gojs';
import {PermissionService} from '../../../../shared/services/permission.service';
import {
	ITdsContextMenuModel
} from 'tds-component-library/lib/context-menu/model/tds-context-menu.model';

@Component({
	selector: 'tds-neighborhood',
	templateUrl: './neighborhood.component.html'
})
export class NeighborhoodComponent implements OnInit, OnDestroy {
	tasks: IGraphTask[];
	nodeData$: BehaviorSubject<any> = new BehaviorSubject({});
	ctxMenuOpts$: ReplaySubject<ITdsContextMenuModel> = new ReplaySubject(2);
	diagramData$: ReplaySubject<any> = new ReplaySubject<any>(1);
	@ViewChild('graph', {static: false}) graph: any;
	@ViewChild('eventsDropdown', {static: false}) eventsDropdown: DropDownListComponent;
	@ViewChild('teamHighlightDropdown', {static: false}) teamHighlightDropdown: DropDownButtonComponent;
	@ViewChild('highlightFilterText', {static: false}) highlightFilterText: ElementRef<HTMLElement>;
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
	textFilter: ReplaySubject<string> = new ReplaySubject<string>(1);
	icons = FA_ICONS;
	selectedEvent: IMoveEvent;
	eventList$: Observable<IMoveEvent[]>;
	TASK_MANAGER_REFRESH_TIMER: string = PREFERENCES_LIST.TASK_MANAGER_REFRESH_TIMER;
	userContext: UserContextModel;
	viewUnpublished: boolean;
	myTasks: boolean;
	minimizeAutoTasks: boolean;
	urlParams: any;
	teamHighlights$: ReplaySubject<any[]> = new ReplaySubject<any[]>(1);
	selectedTeam: any;
	currentUser$: ReplaySubject<any> = new ReplaySubject(1);
	ctxMenuIcons = CTX_MENU_ICONS_PATH;
	unsubscribe$: ReplaySubject<void> = new ReplaySubject(1);
	isFullView: boolean;
	isMoveEventReq: boolean;
	requestId: number;
	refreshTriggered: boolean;
	isNeighbor: boolean;
	taskCycles: number[][];
	hasCycles: boolean;
	showCycles: boolean;
	rootId: number;
	neighborId: any;

	constructor(
			private taskService: TaskService,
			private activatedRoute: ActivatedRoute,
			private userContextService: UserContextService,
			private reportService: ReportsService,
			private preferenceService: PreferenceService,
			private dialogService: UIDialogService,
			private notifierService: NotifierService,
			private location: Location,
			private translatePipe: TranslatePipe,
			private titleService: Title,
			private diagramLayoutService: DiagramLayoutService,
			private permissionService: PermissionService,
			private store: Store
		) {
				this.activatedRoute.queryParams
					.pipe(takeUntil(this.unsubscribe$))
					.subscribe(params => {
					if (params) { this.urlParams = Object.assign({}, params); }
				});
				this.subscribeToNotifications();
	}

	ngOnInit() {
		this.titleService.setTitle('Task Graph');
		this.loadAll();
	}

	loadAll(): void {
		this.subscribeToHighlightFilter();
		this.loadUserContext();
		this.loadFilters();
		this.loadEventList();
	}

	/**
	 *  subscribe to notifications from the Notifier Service
	 */
	subscribeToNotifications(): void {
		this.notifierService.on(DiagramEventAction.ANIMATION_FINISHED, () => this.highlightNeighbor(this.neighborId));
	}

	/**
	 * Subscribe to events with the notifierService
	 */
	onActionDispatched(data: any): void {
		switch (data.name) {
			case TaskActionEvents.START:
				this.start({name: data.name, task: data.node});
				break;
			case TaskActionEvents.HOLD:
				this.hold({name: data.name, task: data.node});
				break;
			case TaskActionEvents.DONE:
				this.done({name: data.name, task: data.node});
				break;
			case TaskActionEvents.RESET:
				this.reset({name: data.name, task: data.node});
				break;
			case TaskActionEvents.INVOKE:
				this.invoke({name: data.name, task: data.node});
				break;
			case TaskActionEvents.EDIT:
				this.editTask({name: data.name, task: data.node});
				break;
			case TaskActionEvents.SHOW:
				this.showTaskDetails({name: data.name, task: data.node});
				break;
			case TaskActionEvents.NEIGHBORHOOD:
				this.onNeighborhood(data.node);
				break;
			case TaskActionEvents.ASSIGN_TO_ME:
				this.assignToMe({name: data.name, task: data.node});
				break;
			case TaskActionEvents.SHOW_ASSET_DETAIL:
				this.showAssetDetail({name: data.name, task: data.node});
				break;
		}
	}

	/**
	 * Load user preferences to filter tasks and then load tasks
 	**/
	loadFilters(): void {
		this.preferenceService.getPreferences(
			PREFERENCES_LIST.MY_TASKS,
			PREFERENCES_LIST.MINIMIZE_AUTO_TASKS,
			PREFERENCES_LIST.VIEW_UNPUBLISHED
		)
			.pipe(takeUntil(this.unsubscribe$))
			.subscribe(res => {
				const {myTasks, minimizeAutoTasks, VIEW_UNPUBLISHED} = res;
				this.myTasks = (myTasks && myTasks === 'true' || myTasks === '1');
				this.minimizeAutoTasks = (minimizeAutoTasks && (minimizeAutoTasks === 'true' || minimizeAutoTasks === '1'));
				this.viewUnpublished = (VIEW_UNPUBLISHED && (VIEW_UNPUBLISHED === 'true' || VIEW_UNPUBLISHED === '1'));

				this.loadData();
			});
	}

	/**
	 * If this a neighborhood view from the task manager, then load tasks from task service, otherwise load tasks
	 from selected event
	 */
	loadData(): void {
		if (this.urlParams && this.urlParams.taskId) {
			this.loadTasks(this.urlParams.taskId);
		} else {
			this.loadFromSelectedEvent();
		}
	}

	/**
	 * Load user context
	 **/
	loadUserContext(): void {
		this.userContextService.getUserContext()
			.pipe(takeUntil(this.unsubscribe$))
			.subscribe((userContext: UserContextModel) => {
			this.userContext = userContext;
			this.selectedEvent = userContext.event;
		});
	}

	/**
		* Load tasks
		* @param {number} taskId to load tasks from
	 **/
	loadTasks(taskId: number): void {

		if (this.isFullView
			&& this.diagramLayoutService.getRequestId() === taskId
			&& !this.diagramLayoutService.isCacheFromMoveEvent()
			&& !this.refreshTriggered
			&& !this.isNeighbor
		) {
			this.generateModelFromCache();
		} else {
			const filters = {
				myTasks: this.myTasks ? '1' : '0',
				minimizeAutoTasks: this.minimizeAutoTasks ? '1' : '0',
				viewUnpublished: this.viewUnpublished ? '1' : '0'
			};
			this.taskService.findTask(taskId, filters)
				.pipe(
					takeUntil(this.unsubscribe$),
					timeout(15000)
				)
				.subscribe(res => {
					if (res.body.status === 'error') {
						if (this.isNeighbor) {
							this.isFullView = true;
							this.isNeighbor = false;
							this.graph.showFullGraphBtn = false;
						}
						return;
					}
					const data = res.body.data;
					if (data && data.length > 0) {
						this.rootId = taskId;
						this.tasks = data && data.map(r => r.task);
						if (this.tasks) {
							if (!this.isNeighbor) {
								this.diagramLayoutService.clearFullGraphCache();
								this.requestId = taskId;
								this.isMoveEventReq = false;
							} else {
								this.graph.cleanUpDiagram();
								this.graph.showFullGraphBtn = true;
								this.neighborId = taskId;
							}
							this.generateModel();
						}
					}
				},
					error => console.error(`Could not load tasks ${error}`));
		}
		this.refreshTriggered = false;
	}

	/**
	 * Load events to fill events dropdown
	 **/
	loadEventList() {
			this.eventList$ = this.reportService
				.getEventList()
				.pipe(
					takeUntil(this.unsubscribe$),
					map(res => res.data));
	}

	/**
	 * Load tasks from a moveEvent Id
	 **/
	loadFromSelectedEvent(): void {
		if (this.urlParams) { this.urlParams = null; }

		if (this.isFullView
			&& this.diagramLayoutService.getRequestId() === this.selectedEvent.id
			&& this.diagramLayoutService.isCacheFromMoveEvent()
			&& !this.refreshTriggered) {
			this.generateModelFromCache();
		} else if (this.selectedEvent && this.selectedEvent.id) {
			const filters = {
				myTasks: this.myTasks ? '1' : '0',
				minimizeAutoTasks: this.minimizeAutoTasks ? '1' : '0',
				viewUnpublished: this.viewUnpublished ? '1' : '0'
			};
			this.taskService.findTasksByMoveEventId(this.selectedEvent.id, filters)
				.pipe(
					takeUntil(this.unsubscribe$)
				)
				.subscribe(res => {
					this.tasks = res && res.tasks;
					if (this.tasks) {
						this.diagramLayoutService.clearFullGraphCache();
						this.requestId = this.selectedEvent.id;
						this.isMoveEventReq = false;
						if (res.cycles && res.cycles.length > 0) {
							this.hasCycles = true;
							this.taskCycles = res.cycles;
						}
						this.generateModel();
					}
				},
					error => console.error(`Could not load tasks ${error}`));
		}
		this.refreshTriggered = false;
	}

	/**
	 * Event selection handler
	 * @param {IMoveEvent}  moveEvent to load tasks from
	 **/
	onEventSelect(moveEvent?: IMoveEvent): void {
		if (moveEvent) { this.selectedEvent = moveEvent; }

		this.loadFromSelectedEvent();

		this.store.dispatch(new SetEvent({ id: this.selectedEvent.id, name: this.selectedEvent.name }));
	}

	/**
	 * load tasks with new filter criteria
	 **/
	onMyTasksFilterChange(): void {
		if (this.myTasks) {
			this.graph.highlightNodes((n: Node) => n.data.myTask);
		} else {
			this.graph.clearHighlights();
		}
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

	/**
	 * Checkbox filter change handler
	 */
	checkboxFilterChange(): void {
		this.refreshTriggered = true;
		this.loadData();
	}

	/**
	 * generate model to be used by diagram with task specific data
	 **/
	generateModel(): void {
		if (!this.tasks) { return; }
		const taskGraphHelper = new TaskGraphDiagramHelper(this.permissionService, {currentUser: this.userContext.person});
		const nodeDataArray = [];
		const linkDataArray = [];
		const teams = [{label: TaskTeam.ALL_TEAMS}, {label: TaskTeam.NO_TEAM_ASSIGNMENT}];

		const tasksCopy = this.tasks.slice();

		// Add tasks to nodeData constant
		// and create linksPath object from number and successors
		tasksCopy.map((t: IGraphTask | any) => {

			const predecessorIds = t.predecessorIds && t.predecessorIds;

			t.key = t.id;
			t.rootNodeKey = this.rootId;
			t.tooltipData = this.tooltipData(t);
			if (t.team && !teams
				.find(team => t.team.trim().toLowerCase() === team.label.trim().toLowerCase())) {
				teams.push({label: t.team});
			}
			nodeDataArray.push(t);

			if (predecessorIds && predecessorIds.length > 0) {
				linkDataArray.push(...this.getLinksPathByPredecessorIds(t.id, predecessorIds));
			}
		});
		this.teamHighlights$.next(teams);
		if (!this.isNeighbor) {
			this.cacheFullGraph({
				requestId: this.requestId,
				isMoveEvent: this.isMoveEventReq,
				data: nodeDataArray,
				linksPath: linkDataArray
			});
		}
		this.diagramData$.next(taskGraphHelper.diagramData({
			rootNode: this.rootId,
			currentUserId: this.userContext.user.id,
			nodeDataArray,
			linkDataArray,
			extras: {
				diagramOpts: {
					initialAutoScale: Diagram.Uniform,
					allowZoom: true
				},
				isExpandable: false
			}
		}));
	}

	tooltipData(t: IGraphTask): any {
		const stateIcon = STATE_ICONS_PATH[t.status && t.status.toLowerCase()];
		const unknownBg = ASSET_ICONS_PATH.unknown.background;
		const assetIcon = ASSET_ICONS_PATH[(t.asset && t.asset.assetType) && t.asset.assetType.toLowerCase()];
		return {
			headerText: `${t.number}:${t.name}`,
				headerBackgroundColor: stateIcon && stateIcon.background || unknownBg,
				headerTextColor: stateIcon && stateIcon.color || unknownBg,
				data: [
					{
						label: TASK_TOOLTIP_FIELDS.STATUS,
						value: t.status
					},
					{
						label: TASK_TOOLTIP_FIELDS.ASSIGNED_TO,
						value: t.assignedTo
					},
					{
						label: TASK_TOOLTIP_FIELDS.TEAM,
						value: t.team
					},
					{
						label: TASK_TOOLTIP_FIELDS.ASSET,
						value: t.asset && t.asset.assetType,
						icon: assetIcon && assetIcon.iconName
					}
			]
		}
	}

	/**
	 * generate model to be used by diagram with task specific data
	 **/
	generateModelFromCache(): void {
		if (!this.diagramLayoutService.getRequestId()) { return; }
		const teams = [{label: TaskTeam.ALL_TEAMS}, {label: TaskTeam.NO_TEAM_ASSIGNMENT}];

		const graphCache = this.diagramLayoutService.getFullGraphCache();

		// Add tasks to nodeData constant
		// and create linksPath object from number and successors
		graphCache.data.forEach((t: IGraphTask | any) => {
			if (t.team && !teams
				.find(team => t.team.trim().toLowerCase() === team.label.trim().toLowerCase())) {
				teams.push({label: t.team});
			}
		});
		this.teamHighlights$.next(teams);
		this.viewFullGraphFromCache();
	}

	/**
	 * Creates a cache to hold the full graph so that a new call won't be needed when returning from a neighbor
	 * @param data
	 */
	cacheFullGraph(data: any): void {
		if (!this.diagramLayoutService.getFullGraphCache() || !this.diagramLayoutService.getFullGraphCache().data) {
			this.diagramLayoutService.setFullGraphCache(data);
			this.isFullView = true;
		}
	}

	/**
	 * create LinksPath object from task succesors
	 * @param {string | number} taskNumber
	 * @param {number[]} successors
	 **/
	getLinksPathBySuccessors(taskNumber: string | number, successors: number[]): ILinkPath[] {
		if (successors && successors.length > 0) {
			return successors.map(dep => ({
				from: taskNumber,
				to: dep
			}));
		}
		return [];
	}

	/**
	 * create LinksPath object from task predecessorIds
	 * @param {string | number} taskId
	 * @param {number[]} predecessorIds
	 **/
	getLinksPathByPredecessorIds(taskId: string | number, predecessorIds: number[]): ILinkPath[] {
		if (predecessorIds && predecessorIds.length > 0) {
			return predecessorIds
				.filter(f => !!this.tasks.find(t => t.id === f))
				.map(pre => ({
				from: pre,
				to: taskId
			}));
		}
		return [];
	}

	/**
	 * highlight nodes by team on the diagram
	 **/
	highlightByTeam(team: any): void {
		this.teamTextFilter(this.filterText, team && team.label);
	}

	/**
	 * highlight nodes by cycles
	 **/
	highlightCycles(): void {
		if ((this.taskCycles && this.taskCycles.length > 0) && this.showCycles) {
			const cycles = [];
			this.taskCycles.forEach(arr => cycles.push(...arr));
			this.graph.highlightNodes((n: Node) => cycles.includes(n.data.id), true);
		} else {
			this.graph.clearHighlights();
		}
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
				takeUntil(this.unsubscribe$),
				skip(2),
				distinctUntilChanged()
			).subscribe(match => {
				this.teamTextFilter(match, this.selectedTeam && this.selectedTeam.label);
		});
	}

	/**
	 * Team + text filter criteria and validation
	 * @param match
	 * @param team
	 */
	teamTextFilter(match?: string, team?: string): void {
		if (team && match) {
			if (team === TaskTeam.ALL_TEAMS) {
				this.graph.highlightNodes((n: Node) => !!n.data.name.toLowerCase().includes(match.toLowerCase())
					|| (n.data.assignedTo && !!n.data.assignedTo.toLowerCase().includes(match.toLowerCase())));
			} else if (team === TaskTeam.NO_TEAM_ASSIGNMENT) {
				this.graph.highlightNodes((n: Node) => (n.data.name.toLowerCase().includes(match.toLowerCase())
					|| n.data.assignedTo && !!n.data.assignedTo.toLowerCase().includes(match.toLowerCase()))
					&& !n.data.team);
			} else {
				this.graph.highlightNodes((n: Node) => {
					return (!!n.data.name.toLowerCase().includes(match.toLowerCase())
						|| (n.data.assignedTo && !!n.data.assignedTo.toLowerCase().includes(match.toLowerCase())))
						&& n.data.team && !!n.data.team.includes(team)
				});
			}
		} else if (team) {
			if (team === TaskTeam.ALL_TEAMS) {
				this.graph.clearHighlights();
			} else if (team === TaskTeam.NO_TEAM_ASSIGNMENT) {
				this.graph.highlightNodes((n: Node) => !n.data.team);
			} else {
				this.graph.highlightNodes((n: Node) => team === n.data.team);
			}
		} else if (match) {
			this.graph.highlightNodes((n: Node) => n.data.name.toLowerCase().includes(match.toLowerCase()));
		} else {
			this.graph.clearHighlights();
		}
	}

	/**
	 * Reload Diagram data and re-render
	 */
	refreshDiagram(): void {
		this.refreshTriggered = true;
		this.subscribeToHighlightFilter();
		this.loadData();
	}

	/**
	 * Put the task on start status
	 **/
	start(data?: ITaskEvent): void {
		if (data) {
			const payload = {
				id: `${data.task.id}`,
				status: TaskStatus.STARTED,
				currentStatus: data.task.status
			};

			this.taskService.updateTaskStatus(payload)
				.pipe(takeUntil(this.unsubscribe$))
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
			const payload = {
				id: `${data.task.id}`,
				status: TaskStatus.HOLD,
				currentStatus: data.task.status
			};

			this.taskService.updateTaskStatus(payload)
				.pipe(takeUntil(this.unsubscribe$))
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
			const payload = {
				id: `${data.task.id}`,
				status: TaskStatus.COMPLETED,
				currentStatus: data.task.status
			};

			this.taskService.updateTaskStatus(payload)
				.pipe(takeUntil(this.unsubscribe$))
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
			.pipe(takeUntil(this.unsubscribe$))
			.subscribe((result) => {
				if (result) {
					this.updateGraphNode(result);
				}
			});
	}

	/**
	 * Put the task on reset status
	 **/
	reset(data?: ITaskEvent): void {

		this.taskService.resetTaskAction(Number(data.task.id))
			.pipe(takeUntil(this.unsubscribe$))
			.subscribe((result) => {
				if (result) {
					this.updateGraphNode(result);
				}
			});

	}

	/**
	 * Show task detail context menu option
	 **/
	showTaskDetails(data?: ITaskEvent): void {

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
				if (result && result.shouldEdit) {
					return this.editTask({task: {id: result.id && result.id.id}});
				}
				if (result && result.isDeleted) {
					const taskId = result.id && result.id.id;
					return this.taskService.deleteTaskComment(taskId)
						.subscribe(() => this.removeGraphNode(taskId));
				}
			}).catch(result => {
			if (result) {
				console.error('catch: ', result);
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
			.pipe(takeUntil(this.unsubscribe$))
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

				this.dialogService.extra(TaskEditCreateComponent, [
					{provide: TaskDetailModel, useValue: clone(model)}
				], false, false)
					.then(result => {
						if (result && result.isDeleted) {
							const taskId = result.id && result.id.id;
							return this.taskService.deleteTaskComment(taskId)
								.subscribe(() => this.removeGraphNode(taskId));
						} else if (result) {
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
		const asset = data.task.asset;
		if (asset  && asset.id) {
			this.taskService.getClassForAsset(`${asset.id}`)
				.pipe(takeUntil(this.unsubscribe$))
				.subscribe(res => {
				if (res.assetClass) {
					this.dialogService.open(AssetShowComponent,
						[UIDialogService,
							{ provide: 'ID', useValue: asset.id },
							{ provide: 'ASSET', useValue: res.assetClass },
							{ provide: 'AssetExplorerModule', useValue: AssetExplorerModule }
						], DIALOG_SIZE.LG).catch(result => {
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
	onNeighborhood(data?: IGraphTask): void {
		this.isFullView = false;
		this.isNeighbor = true;
		this.loadTasks(Number(data.id));
	}

	/**
	 * highlights neighbor task
	 * @param id
	 */
	highlightNeighbor(id: any): void {
		if (!this.isFullView && id) {
			const neighborHighlightFilter = (n: Node) => n.data && (n.data.key === id || n.data.id === id);
			this.graph.highlightNodes(neighborHighlightFilter);
		}
	}

	/**
	 * Diagram Animation Finished output handler
	 */
	onDiagramAnimationFinished(): void {
		this.notifierService.broadcast(DiagramEventAction.ANIMATION_FINISHED);
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
				.pipe(takeUntil(this.unsubscribe$))
				.subscribe((result) => {
					if (result) {
						data.task.assignedTo = result.assignedToName;
						this.updateGraphNode(data.task);
					}
				});
		}
	}

	/**
	 * update node on diagram
	 * @param {IGraphTask} node
	 */
	updateGraphNode(node: IGraphTask): void {
		this.graph.updateNode(node);
	}

	/**
	 * update cached graph
	 */
	updateGraphCache(data: any): void {
		if (this.isFullView) {
			const fullGraph = {
				requestId: this.diagramLayoutService.getRequestId(),
				isMoveEvent: this.diagramLayoutService.isCacheFromMoveEvent(),
				...data
			};
			this.diagramLayoutService.setFullGraphCache(fullGraph);
		}
	}

	/**
	 * Clear text filter
	 */
	clearTextFilter(): void {
		if (!this.filterText) { return; }
		this.highlightFilterText.nativeElement.nodeValue = '';
		this.filterText = '';
		this.textFilter.next(null);
	}

	/**
	 * View full graph from cache
	 */
	viewFullGraphFromCache(): void {
		const taskGraphHelper = new TaskGraphDiagramHelper(this.permissionService);
		this.isFullView = true;
		this.isNeighbor = false;
		this.graph.showFullGraphBtn = false;
		const cache = this.diagramLayoutService.getFullGraphCache();
		const nodeDataArray = cache && cache.data;
		const linkDataArray = cache && cache.linksPath;
		this.extractTeams(nodeDataArray);
		this.diagramData$.next(taskGraphHelper.diagramData({
			rootNode: this.rootId,
			currentUserId: this.userContext.user.id,
			nodeDataArray,
			linkDataArray,
			extras: {
				diagramOpts: {
					autoScale: Diagram.Uniform,
					allowZoom: false
				},
				isExpandable: false
			}
		}));
	}

	/**
	 * Team highlight dropdown population
	 * @param dataArr
	 */
	extractTeams(dataArr: any[]): void {
		const teams = [{label: TaskTeam.ALL_TEAMS}, {label: TaskTeam.NO_TEAM_ASSIGNMENT}];
		dataArr.forEach(t => {
			if (t.team && !teams
				.find(team => t.team.trim().toLowerCase() === team.label.trim().toLowerCase())) {
				teams.push({label: t.team});
			}
		});
		this.teamHighlights$.next(teams);
	}

	/**
	 * Diagram click output handler
	 */
	diagramClicked(): void {
		if (this.myTasks) {
			this.myTasks = false;
		}

		if (this.showCycles) {
			this.showCycles = false;
		}
		this.graph.clearHighlights();
	}

	/**
	 * Set new adornment for given node
	 * @param {any} data
	 */
	setNeighborAdornment(data: any): void {
		this.graph.diagram.commit(d => {
			const taskGraphHelper = new TaskGraphDiagramHelper(this.permissionService);
			const node = d.findNodeForKey(data.key);
			if (node) {
				node.selectionAdornmentTemplate = taskGraphHelper.neighborAdornmentTemplate();
				node.updateAdornments();
				d.select(node.part);
				d.centerRect(node.actualBounds);
			}
		});
	}

	/**
	 * remove node from the diagram
	 * @param {any} taskId
	 */
	removeGraphNode(taskId: any): void {
		this.graph.diagram.commit(d => {
			const node = d.findNodeForKey(taskId) || d.nodes
				.filter(n => n.data.key === taskId || n.data.id === taskId).first();
			if (node) {
				d.remove(node);
			}
		})
	}

	@HostListener('window:beforeunload', ['$event'])
	ngOnDestroy(): void {
		this.unsubscribe$.next();
		this.unsubscribe$.complete();
		this.diagramLayoutService.clearFullGraphCache();
	}

}
