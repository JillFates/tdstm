import {Component, Renderer2} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {NotifierService} from '../../services/notifier.service';
import {UIPromptService} from '../../directives/ui-prompt.directive';
import {TranslatePipe} from '../../pipes/translate.pipe';
import {ASSET_MENU_CSS_TREE} from './model/asset-menu.model';
import {TaskService} from '../../../modules/taskManager/service/task.service';
import {Title} from '@angular/platform-browser';
import {UserPreferencesComponent} from '../../../modules/user/components/preferences/user-preferences.component';
import {UserService} from '../../../modules/user/service/user.service';
import {UIDialogService} from '../../services/ui-dialog.service';

declare var jQuery: any;

@Component({
	selector: 'tds-header',
	template: `
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <ng-container *ngIf="pageMetaData">
	            <!-- Used for the user preferences until fully converted to angular -->
                <span (click)="openPrefModal()" class="open-pref-modal"></span>
                <h1>
                    {{pageMetaData.title | translate}}
                    <small>{{pageMetaData.instruction | translate}}</small>
                </h1>
                <ol class="breadcrumb">
                    <li *ngFor="let menu of pageMetaData.menu; let last = last;" [ngClass]="{'active' : last}" >
                        <a *ngIf="!last">{{(menu.text || menu) | translate}}</a>
                        <ng-container *ngIf="last">
                            {{ menu | translate }}
                        </ng-container>
                    </li>
                </ol>
            </ng-container>
        </section>
        <tds-ui-dialog></tds-ui-dialog>
        <tds-ui-prompt></tds-ui-prompt>
	`,
	providers: [TranslatePipe],
	styles: [`.font-weight-bold {
        font-weight: bold;
    }`]
})

export class HeaderComponent {

	private pageMetaData: {
		id: any,
		title: string,
		instruction: string,
		menu: Array<string>,
		topMenu: any
	};

	constructor(
		private taskService: TaskService,
		private translatePipe: TranslatePipe,
		private route: ActivatedRoute,
		private notifierService: NotifierService,
		private titleService: Title,
		promptService: UIPromptService,
		private dialogService: UIDialogService,
		private renderer: Renderer2) {
		jQuery('.navbar-nav a[href!="#"]').off('click').on('click', function (e) {
			if (this.route && this.route.snapshot.data['hasPendingChanges']) {
				e.preventDefault();
				promptService.open(
					'Confirmation Required',
					'You have changes that have not been saved. Do you want to continue and lose those changes?',
					'Confirm', 'Cancel').then(result => {
					if (result) {
						this.route.snapshot.data['hasPendingChanges'] = false;
						window.location.assign(e.currentTarget.href);
					}
				});
			}
		});

		this.taskService.retrieveUserToDoCount().subscribe(
			(result) => {
				// Please refer to https://kb.transitionmanager.com/display/TMENG/FE%3A+Workaround #3
				jQuery('#todoCountProjectId').html(result.count);
			}
		);

		// Helps to avoid a hardcoded event on the menu being used on the legacy pages 'showMegaMenu'
		let showMegaMenu = jQuery('.menu-parent-tasks > a');
		if (showMegaMenu && showMegaMenu[0]) {
			showMegaMenu[0].onclick = null;
		}

		this.headerListeners();
	}

	/**
	 * Create the Lister for any changes made to the Routing that affects the Header Component
	 * Includes breadcrumbs, tiles, and other menu changes
	 */
	private headerListeners(): void {
		this.notifierService.on('notificationRouteNavigationEnd', event => {
			if (event.route.snapshot.data && event.route.snapshot.data.page) {
				this.pageMetaData = event.route.snapshot.data.page;
				const {report} = event.route.snapshot.data;
				this.pageMetaData.id = report && report.id;
				// Set Title
				this.titleService.setTitle(this.translatePipe.transform(this.pageMetaData.title || '', []));
				this.selectTopMenuSections();
			}
		});
		this.notifierService.on('notificationHeaderTitleChange', event => {
			// Set Title
			this.titleService.setTitle(event.title);
			this.pageMetaData.title = event.title;
		});
	}

	/**
	 * Adds an active class to application top menu based on the pageMetadata.topMenu configuration.
	 */
	private selectTopMenuSections(): void {
		// clear out any other previous active menus.
		jQuery('li[class^="dropdown menu-parent-"]').removeClass('active');
		jQuery('li.menu-child-item').removeClass('active');

		if (this.pageMetaData.topMenu && this.pageMetaData.topMenu.parent) {
			let element = document.getElementsByClassName(this.pageMetaData.topMenu.parent)[0];
			if (element) {
				this.renderer.addClass(element, 'active');
			}
		}
		if (this.pageMetaData.topMenu && this.pageMetaData.topMenu.child) {
			let element = document.getElementsByClassName(this.pageMetaData.topMenu.child)[0];
			if (element) {
				this.renderer.addClass(element, 'active');
			}
		}

		if (this.pageMetaData.topMenu && this.pageMetaData.topMenu.subMenu) {
			const selectedMenu = this.pageMetaData;
			if (ASSET_MENU_CSS_TREE.PARENT_MENU === this.pageMetaData.topMenu.parent
				&& ASSET_MENU_CSS_TREE.CHILD_MENU === this.pageMetaData.topMenu.child) {
				jQuery('li.menu-child-item').removeClass('active');
				let elements: any = document.getElementsByClassName(ASSET_MENU_CSS_TREE.CHILD_CLASS);
				if (elements && elements.length > 0) {
					const targetMenuId = selectedMenu.id && selectedMenu.id.toString();
					for (let i = 0; i < elements.length; i++) {
						if (elements[i].firstElementChild.id === targetMenuId) {
							this.renderer.addClass(elements[i], 'active');
						}
					}
				}
			}
		}
	}

	/**
	 * This is a hack to open the modal window properly before the user menu is angular.
	 */
	public openPrefModal(): void {
		this.dialogService.open(UserPreferencesComponent, []).catch(result => {
			if (result) {
				console.error(result);
			}
		});
	}
}