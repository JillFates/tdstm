import {Component, ElementRef, OnInit, Renderer2, ViewChild} from '@angular/core';
import {FA_ICONS} from '../../../../shared/constants/fontawesome-icons';
import {ActivatedRoute, Router} from '@angular/router';

const routeNames = {
	TASK_MANAGER: 'taskManager',
	NEIGHBORHOOD: 'neighborhood',
	TASK_TIMELINE: 'taskTimeline'
};

@Component({
	selector: 'tds-task-view-toggle',
	template: `
		<div kendoTooltip position="bottom" filter="span" tooltipClass="tvt-tooltip" class="text-center k-align-self-center">
			<span title="Task Manager" id="task-manager" class="task-view-toggle-btn btn" (click)="taskManagerView()" #taskManager>
				<div>
					<fa-icon [icon]="icons.faListUl" size="lg"></fa-icon>
				</div>
			</span>
			<span title="Task Graph" id="neighborhood" class="task-view-toggle-btn btn" (click)="neighborhoodView()" #neighborhood>
				<div>
					<fa-icon [icon]="icons.faSitemap" size="lg" [rotate]="270"></fa-icon>
				</div>
			</span>
			<span title="Task Timeline" id="task-timeline" class="task-view-toggle-btn btn" (click)="taskTimeLineView()" #taskTimeline>
				<div>
					<fa-icon [icon]="icons.faStream" size="lg"></fa-icon>
				</div>
			</span>
		</div>`
})
export class TaskViewToggleComponent implements OnInit {
	icons = FA_ICONS;
	@ViewChild('taskManager') taskManager: ElementRef;
	@ViewChild('neighborhood') neighborhood: ElementRef;
	@ViewChild('taskTimeline') taskTimeline: ElementRef;

	constructor(
		private router: Router,
		private activatedRoute: ActivatedRoute,
		private renderer: Renderer2
	) {	}

	ngOnInit(): void {
		this.subscribeToActivatedRoute();
	}

	/**
	 * validate current view to make corresponding toggle view button modifications
	 **/
	subscribeToActivatedRoute(): void {
		this.activatedRoute.url.subscribe(d => {
			if (d.find(p => p.path === routeNames.TASK_MANAGER)) {
				this.makeActive(this.taskManager);
			} else if (d.find(p => p.path === routeNames.NEIGHBORHOOD)) {
				this.makeActive(this.neighborhood);
			} else if (d.find(p => p.path === routeNames.TASK_TIMELINE)) {
				this.makeActive(this.taskTimeline);
			}
		});
	}

	/**
	 * add styles for active view toggle button and disable it
	 **/
	makeActive(element: ElementRef): void {
		this.renderer.addClass(element.nativeElement, 'tvt-btn-disabled');
		this.renderer.setAttribute(element.nativeElement, 'disabled', 'disabled');
	}

	/**
	 * open the task manager view on a new tab
	 **/
	taskManagerView(): void {
		const url = 'taskManager/list';
		window.open(url, '_blank');
	}

	/**
	 * open the neighborhood view on a new tab
	 **/
	neighborhoodView(): void {
		const url = 'neighborhood';
		window.open(url, '_blank');
	}

	/**
	 * open the task timeline view on a new tab
	 **/
	taskTimeLineView(): void {
		// TODO remove currentUrl variable and 'task/' prefix after timeline is refactored to current angular version
		let currentUrl = window.location.href;
		currentUrl = currentUrl.substr(0, currentUrl.indexOf('module'));
		let url = `${currentUrl}task/taskTimeline`;
		window.open(url, '_blank');
	}
}
