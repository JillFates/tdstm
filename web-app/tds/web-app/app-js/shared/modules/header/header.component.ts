import {Component, Inject, AfterViewInit, Renderer2} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import { NotifierService } from '../../services/notifier.service';
import { AlertType } from '../../model/alert.model';
import { UIPromptService } from '../../directives/ui-prompt.directive';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { ASSET_MENU_CSS_TREE } from './model/asset-menu.model';

declare var jQuery: any;
@Component({
	selector: 'tds-header',
	templateUrl: '../tds/web-app/app-js/shared/modules/header/header.component.html',
	providers: [TranslatePipe],
	styles: [`.font-weight-bold { font-weight:bold; }`]
})

export class HeaderComponent implements AfterViewInit {

	private pageMetaData: {
		id: any,
		title: string,
		instruction: string,
		menu: Array<string>,
		topMenu: any
	};

	taskCount: Number;

	constructor(
		@Inject('taskCount') tasks,
		translatePipe: TranslatePipe,
		private route: ActivatedRoute,
		notifierService: NotifierService,
		promptService: UIPromptService,
		private renderer: Renderer2) {
		jQuery('.navbar-nav a[href!="#"]').off('click').on('click', function (e) {
			if (this.route.snapshot.data['hasPendingChanges']) {
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
		tasks.subscribe(
			(result) => {
				this.taskCount = result.count;
				// Please refer to https://kb.transitionmanager.com/display/TMENG/FE%3A+Workaround #3
				jQuery('#todoCountProjectId').html(this.taskCount);

			},
			(err) => {
				notifierService.broadcast({
					name: AlertType.WARNING,
					message: err
				});

				console.log(err);
			});

		if (this.route && this.route.snapshot && route.snapshot.data && route.snapshot.data.page) {
			this.pageMetaData = {
				id: route.snapshot.data.page.id,
				title: route.snapshot.data.page.title,
				instruction: route.snapshot.data.page.instruction,
				menu: route.snapshot.data.page.menu,
				topMenu: route.snapshot.data.pagetopMenu
			};

			document.title = translatePipe.transform(this.pageMetaData.title, []);
		}
	}

	ngAfterViewInit(): void {
		// Please refer to https://kb.transitionmanager.com/display/TMENG/FE%3A+Workaround #3
		jQuery('.menu-parent-tasks > a')[0].onclick = null;
		this.selectTopMenuSections();
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
					for (let i = 0; i < elements.length; i++) {
						if (elements[i].firstElementChild.id === selectedMenu.id) {
							this.renderer.addClass(elements[i], 'active');
						}
					}
				}
			}
		}
	}
}