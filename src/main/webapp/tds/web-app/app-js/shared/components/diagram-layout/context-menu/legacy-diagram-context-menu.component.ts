import {
	Component,
	ElementRef,
	Input,
	OnInit,
	Renderer2,
	SimpleChanges,
	ViewChild
} from '@angular/core';

import {CTX_MENU_ICONS_PATH} from '../../../../modules/taskManager/components/common/constants/task-icon-path';
import {
	HideBtn,
	IHideBtn,
	IDiagramContextMenuModel,
	IDiagramContextMenuField
} from '../model/legacy-diagram-context-menu.model';
import {NotifierService} from '../../../services/notifier.service';
import {PermissionService} from '../../../services/permission.service';
import {IGraphTask} from '../../../../modules/taskManager/model/graph-task.model';
import {LegacyDiagramContextMenuHelper} from './legacy-diagram-context-menu.helper';

@Component({
	selector: 'tds-task-context-menu',
	template: `
		<div id="ctx-menu"
				 (contextmenu)="avoidDefault($event)"
				 #ctxMenu>
			<ul *ngIf="data">
				<li id="hold"
						*ngFor="let option of data.options.fields"
						[ngStyle]="{display: hasPermission(option.permission) && componentSpecificValidations(option) ? 'block' : 'none'}">
					<button class="btn ctx-menu-btn clr-align-self-center" (click)="dispatchAction(option.event)">
						<div class="clr-row">
							<div class="clr-col-2 clr-align-self-center">
								<fa-icon [icon]="option.icon.icon"
										 [styles]="{ color: option.icon.color, float: 'left' }">
								</fa-icon>
							</div>
							<div class="clr-col-10 clr-align-self-center">
								{{ option.label }}
							</div>
						</div>
					</button>
				</li>
			</ul>
		</div>`
})
export class LegacyDiagramContextMenuComponent implements OnInit {
	@Input() data: IDiagramContextMenuModel;
	@ViewChild('ctxMenu', {static: false}) ctxMenu: ElementRef;
	ctxMenuIcons = CTX_MENU_ICONS_PATH;
	hideBtn: IHideBtn = new HideBtn();

	constructor(
		private renderer: Renderer2,
		private notifierService: NotifierService,
		private permissionService: PermissionService
	) {}

	ngOnInit(): void {
		if (this.data && this.data.selectedNode) {
			this.open();
		}
	}

	/**
	 * Detect changes to update nodeData and linksPath accordingly
	 **/
	ngOnChanges(simpleChanges: SimpleChanges): void {
		if (simpleChanges && simpleChanges.data
			&& !(simpleChanges.data.firstChange)
		) {
			this.open();
		}
	}

	/**
	 * Show task detail context menu option
	 **/
	dispatchAction(action: string): void {
		this.hideCtxMenu();
		this.notifierService.broadcast({name: action, node: this.data.selectedNode});
	}

	/**
	 * Apply component specific validation
	 * @param {IDiagramContextMenuField} option => option to run validations on
	 **/
	componentSpecificValidations(option: IDiagramContextMenuField): boolean {
		return LegacyDiagramContextMenuHelper.validate(this.data.options.containerComp,
			option,
			this.data.selectedNode,
			this.data.currentUser);
	}

	/**
	 * Validate if it has permission to see this option
	 **/
	hasPermission(permission: string): boolean {
		if (permission === 'none') { return true; }
		return this.permissionService.hasPermission(permission);
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
