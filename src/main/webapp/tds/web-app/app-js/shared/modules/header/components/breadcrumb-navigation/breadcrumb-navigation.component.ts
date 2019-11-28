// Angular
import { Component, Renderer2 } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Title } from '@angular/platform-browser';
// Service
import { NotifierService } from '../../../../services/notifier.service';
import { TaskService } from '../../../../../modules/taskManager/service/task.service';
import { UserContextService } from '../../../../../modules/auth/service/user-context.service';
// Models
import { ASSET_MENU_CSS_TREE } from '../../model/asset-menu.model';
import { UserContextModel } from '../../../../../modules/auth/model/user-context.model';
// Other
import { UIPromptService } from '../../../../directives/ui-prompt.directive';
import { TranslatePipe } from '../../../../pipes/translate.pipe';
import { PageMetadataModel } from '../../model/page-metadata.model';

declare var jQuery: any;

@Component({
	selector: 'tds-breadcrumb-navigation',
	template: `
		<!-- Content Header (Page header) -->
		<section class="content-header" *ngIf="!pageMetaData.hideTopNav && !pageMetaData.hideBreadcrumb">
			<ng-container *ngIf="pageMetaData">
				<h1>
					{{ pageMetaData.title | translate }}
					<small *ngIf="pageMetaData.instruction">{{pageMetaData.instruction | translate}}</small>
				</h1>
				<div
					class="breadcrumb licensing-banner-message"
					*ngIf="userContext.licenseInfo && userContext.licenseInfo.license && userContext.licenseInfo.license.banner">
					<div class="callout">
						{{ userContext.licenseInfo?.license.banner }}
					</div>
				</div>
				<ol class="breadcrumb">
					<li *ngFor="let menu of pageMetaData.menu; let last = last" [ngClass]="{ active: last }">
						<a *ngIf="!last && menu.navigateTo"
							[routerLink]="menu.navigateTo">{{ menu.text || menu | translate }}</a>
						<span *ngIf="!last && !menu.navigateTo">{{menu.text || menu | translate}}</span>
						<ng-container *ngIf="last">
							{{ menu.text || menu | translate }}
						</ng-container>
					</li>
				</ol>
			</ng-container>
		</section>
		<tds-ui-dialog></tds-ui-dialog>
		<tds-ui-prompt></tds-ui-prompt>
	`,
	providers: [TranslatePipe],
	styles: [
		`
			.font-weight-bold {
				font-weight: bold;
			}
		`,
	],
})
export class BreadcrumbNavigationComponent {
	protected userContext: UserContextModel;
	public pageMetaData: PageMetadataModel = new PageMetadataModel();

	constructor(
		private taskService: TaskService,
		private translatePipe: TranslatePipe,
		private route: ActivatedRoute,
		private notifierService: NotifierService,
		private titleService: Title,
		promptService: UIPromptService,
		private renderer: Renderer2,
		private userContextService: UserContextService
	) {
		jQuery('.navbar-nav a[href!="#"]')
			.off('click')
			.on('click', function(e) {
				if (
					this.route &&
					this.route.snapshot.data['hasPendingChanges']
				) {
					e.preventDefault();
					promptService
						.open(
							translatePipe.transform(
								'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'
							),
							translatePipe.transform(
								'GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'
							),
							translatePipe.transform('GLOBAL.CONFIRM'),
							translatePipe.transform('GLOBAL.CANCEL')
						)
						.then(result => {
							if (result) {
								this.route.snapshot.data[
									'hasPendingChanges'
								] = false;
								window.location.assign(e.currentTarget.href);
							}
						});
				}
			});

		// Helps to avoid a hardcoded event on the menu being used on the legacy pages 'showMegaMenu'
		let showMegaMenu = jQuery('.menu-parent-tasks > a');
		if (showMegaMenu && showMegaMenu[0]) {
			showMegaMenu[0].onclick = null;
		}

		this.headerListeners();
		this.getUserContext();
	}

	/**
	 * Create the Lister for any changes made to the Routing that affects the Header Component
	 * Includes breadcrumbs, tiles, and other menu changes
	 */
	private headerListeners(): void {
		this.notifierService.on('notificationRouteChange', event => {
			if (event.event.url.indexOf('/auth/') >= 0) {
				this.pageMetaData.hideTopNav = true;
			}
			this.pageMetaData.hideBreadcrumb = event.event.url.indexOf('/taskManager/') >= 0;
		});

		this.notifierService.on('notificationRouteNavigationEnd', event => {
			if (event.route.snapshot.data && event.route.snapshot.data.page) {
				this.pageMetaData = {...event.route.snapshot.data.page, hideBreadcrumb: this.pageMetaData.hideBreadcrumb};
				const { report } = event.route.snapshot.data;
				this.pageMetaData.id = report && report.id;
				// Set Title
				this.titleService.setTitle(
					this.translatePipe.transform(
						this.pageMetaData.title || '',
						[]
					)
				);
				setTimeout(() => {
					this.selectTopMenuSections();
				});
			}
		});
		this.notifierService.on('notificationHeaderTitleChange', event => {
			// Set Title
			this.titleService.setTitle(event.title);
			this.pageMetaData.title = event.title;
		});
		this.notifierService.on('notificationHeaderBreadcrumbChange', event => {
			// Set Breadcrumb
			this.pageMetaData.menu = event.menu;
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
			let element = document.getElementsByClassName(
				this.pageMetaData.topMenu.parent
			)[0];
			if (element) {
				this.renderer.addClass(element, 'active');
			}
		}
		if (this.pageMetaData.topMenu && this.pageMetaData.topMenu.child) {
			let element = document.getElementsByClassName(
				this.pageMetaData.topMenu.child
			)[0];
			if (element) {
				this.renderer.addClass(element, 'active');
			}
		}

		if (this.pageMetaData.topMenu && this.pageMetaData.topMenu.subMenu) {
			const selectedMenu = this.pageMetaData;
			if (
				ASSET_MENU_CSS_TREE.PARENT_MENU ===
					this.pageMetaData.topMenu.parent &&
				ASSET_MENU_CSS_TREE.CHILD_MENU ===
					this.pageMetaData.topMenu.child
			) {
				jQuery('li.menu-child-item').removeClass('active');
				let elements: any = document.getElementsByClassName(
					ASSET_MENU_CSS_TREE.CHILD_CLASS
				);
				if (elements && elements.length > 0) {
					const targetMenuId =
						selectedMenu.id && selectedMenu.id.toString();
					for (let i = 0; i < elements.length; i++) {
						if (elements[i].firstElementChild.id === targetMenuId) {
							this.renderer.addClass(elements[i], 'active');
						}
					}
				}
			}
		}
	}

	protected getUserContext(): void {
		this.userContextService
			.getUserContext()
			.subscribe((userContext: UserContextModel) => {
				if (!userContext.user) {
					this.pageMetaData.hideTopNav = true;
				}
				this.userContext = userContext;
			});
	}
}
