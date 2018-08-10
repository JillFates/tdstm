import {Component, Inject, AfterViewInit, Renderer2} from '@angular/core';
import { StateService } from '@uirouter/angular';
import { NotifierService } from '../../services/notifier.service';
import { AlertType } from '../../model/alert.model';
import { UIPromptService } from '../../directives/ui-prompt.directive';
import { TranslatePipe } from '../../pipes/translate.pipe';

declare var jQuery: any;
@Component({
	selector: 'header',
	templateUrl: '../tds/web-app/app-js/shared/modules/header/header.component.html',
	providers: [TranslatePipe],
	styles: [`.font-weight-bold { font-weight:bold; }`]
})

export class HeaderComponent implements AfterViewInit {

	private state: StateService;
	private pageMetaData: {
		title: string,
		instruction: string,
		menu: Array<string>,
		topMenu: any,
	};
	taskCount: Number;

	constructor(
		@Inject('taskCount') tasks,
		translatePipe: TranslatePipe,
		state: StateService,
		notifierService: NotifierService,
		promptService: UIPromptService,
		private renderer: Renderer2) {
		jQuery('.navbar-nav a[href!="#"]').off('click').on('click', function (e) {
			if (state.$current.data.hasPendingChanges) {
				e.preventDefault();
				promptService.open(
					'Confirmation Required',
					'You have changes that have not been saved. Do you want to continue and lose those changes?',
					'Confirm', 'Cancel').then(result => {
						if (result) {
							state.$current.data.hasPendingChanges = false;
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
		this.state = state;
		// this language will be used as a fallback when a translation isn't found in the current language
		// translate.setDefaultLang('en');

		if (this.state && this.state.$current && this.state.$current.data) {
			this.pageMetaData = this.state.$current.data.page;
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

		if (this.pageMetaData.topMenu.parent) {
			let element = document.getElementsByClassName(this.pageMetaData.topMenu.parent)[0];
			if (element) {
				this.renderer.addClass(element, 'active');
			}
		}
		if (this.pageMetaData.topMenu.child) {
			let element = document.getElementsByClassName(this.pageMetaData.topMenu.child)[0];
			if (element) {
				this.renderer.addClass(element, 'active');
			}
		}
	}
}