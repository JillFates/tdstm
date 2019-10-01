import {Component, ElementRef, Input, OnInit, Renderer2, ViewChild} from '@angular/core';

import {CTX_MENU_ICONS_PATH} from '../../../constants/icons-path';
import {TaskDetailModel} from '../../../../modules/taskManager/model/task-detail.model';
import {TaskDetailComponent} from '../../../../modules/taskManager/components/detail/task-detail.component';
import {TaskEditComponent} from '../../../../modules/taskManager/components/edit/task-edit.component';
import {UIDialogService} from '../../../services/ui-dialog.service';
import {IHideBtn, ITaskContextMenuModel} from './task-context-menu.model';

@Component({
	selector: 'tds-task-context-menu',
	template: `<div id="ctx-menu"
									(contextmenu)="avoidDefault($event)"
									#ctxMenu>
		<ul>
			<li id="hold" *ngIf="hideBtn.hold">
				<button class="btn ctx-menu-btn">
					<fa-icon [icon]="ctxMenuIcons.hold.icon"
									 [styles]="{ color: ctxMenuIcons.hold.color, float: 'left' }">
					</fa-icon>
					Hold
				</button>
			</li>
			<li id="start" *ngIf="hideBtn.start">
				<button class="btn ctx-menu-btn">
					<fa-icon [icon]="ctxMenuIcons.start.icon"
									 [styles]="{ color: ctxMenuIcons.start.color, float: 'left' }">
					</fa-icon>
					Start
				</button>
			</li>
			<li id="done" *ngIf="hideBtn.done">
				<button class="btn ctx-menu-btn">
					<fa-icon [icon]="ctxMenuIcons.done.icon"
									 [styles]="{ color: ctxMenuIcons.done.color, float: 'left' }">
					</fa-icon>
					Done
				</button>
			</li>
			<li id="reset" *ngIf="hideBtn.reset">
				<button class="btn ctx-menu-btn">
					<fa-icon [icon]="ctxMenuIcons.invoke.icon"
									 [styles]="{ color: ctxMenuIcons.invoke.color, float: 'left' }">
					</fa-icon>
					Reset
				</button>
			</li>
			<li id="invoke" *ngIf="hideBtn.invoke">
				<button class="btn ctx-menu-btn">
					<fa-icon [icon]="ctxMenuIcons.invoke.icon"
									 [styles]="{ color: ctxMenuIcons.invoke.color, float: 'left' }">
					</fa-icon>
					Invoke
				</button>
			</li>
			<hr class="ctx-menu-divider"/>
			<li id="edit">
				<button class="btn ctx-menu-btn" (click)="editTask()">
					<fa-icon [icon]="ctxMenuIcons.edit.icon"
									 [styles]="{ color: ctxMenuIcons.edit.color, float: 'left' }">
					</fa-icon>
					Edit
				</button>
			</li>
			<li id="view">
				<button class="btn ctx-menu-btn" (click)="showTaskDetails()">
					<fa-icon [icon]="ctxMenuIcons.view.icon"
									 [styles]="{ color: ctxMenuIcons.view.color, float: 'left' }">
					</fa-icon>
					View
				</button>
			</li>
			<hr  class="ctx-menu-divider"/>
			<li id="assign-to-me">
				<button class="btn ctx-menu-btn">
					<fa-icon [icon]="ctxMenuIcons.assignToMe.icon"
									 [styles]="{ color: ctxMenuIcons.assignToMe.color, float: 'left' }">
					</fa-icon>
					Assign to me
				</button>
			</li>
		</ul>
	</div>`,
})
export class TaskContextMenuComponent implements OnInit {
	@Input() data: ITaskContextMenuModel;
	@ViewChild('ctxMenu') ctxMenu: ElementRef;
	ctxMenuIcons = CTX_MENU_ICONS_PATH;
	hideBtn: IHideBtn;

	constructor(
		private renderer: Renderer2,
		private dialogService: UIDialogService
	) {}

	ngOnInit(): void {
		if (this.data.selectedNode
				&& this.data.selectedNode.status
				&& this.hideBtn[this.data.selectedNode.status.toLowerCase()]) {
					this.hideBtn[this.data.selectedNode.status.toLowerCase()] =
						!!this.hideBtn[this.data.selectedNode.status.toLowerCase()];
		}
	}

	/**
	 * Show task detail context menu option
	 **/
	showTaskDetails(): void {
		this.hideCtxMenu();
		let taskDetailModel: TaskDetailModel = {
			id: `${this.data.selectedNode}`,
			modal: {
				title: 'Task Detail'
			},
			detail: {
				currentUserId: this.data.currentUserId
			}
		};

		this.dialogService.extra(TaskDetailComponent, [
			{provide: TaskDetailModel, useValue: taskDetailModel}
		], false, false)
			.then(result => {
				console.log('then');
			}).catch(result => {
			if (result) {
				console.log('catch');
			}
		});
	}

	/**
	 * Edit task context menu option
	 **/
	editTask(): void {
		this.hideCtxMenu();
		let taskDetailModel: TaskDetailModel = {
			id: `${this.data.selectedNode.id}`,
			modal: {
				title: 'Task Edit'
			},
			detail: {
				currentUserId: this.data.currentUserId
			}
		};

		this.dialogService.extra(TaskEditComponent, [
			{provide: TaskDetailModel, useValue: taskDetailModel}
		], false, false)
			.then(result => {
				console.log('then');
			}).catch(result => {
			if (result) {
				console.log('catch');
			}
		});
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

	hideCtxMenu(): void {
		this.renderer.setStyle(this.ctxMenu.nativeElement, 'display', 'none');
	}

	avoidDefault(e: Event): boolean {
		console.log('avoid default triggered');
		e.preventDefault();
		return false;
	}
}
