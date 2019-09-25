import {Component, OnInit, Renderer2, ViewChild} from '@angular/core';
import {of, Observable, BehaviorSubject} from 'rxjs';
import {distinct, map, skip} from 'rxjs/operators';
import {ActivatedRoute} from '@angular/router';

import {TaskService} from '../../service/task.service';
import {DiagramLayoutComponent} from '../../../../shared/components/diagram-layout/diagram-layout.component';
import {IGraphTask} from '../../model/graph-task.model';
import {FA_ICONS} from '../../../../shared/constants/fontawesome-icons';
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
import {IMoveEvent} from '../../model/move-event.model';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {UserContextService} from '../../../auth/service/user-context.service';
import {ReportsService} from '../../../reports/service/reports.service';

export interface ILinkPath {
	from: number | string;
	to: number | string;
}

@Component({
	selector: 'tds-neighborhood',
	templateUrl: './neighborhood.component.html',
	// styleUrls: ['../../../../../css/page/module/taskManager/neighborhood.component.scss']
})
export class NeighborhoodComponent implements OnInit {
	tasks: IGraphTask[];
	nodeData$: Observable<any[]>;
	links$: Observable<any[]>;
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
		forward: 'forward'
	};
	opened: boolean;
	selectedTask: any;
	filterText: string;
	textFilter: BehaviorSubject<string> = new BehaviorSubject<string>('');
	icons = FA_ICONS;
	selectedEvent: IMoveEvent;
	eventList$: Observable<IMoveEvent[]>;
	isEventDropdownOpen: boolean;
	TASK_MANAGER_REFRESH_TIMER: string = PREFERENCES_LIST.TASK_MANAGER_REFRESH_TIMER;
	userContext: UserContextModel;
	viewUnpublished: boolean;
	myTasks: boolean;
	minimizeAutoTasks: boolean;
	urlParams: any;

	constructor(
			private taskService: TaskService,
			private activatedRoute: ActivatedRoute,
			private renderer: Renderer2,
			private userContextService: UserContextService,
			private reportService: ReportsService,
			private preferenceService: PreferenceService
		) {
				this.activatedRoute.queryParams.subscribe(params => {
					if (params) { this.urlParams = params; }
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
		* @param {number} taskNumber to load tasks from
	 **/
	loadTasks(taskNumber: number): void {

		const filters = {
			myTasks: this.myTasks ? '1' : '0',
			minimizeAutoTasks: this.minimizeAutoTasks ? '1' : '0',
			viewUnpublished: this.viewUnpublished ? '1' : '0'
		};
		this.taskService.findTask(taskNumber, filters)
			.subscribe((res: IGraphTask[]) => {
				if (res && res.length > 0) {
					this.tasks = res;
					this.generateModel();
				}
			});
	}

	/**
	 * Load events to fill events dropdown
	 **/
	loadEventList() {
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
			this.tasks = res;
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
		// If actual task used by diagram comes from task manager neighborhood button, then reload
		// from tasks endpoint, else load tasks from the actual selected event
		if (this.urlParams && this.urlParams.taskId
			&& !!this.tasks.find(t => t.taskNumber === this.urlParams.taskId)) {
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

		tasksCopy.map((t: IGraphTask) => {
			t.key = t.taskNumber;
			t.icon = t.status ? this.statusTypes[t.status.toLowerCase()] : this.statusTypes.pending;
			nodeDataArr.push(t);
		});
		tasksCopy
		.forEach((task: IGraphTask) => linksPath.push(...this.getLinksPath(task)));
		// console.log('tasks found', this.tasks.length, 'tasks dependencies', linksPath);
		this.nodeData$ = of(nodeDataArr);
		this.links$ = of(linksPath);
	}

	/**
	 * Load events to fill events dropdown
	 **/
	getLinksPath(task: IGraphTask): ILinkPath[] {
		const t = Object.assign({}, task);
		if (t.successors) {
			return t.successors.map(dep => ({
				from: t.taskNumber,
				to: dep
			}));
		}
		return [];
	}

	/**
	 * TreeLayout to use (if selected) by the diagram
	 **/
	treeLayout(): void {
		console.log('Tree Layout selected');
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

	showTaskDetails(id: number): void {
		this.selectedTask = { id };
		this.open();
	}

	editTask(id: number): void {
		this.selectedTask = { id };
		this.open();
	}

	public close(status?: any) {
		this.opened = false;
	}

	public open() {
		this.opened = true;
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

	avoidDefault(): boolean {
		return false;
	}

}
