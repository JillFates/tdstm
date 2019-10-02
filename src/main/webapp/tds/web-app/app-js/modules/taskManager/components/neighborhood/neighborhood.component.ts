import {Component, OnInit, ViewChild} from '@angular/core';
import {Observable, BehaviorSubject, ReplaySubject} from 'rxjs';
import {distinct, map, skip} from 'rxjs/operators';
import {ActivatedRoute} from '@angular/router';

import {TaskService} from '../../service/task.service';
import {DiagramLayoutComponent} from '../../../../shared/components/diagram-layout/diagram-layout.component';
import {IGraphNode} from '../../model/graph-task.model';
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

export interface ILinkPath {
	from: number | string;
	to: number | string;
}

@Component({
	selector: 'tds-neighborhood',
	templateUrl: './neighborhood.component.html'
})
export class NeighborhoodComponent implements OnInit {
	tasks: IGraphNode[];
	nodeData$: BehaviorSubject<any[]> = new BehaviorSubject([]);
	links$: BehaviorSubject<any[]> = new BehaviorSubject([]);
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
	selectedTask: any;
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
	currentUserId$: ReplaySubject<string | number> = new ReplaySubject(1);

	constructor(
			private taskService: TaskService,
			private activatedRoute: ActivatedRoute,
			private userContextService: UserContextService,
			private reportService: ReportsService,
			private preferenceService: PreferenceService,
			private dialogService: UIDialogService
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
		// this.teamHighlightDropdownOpened();
		// this.teamHighlightDropdownClosed();
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
			nodeDataArr.push(t.task);
			linksPath.push(...this.getLinksPath(t.task.taskNumber, t.successors))
		});

		this.nodeData$.next(nodeDataArr);
		this.links$.next(linksPath);
		this.currentUserId$.next(this.userContext.user.id);
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
	start(): void {
		// TODO
	}

	/**
	 * Put the task on hold status
	 **/
	hold(): void {
		// TODO

	}

	/**
	 * Put the task on done status
	 **/
	done(): void {
		// TODO

	}

	/**
	 * Put the task on invoke status
	 **/
	invoke(): void {
		// TODO

	}

	/**
	 * Put the task on reset status
	 **/
	reset(): void {
		// TODO

	}

	/**
	 * Show task detail context menu option
	 **/
	showTaskDetails(id: string | number): void {
		let taskDetailModel: TaskDetailModel = {
			id: `${id}`,
			modal: {
				title: 'Task Detail'
			},
			detail: {
				currentUserId: this.userContext.user.id
			}
		};

		this.dialogService.open(TaskDetailComponent, [
			{provide: TaskDetailModel, useValue: taskDetailModel}
		])
			.then(result => {
				console.log('then: ', result);
			}).catch(result => {
			if (result) {
				console.log('catch: ', result);
			}
		});
	}

	/**
	 * Edit task context menu option
	 **/
	editTask(id: string | number): void {
		let taskDetailModel: TaskDetailModel = {
			id: `${id}`,
			modal: {
				title: 'Task Edit'
			},
			detail: {
				currentUserId: this.userContext.user.id
			}
		};

		this.dialogService.extra(TaskEditComponent, [
			{provide: TaskDetailModel, useValue: taskDetailModel}
		], false, false)
			.then(result => {
				console.log('then: ', result);
			}).catch(result => {
			if (result) {
				console.log('catch: ', result);
			}
		});
	}

}
