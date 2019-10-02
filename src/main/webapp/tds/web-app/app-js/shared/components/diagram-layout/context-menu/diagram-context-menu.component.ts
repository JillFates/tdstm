import {
	Component,
	ElementRef,
	Input,
	OnInit,
	Renderer2,
	SimpleChanges,
	ViewChild
} from '@angular/core';

import {CTX_MENU_ICONS_PATH} from '../../../constants/icons-path';
import {HideBtn, IHideBtn, ITaskContextMenuModel} from './diagram-context-menu.model';
import {NotifierService} from '../../../services/notifier.service';

@Component({
	selector: 'tds-task-context-menu',
	template: `
		<div id="ctx-menu"
									(contextmenu)="avoidDefault($event)"
									#ctxMenu>
		<ul>
			<li id="hold" *ngIf="!hideBtn.pending">
				<button class="btn ctx-menu-btn">
					<fa-icon [icon]="ctxMenuIcons.hold.icon"
									 [styles]="{ color: ctxMenuIcons.hold.color, float: 'left' }">
					</fa-icon>
					Hold
				</button>
			</li>
			<li id="start" *ngIf="!hideBtn.ready">
				<button class="btn ctx-menu-btn">
					<fa-icon [icon]="ctxMenuIcons.start.icon"
									 [styles]="{ color: ctxMenuIcons.start.color, float: 'left' }">
					</fa-icon>
					Start
				</button>
			</li>
			<li id="done" *ngIf="!hideBtn.completed">
				<button class="btn ctx-menu-btn">
					<fa-icon [icon]="ctxMenuIcons.done.icon"
									 [styles]="{ color: ctxMenuIcons.done.color, float: 'left' }">
					</fa-icon>
					Done
				</button>
			</li>
			<li id="reset" *ngIf="!hideBtn.reset">
				<button class="btn ctx-menu-btn">
					<fa-icon [icon]="ctxMenuIcons.invoke.icon"
									 [styles]="{ color: ctxMenuIcons.invoke.color, float: 'left' }">
					</fa-icon>
					Reset
				</button>
			</li>
			<li id="invoke" *ngIf="!hideBtn.invoke">
				<button class="btn ctx-menu-btn">
					<fa-icon [icon]="ctxMenuIcons.invoke.icon"
									 [styles]="{ color: ctxMenuIcons.invoke.color, float: 'left' }">
					</fa-icon>
					Invoke
				</button>
			</li>
			<hr class="ctx-menu-divider"/>
			<li id="edit">
				<button class="btn ctx-menu-btn" (click)="dispatchAction('editTask')">
					<fa-icon [icon]="ctxMenuIcons.edit.icon"
									 [styles]="{ color: ctxMenuIcons.edit.color, float: 'left' }">
					</fa-icon>
					Edit
				</button>
			</li>
			<li id="view">
				<button class="btn ctx-menu-btn" (click)="dispatchAction('showTaskDetails')">
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
	</div>`
})
export class DiagramContextMenuComponent implements OnInit {
	@Input() data: ITaskContextMenuModel;
	@ViewChild('ctxMenu') ctxMenu: ElementRef;
	ctxMenuIcons = CTX_MENU_ICONS_PATH;
	hideBtn: IHideBtn = new HideBtn();

	constructor(
		private renderer: Renderer2,
		private notifierService: NotifierService
	) {}

	ngOnInit(): void {
		if (this.data && this.data.selectedNode) {
			this.hideBtnByStatus();
			this.open();
		}
	}

	/**
	 * Detect changes to update nodeDataArray and linksPath accordingly
	 **/
	ngOnChanges(simpleChanges: SimpleChanges): void {
		if (simpleChanges && simpleChanges.data
			&& !(simpleChanges.data.firstChange)
		) {
			this.hideBtnByStatus();
			this.open();
		}
	}

	hideBtnByStatus(): void {
		console.log('hideBtn: ', this.data && this.data.selectedNode.status.toLowerCase());

		if (this.data
			&& this.data.selectedNode
			&& this.data.selectedNode.status
			&& Object.keys(this.hideBtn).includes(this.data.selectedNode.status.toLowerCase())) {

				this.hideBtn = new HideBtn();
				this.hideBtn[this.data.selectedNode.status.toLowerCase()] = true;
		}
	}

	/**
	 * Show task detail context menu option
	 **/
	dispatchAction(action: string): void {
		this.hideCtxMenu();
		this.notifierService.broadcast({taskId: this.data.selectedNode.id, action});
	}

	open(): void {
		if (this.ctxMenu && this.ctxMenu.nativeElement) {
			this.renderer.setStyle(this.ctxMenu.nativeElement, 'display', 'block');
			this.renderer.setStyle(this.ctxMenu.nativeElement, 'left', `${this.data.mousePt.x}`);
			this.renderer.setStyle(this.ctxMenu.nativeElement, 'top', `${this.data.mousePt.y}`);
		}
	}

	hideCtxMenu(): void {
		this.renderer.setStyle(this.ctxMenu.nativeElement, 'display', 'none');
	}

	avoidDefault(e: Event): boolean {
		e.preventDefault();
		return false;
	}
}
