import {Component, OnInit, Renderer2, ViewChild} from '@angular/core';
import {of, Observable, BehaviorSubject} from 'rxjs';
import {distinct, skip} from 'rxjs/operators';
import {ActivatedRoute} from '@angular/router';

import {TaskService} from '../../service/task.service';
import {DiagramLayoutComponent} from '../../../../shared/components/diagram-layout/diagram-layout.component';
import {IGraphTask} from '../../../../shared/model/graph-task.model';
import {FA_ICONS} from '../../../../shared/constants/fontawesome-icons';
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';

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
	selectedEvent: {text: string, value: string | number};
	eventList: {text: string, value: string | number}[];
	isEventDropdownOpen: boolean;

	constructor(
			private taskService: TaskService,
			private activatedRoute: ActivatedRoute,
			private renderer: Renderer2
		) {}

	ngOnInit() {
		this.subscribeToHighlightFilter();
		this.activatedRoute.queryParams.subscribe(params => {
			if (params && params.id) { this.loadTasks(params.id); }
		});
		this.eventsDropdownOpened();
		this.eventsDropdownClosed();
	}

	loadTasks(id: number): void {
		this.taskService.findTask(id)
			.subscribe((res: IGraphTask[]) => {
				if (res && res.length > 0) {
					this.tasks = res;
					this.eventList = this.getEventList(this.tasks);
					this.generateModel();
				}
			});
	}

	getEventList(tasks: IGraphTask[]): {text: string, value: string | number}[] {
		return tasks.map(t => ({text: t.label, value: t.id}));
	}

	loadFromSelectedTask(id?: number): void {
		this.taskService.findTask(this.selectedEvent.value)
		.subscribe(res => {
			this.tasks = res;
			this.eventList = this.getEventList(this.tasks);
			this.generateModel();
		});
	}

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

	treeLayout(): void {
		console.log('Tree Layout selected');
		this.graph.setTreeLayout();
	}

	layeredDigraphLayout(): void {
		this.graph.layeredDigraphLayout();
	}

	forceDirectedLayout(): void {
		this.graph.setForceDirectedLayout();
	}

	highlightAll(): void {
		this.graph.highlightAllNodes();
	}

	highlightByCategory(category: string): void {
		const matches = [category];
		this.graph.highlightNodesByCategory(matches);
	}

	highlightByStatus(status: string): void {
		const matches = [status];
		this.graph.highlightNodesByStatus(matches);
	}

	zoomIn() {
		this.graph.zoomIn();
	}

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

	eventsDropdownOpened(): void {
		this.eventsDropdown.open.subscribe(() => {
			console.log('events dropdown opened ');
			this.isEventDropdownOpen = false;
			this.graph.resetOverviewIndex();
		})
	}

	eventsDropdownClosed(): void {
		this.eventsDropdown.close.subscribe(() => {
			console.log('events dropdown closed ');
			this.isEventDropdownOpen = false;
			this.graph.restoreOverviewIndex();
		})
	}

	filterChange(): void {
		this.textFilter.next(this.filterText);
	}

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

}
