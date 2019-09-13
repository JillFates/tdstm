import {Component, ElementRef, OnInit, Renderer2, ViewChild} from '@angular/core';
import {of, Observable, BehaviorSubject, Subscription} from 'rxjs';
import {distinct, skip} from 'rxjs/operators';
import {ActivatedRoute} from '@angular/router';

import {TaskService} from '../../service/task.service';
import {DiagramLayoutComponent} from '../../../../shared/components/diagram-layout/diagram-layout.component';
import {IGraphTask} from '../../model/graph-task.model';
import {FA_ICONS} from '../../../../shared/constants/fontawesome-icons';
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
import {IMoveEvent} from '../../model/move-event.model';

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
	refreshCountdown$: Observable<number>;
	refreshCountdownSubscription: Subscription;
	refreshCountdownTime = 30;
	refreshCount: number;
	@ViewChild('refreshCircle') refreshCircle: ElementRef;

	constructor(
			private taskService: TaskService,
			private activatedRoute: ActivatedRoute,
			private renderer: Renderer2
		) {}

	ngOnInit() {
		this.setRefreshTime();
		this.loadAll();
	}

	loadAll(): void {
		this.subscribeToHighlightFilter();
		this.activatedRoute.queryParams.subscribe(params => {
			if (params && params.id) { this.loadTasks(params.id); }
		});
		this.loadEventList();
		this.eventsDropdownOpened();
		this.eventsDropdownClosed();
	}

	/**
		* Load tasks
		* @param {number} taskNumber to load tasks from
	 **/
	loadTasks(taskNumber: number): void {
		this.taskService.findTask(taskNumber)
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
		this.taskService.findMoveEvents().subscribe(res => {
			this.eventList$ = of(res);
			this.selectedEvent = res[0];
		});
	}

	/**
	 * Load tasks
	 * @param {number} moveEventId of moveEvent to load tasks from
	 **/
	loadFromSelectedEvent(moveEventId?: number): void {
		if (this.tasks) { return; }
		this.taskService.findTasksByMoveEventId(this.selectedEvent.id)
		.subscribe(res => {
			this.tasks = res;
			this.generateModel();
		});
	}

	/**
	 * generate model to be used by diagram with task specific data
	 **/
	generateModel(): void {
		const nodeDataArr = [];
		const linksPath = [];

		const tasksCopy = this.tasks.slice();

		const copySize = tasksCopy.length - 1;
		tasksCopy.map((t: IGraphTask) => {
			t.key = t.taskNumber;
			console.log('determined icon: ', t.status, t.status ? this.statusTypes[t.status.toLowerCase()] : this.statusTypes.pending);
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
		this.graph.layeredDigraphLayout();
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
		console.log('show details for task: ', id);
		this.selectedTask = { id };
		this.open();
	}

	editTask(id: number): void {
		console.log('edit task: ', id);
		this.selectedTask = { id };
		this.open();
	}

	public close(status) {
		console.log(`Dialog result: ${status}`);
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
			console.log('events dropdown opened ');
			this.isEventDropdownOpen = false;
			this.graph.resetOverviewIndex();
		})
	}

	/**
	 * When events dropdown is closed reset z-index on minimap so that it's visible on top of th diagram
	 **/
	eventsDropdownClosed(): void {
		this.eventsDropdown.close.subscribe(() => {
			console.log('events dropdown closed ');
			this.isEventDropdownOpen = false;
			this.graph.restoreOverviewIndex();
		})
	}

	/**
	 * When highlight filter change update search
	 **/
	filterChange(): void {
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
				console.log('text: ', text);
				this.graph.highlightNodesByText(text);
		});
	}

	setRefreshTime(): void {
		this.refreshCount = this.refreshCountdownTime;
		this.refreshCountdown$ = Observable.interval(1000)
			.map(x => Math.floor(--this.refreshCount));

		this.refreshCountdownSubscription = this.refreshCountdown$
			.subscribe(x => this.refreshCount =  x <= 0 ? this.refreshCountdownTime : x);
	}

	onRefreshCountdownClick(): void {
		this.refreshCountdownTime += 30;

		this.renderer.setStyle(
				this.refreshCircle.nativeElement,
				'animation', `countdown ${this.refreshCountdownTime}s linear infinite forwards !important`);

		this.refreshCountdownSubscription.unsubscribe();

		this.setRefreshTime();
	}

	refreshDiagram(): void {
		this.loadAll();
	}

	avoidDefault(): boolean {
		return false;
	}

}
