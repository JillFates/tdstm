import { AfterViewInit, Component, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
import { FA_ICONS } from '../../../../shared/constants/fontawesome-icons';
import { ActivatedRoute, Router } from '@angular/router';
import { TaskManagerRoutingStates } from '../../task-manager-routing.states';
import { PermissionService } from '../../../../shared/services/permission.service';
import { Permission } from '../../../../shared/model/permission.model';

const routeNames = {
	TASK_MANAGER: 'taskManager',
	NEIGHBORHOOD: 'neighborhood',
	TASK_TIMELINE: 'taskTimeline'
};

@Component({
	selector: 'tds-task-view-toggle',
	template: `
		<div kendoTooltip position="top"
				 filter="span"
				 tooltipClass="tvt-tooltip"
				 class="text-center k-align-self-center">
			<span *ngIf="canViewTaskList()"
						title="Task Manager"
						id="task-manager"
						class="task-view-toggle-btn btn"
						(click)="taskManagerView()"
						#taskManager>
				<div>
					<fa-icon [icon]="icons.faListUl" size="lg"></fa-icon>
				</div>
			</span>
			<span *ngIf="canViewTaskGraph()"
						title="Task Graph"
						id="neighborhood"
						class="task-view-toggle-btn btn"
						(click)="neighborhoodView()"
						#neighborhood>
				<div>
					<fa-icon [icon]="icons.faSitemap" size="lg" [rotate]="270"></fa-icon>
				</div>
			</span>
			<span *ngIf="canViewTaskTimeline()"
						title="Task Timeline"
						id="task-timeline"
						class="task-view-toggle-btn btn"
						(click)="taskTimeLineView()"
						#taskTimeline>
				<div>
					<fa-icon [icon]="icons.faStream" size="lg"></fa-icon>
				</div>
			</span>
		</div>`
})
export class TaskViewToggleComponent implements AfterViewInit {
	icons = FA_ICONS;
	@ViewChild('taskManager', { static: false }) taskManager: ElementRef;
	@ViewChild('neighborhood', { static: false }) neighborhood: ElementRef;
	@ViewChild('taskTimeline', { static: false }) taskTimeline: ElementRef;
	disableNeighborhood: boolean;
	disableTaskManager: boolean;
	disableTaskTimeline: boolean;

	constructor(
		private router: Router,
		private activatedRoute: ActivatedRoute,
		private renderer: Renderer2,
		private permissionService: PermissionService
	) {
	}

	ngAfterViewInit(): void {
		this.subscribeToActivatedRoute();
	}

	/**
	 * validate current view to make corresponding toggle view button modifications
	 **/
	subscribeToActivatedRoute(): void {
		this.activatedRoute.url.subscribe(d => {
			if (d.find(p => p.path === TaskManagerRoutingStates.TASK_MANAGER_LIST.url)) {
				this.makeActive(this.taskManager);
				this.disableTaskManager = true;
				this.disableNeighborhood = false;
				this.disableTaskTimeline = false;
			} else if (d.find(p => p.path === TaskManagerRoutingStates.TASK_NEIGHBORHOOD.url)) {
				this.makeActive(this.neighborhood);
				this.disableNeighborhood = true;
				this.disableTaskManager = false;
				this.disableTaskTimeline = false;
			} else if (d.find(p => p.path === routeNames.TASK_TIMELINE)) {
				this.makeActive(this.taskTimeline);
				this.disableTaskTimeline = true;
				this.disableTaskManager = false;
				this.disableNeighborhood = false;
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
		if (this.disableTaskManager) {
			return;
		}
		const url = 'taskManager/list';
		window.open(url, '_blank');
	}

	/**
	 * open the neighborhood view on a new tab
	 **/
	neighborhoodView(): void {
		if (this.disableNeighborhood) {
			return;
		}
		const url = 'taskManager/task-graph';
		window.open(url, '_blank');
	}

	/**
	 * open the task timeline view on a new tab
	 **/
	taskTimeLineView(): void {
		if (this.disableTaskTimeline) {
			return;
		}
		// TODO remove currentUrl variable and 'task/' prefix after timeline is refactored to current angular version
		let currentUrl = window.location.href;
		currentUrl = currentUrl.substr(0, currentUrl.indexOf('module'));
		let url = `${ currentUrl }task/taskTimeline`;
		window.open(url, '_blank');
	}

	/**
	 * Returns true if user can access Task Manager view, otherwise false.
	 */
	canViewTaskList(): boolean {
		return this.permissionService.hasPermission(Permission.TaskManagerView);
	}

	/**
	 * Returns true if user can access Task Graph view, otherwise false.
	 */
	canViewTaskGraph(): boolean {
		return this.permissionService.hasPermission(Permission.TaskGraphView);
	}

	/**
	 * Returns true if user can access Task Timeline view, otherwise false.
	 */
	canViewTaskTimeline(): boolean {
		return this.permissionService.hasPermission(Permission.TaskTimelineView);
	}
}
