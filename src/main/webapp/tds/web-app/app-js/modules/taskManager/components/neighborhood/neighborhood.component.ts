import { Component, OnInit, ViewChild } from '@angular/core';
import {of, Observable, BehaviorSubject} from 'rxjs';
import {distinct, skip} from 'rxjs/operators';
import {ActivatedRoute} from '@angular/router';

import {TaskService} from '../../service/task.service';
import {LayeredDigraphLayoutComponent} from '../../../../shared/components/layered-digraph-layout/layered-digraph-layout.component';
import {IGraphTask} from '../../../../shared/model/graph-task.model';
import {FA_ICONS} from '../../../../shared/constants/fontawesome-icons';

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
	tasks: any[];
	nodeData$: Observable<any[]>;
	links$: Observable<any[]>;
	@ViewChild('graph') graph: LayeredDigraphLayoutComponent;
	statusTypes = {
		started: 'play',
		pause: 'pause',
		clock: 'clock',
		unknown: 'unknown',
		pending: 'clock'
	};
	opened: boolean;
	selectedTask: any;
	filterText: string;
	textFilter: BehaviorSubject<string> = new BehaviorSubject<string>('');
	icons = FA_ICONS;

	constructor(
			private taskService: TaskService,
			private activatedRoute: ActivatedRoute
		) {}

	ngOnInit() {
		this.subscribeToHighlightFilter();
		this.activatedRoute.queryParams.subscribe(params => {
			if (params && params.id) { this.loadTasks(params.id); }
		});
	}

	loadTasks(id: number): void {
		this.taskService.findTask(id)
			.subscribe((res: IGraphTask[]) => {
				this.tasks = res;
				console.log
				this.generateModel();
			});
	}

	loadFromSelectedTask(id): void {
		this.taskService.findTask(id)
		.subscribe(res => {
			this.tasks = res;
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
			console.log('determined icon: ', t.status ? this.statusTypes[t.status.toLowerCase()] : this.statusTypes.pending);
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
